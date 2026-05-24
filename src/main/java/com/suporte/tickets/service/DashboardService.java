package com.suporte.tickets.service;

import com.suporte.tickets.dto.ClientePendenciasDTO;
import com.suporte.tickets.dto.DashboardAnalistaResumoDTO;
import com.suporte.tickets.dto.DashboardGerencialDTO;
import com.suporte.tickets.dto.DashboardGrupoDTO;
import com.suporte.tickets.dto.DashboardPrioridadeTotaisDTO;
import com.suporte.tickets.dto.DashboardResumoDTO;
import com.suporte.tickets.dto.DashboardSatisfacaoResumoDTO;
import com.suporte.tickets.dto.DashboardSubgrupoResumoDTO;
import com.suporte.tickets.dto.IndicadoresEncerramentoAvaliacaoDTO;
import com.suporte.tickets.dto.TicketPendenciaClienteDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.StatusOperador;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final String CLIENTE_NAO_INFORMADO = "Sem cliente";
    private static final int TOP_CATEGORIAS = 5;
    private static final int LIMITE_TICKETS_CRITICOS_ALTOS = 10;
    /** Período padrão dos cards de encerramento/satisfação no Dashboard (Sprint 203). */
    public static final int DIAS_PERIODO_ENCERRAMENTO_SATISFACAO = 30;

    private final TicketRepository ticketRepository;
    private final ClienteRepository clienteRepository;
    private final AnalistaRepository analistaRepository;
    private final TicketService ticketService;
    private final IndicadoresEncerramentoAvaliacaoService indicadoresEncerramentoAvaliacaoService;

    @Transactional(readOnly = true)
    public DashboardResumoDTO obterResumo() {
        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();

        DashboardResumoDTO resumo = new DashboardResumoDTO();
        resumo.setTotalTickets(ticketRepository.count());
        resumo.setTicketsAbertos(ticketRepository.countByStatus(TicketStatus.ABERTO));
        resumo.setTicketsEmAtendimento(ticketRepository.countByStatus(TicketStatus.EM_ATENDIMENTO));
        resumo.setTicketsResolvidos(ticketRepository.countByStatus(TicketStatus.RESOLVIDO));
        resumo.setTicketsCancelados(ticketRepository.countByStatus(TicketStatus.CANCELADO));
        resumo.setTicketsNaoAtendimento(ticketRepository.countByStatus(TicketStatus.INDEVIDO));
        resumo.setTicketsSemAnalista(ticketRepository.countByAnalistaResponsavelIsNullAndStatusIn(
                List.copyOf(TicketAtivoService.STATUS_ATIVOS)));
        resumo.setTicketsAbertosHoje(ticketRepository.countByDataAberturaGreaterThanEqual(inicioHoje));
        resumo.setTicketsResolvidosHoje(
                ticketRepository.countByStatusAndDataEncerramentoGreaterThanEqual(TicketStatus.RESOLVIDO, inicioHoje));

        resumo.setClientesAtivos(clienteRepository.countByAtivoTrue());
        resumo.setAnalistasOnline(analistaRepository.countByAtivoTrueAndStatusOperador(StatusOperador.ONLINE));
        resumo.setAnalistasAusentes(analistaRepository.countByAtivoTrueAndStatusOperador(StatusOperador.AUSENTE));
        resumo.setAnalistasOffline(analistaRepository.countByAtivoTrueAndStatusOperador(StatusOperador.OFFLINE));

        resumo.setTempoMedioPrimeiroAtendimento(calcularTempoMedioPrimeiroAtendimento());
        resumo.setTempoMedioResolucao(calcularTempoMedioResolucao());
        resumo.setTicketsPorStatus(agruparPorStatus());
        resumo.setTicketsPorCliente(agruparPorCliente());
        resumo.setTicketsPorAnalista(agruparPorAnalista());

        return resumo;
    }

    /**
     * Período do card de encerramento no Dashboard: apenas 7, 30 ou 90 dias (padrão 30).
     */
    public static int normalizarDiasPeriodoEncerramento(Integer dias) {
        if (dias == null) {
            return DIAS_PERIODO_ENCERRAMENTO_SATISFACAO;
        }
        if (dias == 7 || dias == 30 || dias == 90) {
            return dias;
        }
        return DIAS_PERIODO_ENCERRAMENTO_SATISFACAO;
    }

    @Transactional(readOnly = true)
    public DashboardSatisfacaoResumoDTO obterEncerramentoSatisfacaoResumo(Integer dias, Integer clienteId) {
        int diasPeriodo = normalizarDiasPeriodoEncerramento(dias);
        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.minusDays(diasPeriodo);
        IndicadoresEncerramentoAvaliacaoDTO indicadores = indicadoresEncerramentoAvaliacaoService.obter(
                inicio, fim, clienteId, null, null, null);
        return DashboardEncerramentoSatisfacaoResumoMapper.fromIndicadores(
                indicadores, inicio.toString(), fim.toString());
    }

    @Transactional(readOnly = true)
    public DashboardGerencialDTO obterGerencial() {
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> TicketAtivoService.isAtendimentoOperacionalValido(t.getStatus()))
                .toList();
        DashboardGerencialDTO gerencial = new DashboardGerencialDTO();
        gerencial.setTotaisPorPrioridade(agruparPorPrioridade(tickets));
        gerencial.setTopGrupos(agruparTopGrupos(tickets));
        gerencial.setTopSubgrupos(agruparTopSubgrupos(tickets));
        gerencial.setTicketsCriticosAltos(listarTicketsCriticosAltos(tickets));
        return gerencial;
    }

    @Transactional(readOnly = true)
    public List<ClientePendenciasDTO> listarPendenciasPorCliente() {
        LocalDateTime agora = LocalDateTime.now();
        List<Ticket> ticketsAtivos = ticketRepository.findByStatusInOrderByDataAberturaAsc(
                List.of(TicketStatus.ABERTO, TicketStatus.AGUARDANDO_CLIENTE, TicketStatus.EM_ATENDIMENTO)
        );

        Map<String, List<TicketPendenciaClienteDTO>> porCliente = new LinkedHashMap<>();
        for (Ticket ticket : ticketsAtivos) {
            String cliente = normalizarNomeCliente(ticket);
            porCliente
                    .computeIfAbsent(cliente, chave -> new ArrayList<>())
                    .add(converterPendencia(ticket, agora));
        }

        return porCliente.entrySet()
                .stream()
                .map(entry -> new ClientePendenciasDTO(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue()
                ))
                .toList();
    }

    private String normalizarNomeCliente(Ticket ticket) {
        if (ticket.getCliente() != null && ticket.getCliente().getNome() != null
                && !ticket.getCliente().getNome().isBlank()) {
            return ticket.getCliente().getNome().trim();
        }
        return CLIENTE_NAO_INFORMADO;
    }

    private TicketPendenciaClienteDTO converterPendencia(Ticket ticket, LocalDateTime agora) {
        String cliente = ticket.getCliente() != null ? ticket.getCliente().getNome() : "-";
        String contato = resolverContatoPendencia(ticket);
        boolean emAtendimento = ticket.getStatus() == TicketStatus.EM_ATENDIMENTO;
        String tipoStatus = emAtendimento ? "ATENDENDO" : "EM_ESPERA";
        String analista = "-";
        String tmaFormatado = "-";
        if (emAtendimento && ticket.getAnalistaResponsavel() != null) {
            String nome = ticket.getAnalistaResponsavel().getNome();
            analista = (nome != null && !nome.isBlank()) ? nome.trim() : "-";
            LocalDateTime inicio = ticket.getDataPrimeiroAtendimento() != null
                    ? ticket.getDataPrimeiroAtendimento()
                    : ticket.getDataAbertura();
            if (inicio != null) {
                long tmaSeg = Duration.between(inicio, agora).getSeconds();
                tmaFormatado = tmaSeg >= 0 ? formatarDuracaoSeg(tmaSeg) : "-";
            }
        }
        return new TicketPendenciaClienteDTO(
                ticket.getNumeroTicket(),
                cliente,
                contato,
                ticket.getMensagemInicial(),
                ticket.getStatus().name(),
                tipoStatus,
                analista,
                tmaFormatado,
                ticket.getDataAbertura()
        );
    }

    private String resolverContatoPendencia(Ticket ticket) {
        Contato ct = ticket.getContato();
        if (ct != null && ct.getNome() != null && !ct.getNome().isBlank()) {
            return ct.getNome().trim();
        }
        return "-";
    }

    private static String formatarDuracaoSeg(long segundos) {
        Duration d = Duration.ofSeconds(segundos);
        return String.format("%02d:%02d:%02d", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }

    private String calcularTempoMedioPrimeiroAtendimento() {
        List<Ticket> tickets = ticketRepository.findByDataPrimeiroAtendimentoIsNotNullAndDataAberturaIsNotNull();
        return formatarMediaDuracao(tickets.stream()
                .filter(t -> TicketAtivoService.isAtendimentoOperacionalValido(t.getStatus()))
                .map(ticket -> Duration.between(ticket.getDataAbertura(), ticket.getDataPrimeiroAtendimento()).getSeconds())
                .filter(segundos -> segundos >= 0)
                .toList());
    }

    private String calcularTempoMedioResolucao() {
        List<Ticket> tickets = ticketRepository.findByDataEncerramentoIsNotNullAndDataAberturaIsNotNull();
        return formatarMediaDuracao(tickets.stream()
                .filter(t -> TicketAtivoService.isAtendimentoOperacionalValido(t.getStatus()))
                .map(ticket -> Duration.between(ticket.getDataAbertura(), ticket.getDataEncerramento()).getSeconds())
                .filter(segundos -> segundos >= 0)
                .toList());
    }

    private String formatarMediaDuracao(List<Long> segundosLista) {
        if (segundosLista == null || segundosLista.isEmpty()) {
            return "-";
        }
        long mediaSegundos = Math.round(segundosLista.stream().mapToLong(Long::longValue).average().orElse(0));
        Duration duration = Duration.ofSeconds(mediaSegundos);
        long horas = duration.toHours();
        long minutos = duration.toMinutesPart();
        long segundos = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    private DashboardPrioridadeTotaisDTO agruparPorPrioridade(List<Ticket> tickets) {
        DashboardPrioridadeTotaisDTO totais = new DashboardPrioridadeTotaisDTO();
        for (Ticket ticket : tickets) {
            PrioridadeTicket prioridade = ticket.getPrioridade();
            if (prioridade == null) {
                totais.setSemPrioridade(totais.getSemPrioridade() + 1);
                continue;
            }
            switch (prioridade) {
                case CRITICA -> totais.setCritica(totais.getCritica() + 1);
                case ALTA -> totais.setAlta(totais.getAlta() + 1);
                case MEDIA -> totais.setMedia(totais.getMedia() + 1);
                case BAIXA -> totais.setBaixa(totais.getBaixa() + 1);
                default -> totais.setSemPrioridade(totais.getSemPrioridade() + 1);
            }
        }
        return totais;
    }

    private List<DashboardGrupoDTO> agruparTopGrupos(List<Ticket> tickets) {
        Map<String, Long> agrupado = tickets.stream()
                .filter(ticket -> ticket.getGrupoCategoria() != null
                        && ticket.getGrupoCategoria().getNome() != null
                        && !ticket.getGrupoCategoria().getNome().isBlank())
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getGrupoCategoria().getNome().trim(),
                        Collectors.counting()
                ));

        return agrupado.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(TOP_CATEGORIAS)
                .map(entry -> new DashboardGrupoDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<DashboardSubgrupoResumoDTO> agruparTopSubgrupos(List<Ticket> tickets) {
        Map<String, DashboardSubgrupoResumoDTO> agrupado = new LinkedHashMap<>();
        for (Ticket ticket : tickets) {
            if (ticket.getSubgrupoCategoria() == null
                    || ticket.getSubgrupoCategoria().getNome() == null
                    || ticket.getSubgrupoCategoria().getNome().isBlank()) {
                continue;
            }
            String subgrupoNome = ticket.getSubgrupoCategoria().getNome().trim();
            String grupoNome = ticket.getGrupoCategoria() != null && ticket.getGrupoCategoria().getNome() != null
                    ? ticket.getGrupoCategoria().getNome().trim()
                    : "-";
            String chave = subgrupoNome + "|" + grupoNome;
            DashboardSubgrupoResumoDTO atual = agrupado.get(chave);
            if (atual == null) {
                agrupado.put(chave, new DashboardSubgrupoResumoDTO(subgrupoNome, grupoNome, 1));
            } else {
                atual.setTotal(atual.getTotal() + 1);
            }
        }

        return agrupado.values().stream()
                .sorted(Comparator.comparing(DashboardSubgrupoResumoDTO::getTotal).reversed()
                        .thenComparing(DashboardSubgrupoResumoDTO::getNome))
                .limit(TOP_CATEGORIAS)
                .toList();
    }

    private List<TicketResponseDTO> listarTicketsCriticosAltos(List<Ticket> tickets) {
        return tickets.stream()
                .filter(ticket -> TicketAtivoService.isStatusAtivo(ticket.getStatus()))
                .filter(ticket -> ticket.getPrioridade() == PrioridadeTicket.CRITICA
                        || ticket.getPrioridade() == PrioridadeTicket.ALTA)
                .sorted(Comparator
                        .comparingInt((Ticket ticket) -> prioridadeRank(ticket.getPrioridade()))
                        .thenComparing(Ticket::getDataAbertura, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(LIMITE_TICKETS_CRITICOS_ALTOS)
                .map(ticketService::converterParaResponseSeguro)
                .toList();
    }

    private int prioridadeRank(PrioridadeTicket prioridade) {
        if (prioridade == PrioridadeTicket.CRITICA) {
            return 0;
        }
        if (prioridade == PrioridadeTicket.ALTA) {
            return 1;
        }
        return 2;
    }

    private List<DashboardGrupoDTO> agruparPorStatus() {
        Map<String, Long> agrupado = ticketRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getStatus() != null ? ticket.getStatus().name() : "SEM_STATUS",
                        Collectors.counting()
                ));

        return agrupado.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardGrupoDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<DashboardGrupoDTO> agruparPorCliente() {
        Map<String, Long> agrupado = ticketRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        this::normalizarNomeCliente,
                        Collectors.counting()
                ));

        return agrupado.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardGrupoDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<DashboardAnalistaResumoDTO> agruparPorAnalista() {
        Map<String, DashboardAnalistaResumoDTO> agrupado = new LinkedHashMap<>();

        for (Ticket ticket : ticketRepository.findAll()) {
            if (TicketAtivoService.isTicketIndevido(ticket.getStatus())) {
                continue;
            }
            Analista analista = ticket.getAnalistaResponsavel();
            String nome = analista != null ? textoOuPadrao(analista.getNome(), "Analista") : "Sem analista";
            String status = analista != null && analista.getStatusOperador() != null
                    ? analista.getStatusOperador().name()
                    : "-";
            String chave = nome + "|" + status;

            DashboardAnalistaResumoDTO atual = agrupado.get(chave);
            if (atual == null) {
                agrupado.put(chave, new DashboardAnalistaResumoDTO(nome, status, 1));
            } else {
                atual.setTotal(atual.getTotal() + 1);
            }
        }

        return agrupado.values().stream()
                .sorted(Comparator.comparing(DashboardAnalistaResumoDTO::getNome))
                .toList();
    }

    private String textoOuPadrao(String valor, String padrao) {
        if (valor == null || valor.isBlank()) {
            return padrao;
        }
        return valor.trim();
    }
}

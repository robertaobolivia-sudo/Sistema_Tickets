package com.suporte.tickets.service;

import com.suporte.tickets.dto.DashboardOperacaoChamadoB2BDTO;
import com.suporte.tickets.dto.DashboardOperacaoClienteB2BCardDTO;
import com.suporte.tickets.dto.DashboardOperacaoClienteB2BDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.InteracaoPendenteDecisao;
import com.suporte.tickets.entity.InteracaoPendenteDecisaoStatus;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.InteracaoPendenteDecisaoRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardOperacaoClienteB2BService {

    public static final String TIPO_TICKET = "TICKET";
    public static final String TIPO_PENDENCIA = "PENDENCIA_DECISAO";

    private static final List<TicketStatus> STATUS_OPERACIONAIS = List.copyOf(TicketAtivoService.STATUS_ATIVOS);

    private final ClienteRepository clienteRepository;
    private final TicketRepository ticketRepository;
    private final InteracaoPendenteDecisaoRepository interacaoPendenteDecisaoRepository;

    @Transactional(readOnly = true)
    public DashboardOperacaoClienteB2BDTO obter() {
        LocalDateTime agora = LocalDateTime.now();
        Map<Integer, DashboardOperacaoClienteB2BCardDTO> porCliente = new LinkedHashMap<>();

        for (Cliente cliente : clienteRepository.findByAtivoTrueOrderByDataCadastroDesc()) {
            if (cliente.getId() == null) {
                continue;
            }
            String nome = cliente.getNome() != null && !cliente.getNome().isBlank()
                    ? cliente.getNome().trim()
                    : "Cliente " + cliente.getId();
            porCliente.put(cliente.getId(), new DashboardOperacaoClienteB2BCardDTO(cliente.getId(), nome, new ArrayList<>()));
        }

        List<Ticket> tickets = ticketRepository.findOperacionaisPorClientesAtivos(STATUS_OPERACIONAIS);
        for (Ticket ticket : tickets) {
            if (ticket.getCliente() == null || ticket.getCliente().getId() == null) {
                continue;
            }
            DashboardOperacaoClienteB2BCardDTO card = porCliente.computeIfAbsent(
                    ticket.getCliente().getId(),
                    id -> novoCard(ticket.getCliente()));
            card.getChamados().add(mapearTicket(ticket, agora));
        }

        List<InteracaoPendenteDecisao> pendencias = interacaoPendenteDecisaoRepository
                .findPendentesPorClientesAtivos(InteracaoPendenteDecisaoStatus.PENDENTE);
        for (InteracaoPendenteDecisao p : pendencias) {
            if (p.getCliente() == null || p.getCliente().getId() == null) {
                continue;
            }
            DashboardOperacaoClienteB2BCardDTO card = porCliente.computeIfAbsent(
                    p.getCliente().getId(),
                    id -> novoCard(p.getCliente()));
            card.getChamados().add(mapearPendencia(p, agora));
        }

        List<DashboardOperacaoClienteB2BCardDTO> clientes = new ArrayList<>(porCliente.values());
        clientes.sort(Comparator.comparing(DashboardOperacaoClienteB2BCardDTO::getClienteNome,
                String.CASE_INSENSITIVE_ORDER));
        for (DashboardOperacaoClienteB2BCardDTO card : clientes) {
            card.getChamados().sort(Comparator
                    .comparing(DashboardOperacaoChamadoB2BDTO::getNumeroTicket, String.CASE_INSENSITIVE_ORDER));
        }

        return new DashboardOperacaoClienteB2BDTO(clientes);
    }

    private static DashboardOperacaoClienteB2BCardDTO novoCard(Cliente cliente) {
        String nome = cliente.getNome() != null && !cliente.getNome().isBlank()
                ? cliente.getNome().trim()
                : "Cliente " + cliente.getId();
        return new DashboardOperacaoClienteB2BCardDTO(cliente.getId(), nome, new ArrayList<>());
    }

    static DashboardOperacaoChamadoB2BDTO mapearTicket(Ticket ticket, LocalDateTime agora) {
        LocalDateTime abertura = ticket.getDataAbertura();
        long tmeSeg = abertura != null ? Duration.between(abertura, agora).getSeconds() : -1;

        LocalDateTime inicioAtendimento = ticket.getDataPrimeiroAtendimento() != null
                ? ticket.getDataPrimeiroAtendimento()
                : (ticket.getStatus() == TicketStatus.EM_ATENDIMENTO ? abertura : null);
        long tmaSeg = inicioAtendimento != null ? Duration.between(inicioAtendimento, agora).getSeconds() : -1;

        String tmaFmt = ticket.getStatus() == TicketStatus.EM_ATENDIMENTO
                ? formatarDuracao(tmaSeg)
                : "-";

        return new DashboardOperacaoChamadoB2BDTO(
                TIPO_TICKET,
                ticket.getNumeroTicket(),
                resolverContatoTicket(ticket),
                ticket.getStatus() != null ? ticket.getStatus().name() : "-",
                resolverAnalista(ticket.getAnalistaResponsavel()),
                formatarDuracao(tmeSeg),
                tmaFmt,
                null);
    }

    static DashboardOperacaoChamadoB2BDTO mapearPendencia(InteracaoPendenteDecisao p, LocalDateTime agora) {
        String anterior = p.getTicketAnterior() != null ? p.getTicketAnterior().getNumeroTicket() : null;
        String rotulo = anterior != null ? anterior + " · pendência" : "Pendência pós-encerramento";
        long tmeSeg = p.getCriadaEm() != null ? Duration.between(p.getCriadaEm(), agora).getSeconds() : -1;
        Contato ct = p.getContato();
        String contato = ct != null && ct.getNome() != null && !ct.getNome().isBlank()
                ? ct.getNome().trim()
                : (ct != null ? ct.getWhatsappNormalizado() : "-");

        return new DashboardOperacaoChamadoB2BDTO(
                TIPO_PENDENCIA,
                rotulo,
                contato,
                "PENDENCIA_DECISAO",
                "-",
                formatarDuracao(tmeSeg),
                "-",
                p.getId());
    }

    static String resolverContatoTicket(Ticket ticket) {
        Contato ct = ticket.getContato();
        if (ct != null && ct.getNome() != null && !ct.getNome().isBlank()) {
            return ct.getNome().trim();
        }
        return "-";
    }

    static String resolverAnalista(Analista analista) {
        if (analista == null || analista.getNome() == null || analista.getNome().isBlank()) {
            return "-";
        }
        return analista.getNome().trim();
    }

    static String formatarDuracao(long segundos) {
        if (segundos < 0) {
            return "-";
        }
        Duration d = Duration.ofSeconds(segundos);
        return String.format("%02d:%02d:%02d", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }
}

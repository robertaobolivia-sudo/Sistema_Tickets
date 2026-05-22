package com.suporte.tickets.service;

import com.suporte.tickets.dto.IndicadorMotivoItemDTO;
import com.suporte.tickets.dto.IndicadoresEncerramentoAvaliacaoDTO;
import com.suporte.tickets.dto.IndicadoresEnvioResumoDTO;
import com.suporte.tickets.dto.IndicadoresPesquisaResumoDTO;
import com.suporte.tickets.entity.Motivo;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoEnvioStatus;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndicadoresEncerramentoAvaliacaoService {

    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public IndicadoresEncerramentoAvaliacaoDTO obter(
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer clienteId,
            Long motivoId,
            String statusPesquisa,
            Integer notaAvaliacao) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior a data final.");
        }
        if (notaAvaliacao != null && (notaAvaliacao < 1 || notaAvaliacao > 5)) {
            throw new IllegalArgumentException("Nota deve ser entre 1 e 5.");
        }
        TicketSatisfacaoStatus statusSat = resolverStatusPesquisa(statusPesquisa);

        List<TicketSatisfacao> avaliacoes = ticketSatisfacaoRepository.findForIndicadoresEncerramento(
                inicio, fim, clienteId, motivoId, statusSat, notaAvaliacao);

        List<Ticket> ticketsMotivo = ticketRepository.findEncerradosComMotivoParaIndicadores(
                inicio, fim, clienteId, motivoId);

        IndicadoresEncerramentoAvaliacaoDTO dto = new IndicadoresEncerramentoAvaliacaoDTO();
        dto.setTopMotivos(agregarTopMotivos(ticketsMotivo));
        dto.setPesquisa(agregarPesquisa(avaliacoes));
        dto.setEnvio(agregarEnvio(avaliacoes));
        return dto;
    }

    static TicketSatisfacaoStatus resolverStatusPesquisa(String statusPesquisa) {
        if (statusPesquisa == null || statusPesquisa.isBlank()) {
            return null;
        }
        return TicketSatisfacaoStatus.valueOf(statusPesquisa.trim().toUpperCase(Locale.ROOT));
    }

    static List<IndicadorMotivoItemDTO> agregarTopMotivos(List<Ticket> tickets) {
        Map<Long, IndicadorMotivoItemDTO> mapa = new LinkedHashMap<>();
        for (Ticket t : tickets) {
            Motivo m = t.getMotivo();
            if (m == null || m.getId() == null) {
                continue;
            }
            IndicadorMotivoItemDTO item = mapa.computeIfAbsent(m.getId(), id -> {
                IndicadorMotivoItemDTO dto = new IndicadorMotivoItemDTO();
                dto.setMotivoId(m.getId());
                dto.setMotivoNome(m.getNome());
                if (m.getSubgrupoCategoria() != null) {
                    dto.setSubcategoriaNome(m.getSubgrupoCategoria().getNome());
                    if (m.getSubgrupoCategoria().getGrupoCategoria() != null) {
                        dto.setCategoriaNome(m.getSubgrupoCategoria().getGrupoCategoria().getNome());
                    }
                }
                dto.setTotalTickets(0L);
                return dto;
            });
            item.setTotalTickets(item.getTotalTickets() + 1);
        }
        return mapa.values().stream()
                .sorted(Comparator.comparingLong(IndicadorMotivoItemDTO::getTotalTickets).reversed())
                .limit(15)
                .toList();
    }

    static IndicadoresPesquisaResumoDTO agregarPesquisa(List<TicketSatisfacao> avaliacoes) {
        IndicadoresPesquisaResumoDTO p = new IndicadoresPesquisaResumoDTO();
        long total = avaliacoes.size();
        p.setTotalPesquisas(total);
        long somaNota = 0;
        long comNota = 0;
        for (TicketSatisfacao s : avaliacoes) {
            TicketSatisfacaoStatus st = s.getStatus();
            if (st == null && s.getNota() != null) {
                st = TicketSatisfacaoStatus.REGISTRADA_MANUALMENTE;
            }
            if (st == TicketSatisfacaoStatus.PENDENTE) {
                p.setPendentes(p.getPendentes() + 1);
            } else if (st == TicketSatisfacaoStatus.RESPONDIDA) {
                p.setRespondidas(p.getRespondidas() + 1);
            } else if (st == TicketSatisfacaoStatus.EXPIRADA) {
                p.setExpiradas(p.getExpiradas() + 1);
            } else if (st == TicketSatisfacaoStatus.NAO_ENVIADA) {
                p.setNaoEnviadas(p.getNaoEnviadas() + 1);
            } else if (st == TicketSatisfacaoStatus.REGISTRADA_MANUALMENTE) {
                p.setRegistradasManualmente(p.getRegistradasManualmente() + 1);
            }
            if (contaParaMedia(st) && s.getNota() != null) {
                int nota = s.getNota();
                somaNota += nota;
                comNota++;
                switch (nota) {
                    case 1 -> p.setQuantidadeNota1(p.getQuantidadeNota1() + 1);
                    case 2 -> p.setQuantidadeNota2(p.getQuantidadeNota2() + 1);
                    case 3 -> p.setQuantidadeNota3(p.getQuantidadeNota3() + 1);
                    case 4 -> p.setQuantidadeNota4(p.getQuantidadeNota4() + 1);
                    case 5 -> p.setQuantidadeNota5(p.getQuantidadeNota5() + 1);
                    default -> { }
                }
            }
        }
        if (comNota > 0) {
            p.setMediaNota(Math.round((somaNota * 10.0) / comNota) / 10.0);
        } else {
            p.setMediaNota(null);
        }
        return p;
    }

    static boolean contaParaMedia(TicketSatisfacaoStatus status) {
        return status == TicketSatisfacaoStatus.RESPONDIDA
                || status == TicketSatisfacaoStatus.REGISTRADA_MANUALMENTE;
    }

    static IndicadoresEnvioResumoDTO agregarEnvio(List<TicketSatisfacao> avaliacoes) {
        IndicadoresEnvioResumoDTO e = new IndicadoresEnvioResumoDTO();
        for (TicketSatisfacao s : avaliacoes) {
            if (s.getStatus() != TicketSatisfacaoStatus.PENDENTE
                    && s.getStatus() != TicketSatisfacaoStatus.NAO_ENVIADA) {
                continue;
            }
            if (s.getEnvioStatus() == TicketSatisfacaoEnvioStatus.SIMULADO) {
                e.setSimuladas(e.getSimuladas() + 1);
            } else if (s.getEnvioStatus() == TicketSatisfacaoEnvioStatus.FALHA) {
                e.setFalhas(e.getFalhas() + 1);
            } else {
                e.setSemTentativa(e.getSemTentativa() + 1);
            }
        }
        return e;
    }
}

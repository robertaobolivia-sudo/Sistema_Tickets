package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketFiltroDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.SlaStatus;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoEnvioStatus;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.entity.TicketOrigem;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketBuscaService {

    private final TicketRepository ticketRepository;
    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;
    private final TicketService ticketService;

    @Transactional(readOnly = true)
    public List<TicketResponseDTO> buscar(TicketFiltroDTO filtro) {
        List<TicketResponseDTO> resultado;
        if (!possuiFiltros(filtro)) {
            resultado = aplicarFiltroSla(filtrarIndevidoPadrao(ticketService.listarTodos(), filtro), filtro);
        } else {
            Specification<Ticket> specification = montarSpecification(filtro);
            resultado = ticketRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "dataAbertura"))
                    .stream()
                    .map(ticketService::converterParaResponseSeguro)
                    .toList();
            resultado = aplicarFiltroSla(resultado, filtro);
        }
        return enriquecerComSatisfacao(resultado);
    }

    private List<TicketResponseDTO> enriquecerComSatisfacao(List<TicketResponseDTO> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return tickets;
        }
        List<Integer> ids = tickets.stream()
                .map(TicketResponseDTO::getId)
                .filter(Objects::nonNull)
                .map(Long::intValue)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return tickets;
        }
        Map<Integer, TicketSatisfacao> porTicket =
                TicketSatisfacaoResumoRelatorioHelper.indexarPorTicketId(
                        ticketSatisfacaoRepository.findByTicket_IdIn(ids));
        for (TicketResponseDTO dto : tickets) {
            if (dto.getId() == null) {
                continue;
            }
            TicketSatisfacao sat = porTicket.get(dto.getId().intValue());
            TicketSatisfacaoResumoRelatorioHelper.aplicarEmDto(dto, sat);
        }
        return tickets;
    }

    private List<TicketResponseDTO> aplicarFiltroSla(List<TicketResponseDTO> tickets, TicketFiltroDTO filtro) {
        if (filtro == null) {
            return tickets;
        }
        return tickets.stream()
                .filter(ticket -> correspondeFiltroSla(filtro.getSlaPrimeiroAtendimentoStatus(), ticket.getSlaPrimeiroAtendimentoStatus()))
                .filter(ticket -> correspondeFiltroSla(filtro.getSlaResolucaoStatus(), ticket.getSlaResolucaoStatus()))
                .filter(ticket -> correspondeFiltroEscalonado(filtro.getEscalonado(), ticket.getEscalonado()))
                .toList();
    }

    private boolean correspondeFiltroEscalonado(Boolean filtroEscalonado, Boolean ticketEscalonado) {
        if (filtroEscalonado == null) {
            return true;
        }
        return Boolean.TRUE.equals(ticketEscalonado) == filtroEscalonado;
    }

    private boolean correspondeFiltroSla(String filtroStatus, String ticketStatus) {
        if (!temTexto(filtroStatus) || "ALL".equalsIgnoreCase(filtroStatus.trim())) {
            return true;
        }
        if (!temTexto(ticketStatus)) {
            return SlaStatus.NAO_CALCULADO.name().equalsIgnoreCase(filtroStatus.trim());
        }
        return ticketStatus.equalsIgnoreCase(filtroStatus.trim());
    }

    private boolean possuiFiltros(TicketFiltroDTO filtro) {
        if (filtro == null) {
            return false;
        }
        return temTexto(filtro.getNumeroTicket())
                || temTexto(filtro.getCliente())
                || filtro.getClienteId() != null
                || temTexto(filtro.getStatus())
                || temTexto(filtro.getPrioridade())
                || filtro.getAnalistaId() != null
                || temTexto(filtro.getCanal())
                || temTexto(filtro.getGrupo())
                || temTexto(filtro.getSubgrupo())
                || filtro.getDataInicio() != null
                || filtro.getDataFim() != null
                || temTexto(filtro.getTextoLivre())
                || temFiltroSla(filtro.getSlaPrimeiroAtendimentoStatus())
                || temFiltroSla(filtro.getSlaResolucaoStatus())
                || filtro.getEscalonado() != null
                || filtro.getMotivoId() != null
                || temTexto(filtro.getStatusPesquisa())
                || filtro.getNotaAvaliacao() != null
                || temTexto(filtro.getEnvioStatus())
                || temTexto(filtro.getOrigemTicket());
    }

    private boolean temFiltroSla(String valor) {
        return temTexto(valor) && !"ALL".equalsIgnoreCase(valor.trim());
    }

    private Specification<Ticket> montarSpecification(TicketFiltroDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Object, Object> clienteJoin = root.join("cliente", JoinType.LEFT);
            Join<Object, Object> analistaJoin = root.join("analistaResponsavel", JoinType.LEFT);
            Join<Object, Object> grupoJoin = root.join("grupoCategoria", JoinType.LEFT);
            Join<Object, Object> subgrupoJoin = root.join("subgrupoCategoria", JoinType.LEFT);

            if (temTexto(filtro.getNumeroTicket())) {
                predicates.add(cb.like(
                        cb.lower(root.get("numeroTicket")),
                        "%" + filtro.getNumeroTicket().trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (filtro.getClienteId() != null) {
                predicates.add(cb.equal(clienteJoin.get("id"), filtro.getClienteId()));
            } else if (temTexto(filtro.getCliente())) {
                predicates.add(cb.like(
                        cb.lower(clienteJoin.get("nome")),
                        "%" + filtro.getCliente().trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (temTexto(filtro.getStatus()) && TicketStatus.isValido(filtro.getStatus())) {
                predicates.add(cb.equal(root.get("status"), TicketStatus.valueOf(filtro.getStatus())));
            } else if (!incluirIndevidoPorPadrao(filtro)) {
                predicates.add(cb.notEqual(root.get("status"), TicketStatus.INDEVIDO));
            }

            if (temTexto(filtro.getPrioridade()) && PrioridadeTicket.isValido(filtro.getPrioridade())) {
                predicates.add(cb.equal(root.get("prioridade"), PrioridadeTicket.valueOf(filtro.getPrioridade())));
            }

            if (temTexto(filtro.getOrigemTicket()) && TicketOrigem.isValido(filtro.getOrigemTicket())) {
                predicates.add(cb.equal(root.get("origemTicket"), TicketOrigem.valueOf(filtro.getOrigemTicket().trim())));
            }

            if (filtro.getAnalistaId() != null) {
                predicates.add(cb.equal(analistaJoin.get("id"), filtro.getAnalistaId()));
            }

            if (temTexto(filtro.getCanal())) {
                predicates.add(cb.like(
                        cb.lower(root.get("canal")),
                        "%" + filtro.getCanal().trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (temTexto(filtro.getGrupo())) {
                predicates.add(cb.like(
                        cb.lower(grupoJoin.get("nome")),
                        "%" + filtro.getGrupo().trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (temTexto(filtro.getSubgrupo())) {
                predicates.add(cb.like(
                        cb.lower(subgrupoJoin.get("nome")),
                        "%" + filtro.getSubgrupo().trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (filtro.getDataInicio() != null) {
                LocalDateTime inicio = filtro.getDataInicio().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataAbertura"), inicio));
            }

            if (filtro.getDataFim() != null) {
                LocalDateTime fim = filtro.getDataFim().atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("dataAbertura"), fim));
            }

            if (filtro.getEscalonado() != null) {
                predicates.add(cb.equal(root.get("escalonado"), filtro.getEscalonado()));
            }

            if (filtro.getMotivoId() != null) {
                Join<Object, Object> motivoJoin = root.join("motivo", JoinType.LEFT);
                predicates.add(cb.equal(motivoJoin.get("id"), filtro.getMotivoId()));
            }

            adicionarPredicadosSatisfacao(filtro, root, query, cb, predicates);

            if (temTexto(filtro.getTextoLivre())) {
                String termo = "%" + filtro.getTextoLivre().trim().toLowerCase(Locale.ROOT) + "%";
                Join<Object, Object> contatoJoin = root.join("contato", JoinType.LEFT);
                // mensagemInicial (@Lob/CLOB): lower() no criteria causa HTTP 500 no MySQL — fora do texto livre (Sprint 238)
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("numeroTicket")), termo),
                        cb.like(cb.lower(clienteJoin.get("nome")), termo),
                        cb.like(cb.lower(cb.coalesce(contatoJoin.get("nome"), "")), termo),
                        cb.like(cb.lower(cb.coalesce(contatoJoin.get("whatsapp"), "")), termo),
                        cb.like(cb.lower(cb.coalesce(grupoJoin.get("nome"), "")), termo),
                        cb.like(cb.lower(cb.coalesce(subgrupoJoin.get("nome"), "")), termo)
                ));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void adicionarPredicadosSatisfacao(
            TicketFiltroDTO filtro,
            Root<Ticket> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            List<Predicate> predicates) {
        if (temTexto(filtro.getStatusPesquisa())) {
            TicketSatisfacaoStatus statusSat =
                    TicketSatisfacaoStatus.valueOf(filtro.getStatusPesquisa().trim());
            predicates.add(existsSatisfacaoComStatus(root, query, cb, statusSat, null, null));
        }
        if (filtro.getNotaAvaliacao() != null) {
            predicates.add(existsSatisfacaoComStatus(root, query, cb, null, filtro.getNotaAvaliacao(), null));
        }
        if (temTexto(filtro.getEnvioStatus())) {
            TicketSatisfacaoEnvioStatus envio =
                    TicketSatisfacaoEnvioStatus.valueOf(filtro.getEnvioStatus().trim());
            predicates.add(existsSatisfacaoComStatus(root, query, cb, null, null, envio));
        }
    }

    private Predicate existsSatisfacaoComStatus(
            Root<Ticket> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            TicketSatisfacaoStatus status,
            Integer nota,
            TicketSatisfacaoEnvioStatus envioStatus) {
        Subquery<Integer> sub = query.subquery(Integer.class);
        Root<TicketSatisfacao> sat = sub.from(TicketSatisfacao.class);
        sub.select(sat.get("ticket").get("id"));
        List<Predicate> conds = new ArrayList<>();
        conds.add(cb.equal(sat.get("ticket").get("id"), root.get("id")));
        if (status != null) {
            conds.add(cb.equal(sat.get("status"), status));
        }
        if (nota != null) {
            conds.add(cb.equal(sat.get("nota"), nota));
        }
        if (envioStatus != null) {
            conds.add(cb.equal(sat.get("envioStatus"), envioStatus));
        }
        sub.where(conds.toArray(new Predicate[0]));
        return cb.exists(sub);
    }

    private static boolean incluirIndevidoPorPadrao(TicketFiltroDTO filtro) {
        return filtro != null
                && temTextoStatic(filtro.getStatus())
                && TicketStatus.INDEVIDO.name().equalsIgnoreCase(filtro.getStatus().trim());
    }

    private static boolean temTextoStatic(String valor) {
        return valor != null && !valor.isBlank();
    }

    private List<TicketResponseDTO> filtrarIndevidoPadrao(List<TicketResponseDTO> tickets, TicketFiltroDTO filtro) {
        if (tickets == null || tickets.isEmpty() || incluirIndevidoPorPadrao(filtro)) {
            return tickets;
        }
        return tickets.stream()
                .filter(t -> t.getStatus() == null || !TicketStatus.INDEVIDO.name().equalsIgnoreCase(t.getStatus()))
                .toList();
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

}

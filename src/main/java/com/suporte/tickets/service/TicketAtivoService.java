package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.ContatoCliente;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.ContatoClienteRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketAtivoService {

    public static final EnumSet<TicketStatus> STATUS_ATIVOS = EnumSet.of(
            TicketStatus.ABERTO,
            TicketStatus.EM_ATENDIMENTO,
            TicketStatus.AGUARDANDO_CLIENTE
    );

    public static final EnumSet<TicketStatus> STATUS_ENCERRADOS = EnumSet.of(
            TicketStatus.RESOLVIDO,
            TicketStatus.CANCELADO,
            TicketStatus.INDEVIDO
    );

    /** Terminais que não entram em fila operacional nem em SLA de atendimento. */
    public static final EnumSet<TicketStatus> STATUS_FORA_ATENDIMENTO_OPERACIONAL = EnumSet.of(
            TicketStatus.RESOLVIDO,
            TicketStatus.CANCELADO,
            TicketStatus.INDEVIDO
    );

    private static final List<TicketStatus> LISTA_STATUS_ATIVOS = List.copyOf(STATUS_ATIVOS);

    public static final String CONSULTA_ATIVO_MODO_CLIENTE_CONTATO = "CLIENTE_CONTATO";
    public static final String CONSULTA_ATIVO_MODO_LEGADO = "LEGADO";
    public static final String LEGADO_MOTIVO_CLIENTE_SEM_CONTATO_WHATSAPP = "CLIENTE_ID_SEM_CONTATO_WHATSAPP";

    private final TicketRepository ticketRepository;
    private final ClienteRepository clienteRepository;
    private final ContatoClienteRepository contatoClienteRepository;
    private final TicketService ticketService;

    @Transactional(readOnly = true)
    public Optional<TicketResponseDTO> buscarTicketAtivo(Integer clienteId, Integer contatoSolicitanteId, String telefone) {
        return buscarTicketAtivo(clienteId, null, contatoSolicitanteId, telefone);
    }

    @Transactional(readOnly = true)
    public Optional<TicketResponseDTO> buscarTicketAtivo(
            Integer clienteId,
            Integer contatoWhatsappId,
            Integer contatoSolicitanteId,
            String telefone) {
        Optional<Ticket> ticket;
        String consultaModo;
        String legadoMotivo = null;
        if (clienteId != null && contatoWhatsappId != null) {
            ticket = buscarEntidadeAtivaAtendimentoWhatsapp(
                    clienteId, contatoWhatsappId, contatoSolicitanteId, telefone);
            consultaModo = CONSULTA_ATIVO_MODO_CLIENTE_CONTATO;
        } else {
            ticket = buscarEntidadeAtiva(clienteId, contatoSolicitanteId, telefone);
            consultaModo = CONSULTA_ATIVO_MODO_LEGADO;
            if (clienteId != null && contatoWhatsappId == null) {
                legadoMotivo = LEGADO_MOTIVO_CLIENTE_SEM_CONTATO_WHATSAPP;
            }
        }
        final String modoResposta = consultaModo;
        final String motivoLegadoResposta = legadoMotivo;
        return ticket.map(t -> enriquecerRespostaConsultaAtivo(
                ticketService.converterParaResponseSeguro(t), modoResposta, motivoLegadoResposta));
    }

    static TicketResponseDTO enriquecerRespostaConsultaAtivo(
            TicketResponseDTO dto,
            String consultaModo,
            String legadoMotivo) {
        dto.setConsultaAtivoModo(consultaModo);
        if (CONSULTA_ATIVO_MODO_LEGADO.equals(consultaModo)) {
            dto.setConsultaAtivoLegadoDeprecated(true);
            dto.setConsultaAtivoLegadoMotivo(legadoMotivo);
        } else {
            dto.setConsultaAtivoLegadoDeprecated(false);
            dto.setConsultaAtivoLegadoMotivo(null);
        }
        return dto;
    }

    public static boolean isConsultaAtivoLegadoPorClienteSemContato(Integer clienteId, Integer contatoWhatsappId) {
        return clienteId != null && contatoWhatsappId == null;
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> buscarEntidadeAtiva(Integer clienteId, Integer contatoSolicitanteId, String telefone) {
        return buscarEntidadeAtiva(clienteId, null, contatoSolicitanteId, telefone);
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> buscarEntidadeAtiva(
            Integer clienteId,
            Integer contatoWhatsappId,
            Integer contatoSolicitanteId,
            String telefone) {
        if (contatoWhatsappId == null && contatoSolicitanteId == null && clienteId == null && !temTelefone(telefone)) {
            throw new IllegalArgumentException(
                    "Informe telefone, clienteId ou contatoSolicitanteId para buscar ticket ativo.");
        }

        // Sprint 206: com Contato WhatsApp resolvido, nunca reaproveitar ticket ativo de outro Contato do mesmo Cliente.
        if (clienteId != null && contatoWhatsappId != null) {
            return ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                    clienteId,
                    contatoWhatsappId,
                    LISTA_STATUS_ATIVOS);
        }

        if (contatoSolicitanteId != null) {
            Optional<Ticket> porContato = ticketRepository
                    .findFirstByContatoSolicitante_IdAndStatusInOrderByDataAberturaDesc(
                            contatoSolicitanteId,
                            LISTA_STATUS_ATIVOS);
            if (porContato.isPresent()) {
                return porContato;
            }
        }

        if (clienteId != null) {
            Optional<Ticket> porCliente = ticketRepository
                    .findFirstByCliente_IdAndStatusInOrderByDataAberturaDesc(
                            clienteId,
                            LISTA_STATUS_ATIVOS);
            if (porCliente.isPresent()) {
                return porCliente;
            }
        }

        if (temTelefone(telefone)) {
            String telefoneNorm = normalizarTelefone(telefone);
            Set<Integer> clienteIds = resolverClienteIdsPorTelefone(telefoneNorm);
            if (!clienteIds.isEmpty()) {
                List<Ticket> candidatos = ticketRepository.findByCliente_IdInAndStatusInOrderByDataAberturaDesc(
                        new ArrayList<>(clienteIds),
                        LISTA_STATUS_ATIVOS,
                        PageRequest.of(0, 1));
                if (!candidatos.isEmpty()) {
                    return Optional.of(candidatos.get(0));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Sprint F1 (C02/C03): fluxo WhatsApp / Contato resolvido — não reutiliza ticket ativo só por {@code clienteId}.
     * Com par Cliente+Contato, busca apenas esse par; com {@code clienteId} sem {@code contatoWhatsappId}, retorna vazio
     * (evita anexar mensagem ao ticket de outro Contato do mesmo contratante).
     * Sem {@code clienteId}, delega à busca legado ({@link #buscarEntidadeAtiva}).
     */
    @Transactional(readOnly = true)
    public Optional<Ticket> buscarEntidadeAtivaAtendimentoWhatsapp(
            Integer clienteId,
            Integer contatoWhatsappId,
            Integer contatoSolicitanteId,
            String telefone) {
        if (clienteId != null && contatoWhatsappId != null) {
            return ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                    clienteId,
                    contatoWhatsappId,
                    LISTA_STATUS_ATIVOS);
        }
        if (clienteId != null) {
            return Optional.empty();
        }
        return buscarEntidadeAtiva(clienteId, contatoWhatsappId, contatoSolicitanteId, telefone);
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> buscarUltimoEncerradoPorClienteEContato(Integer clienteId, Integer contatoWhatsappId) {
        if (clienteId == null || contatoWhatsappId == null) {
            return Optional.empty();
        }
        List<Ticket> encerrados = ticketRepository.findUltimoEncerradoPorClienteEContato(
                clienteId,
                contatoWhatsappId,
                List.copyOf(STATUS_ENCERRADOS),
                PageRequest.of(0, 1));
        return encerrados.isEmpty() ? Optional.empty() : Optional.of(encerrados.get(0));
    }

    public static boolean isStatusAtivo(TicketStatus status) {
        return status != null && STATUS_ATIVOS.contains(status);
    }

    /** Métricas gerenciais/operacionais — exclui terminais fora do atendimento (Sprint 277). */
    public static boolean isAtendimentoOperacionalValido(TicketStatus status) {
        return status != null && !STATUS_FORA_ATENDIMENTO_OPERACIONAL.contains(status);
    }

    public static boolean isTicketIndevido(TicketStatus status) {
        return status == TicketStatus.INDEVIDO;
    }

    public static String normalizarTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }
        String apenasDigitos = telefone.replaceAll("\\D", "");
        return apenasDigitos.isEmpty() ? null : apenasDigitos;
    }

    private Set<Integer> resolverClienteIdsPorTelefone(String telefoneNorm) {
        Set<Integer> ids = new LinkedHashSet<>();
        if (telefoneNorm == null) {
            return ids;
        }
        clienteRepository.findByTelefone(telefoneNorm).ifPresent(c -> ids.add(c.getId()));
        clienteRepository.findByTelefoneContato(telefoneNorm).ifPresent(c -> ids.add(c.getId()));
        for (ContatoCliente contato : contatoClienteRepository.findByTelefoneOuCelular(telefoneNorm)) {
            if (contato.getCliente() != null && contato.getCliente().getId() != null) {
                ids.add(contato.getCliente().getId());
            }
        }
        return ids;
    }

    private static boolean temTelefone(String telefone) {
        return normalizarTelefone(telefone) != null;
    }
}

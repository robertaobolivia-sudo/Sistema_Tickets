package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

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
    public static final String CONSULTA_ATIVO_MODO_REJEITADO = "REJEITADO";
    public static final String LEGADO_MOTIVO_CLIENTE_SEM_CONTATO_WHATSAPP = "CLIENTE_ID_SEM_CONTATO_WHATSAPP";
    public static final String REJEITADO_MOTIVO_CLIENTE_SEM_CONTATO_F6 = "CLIENTE_ID_SEM_CONTATO_WHATSAPP_F6";
    public static final String REJEITADO_MOTIVO_SEM_PAR_CLIENTE_CONTATO_F7 = "EXIGE_CLIENTE_ID_E_CONTATO_WHATSAPP_F7";

    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    @Transactional(readOnly = true)
    public Optional<TicketResponseDTO> buscarTicketAtivo(Integer clienteId, Integer contatoWhatsappId, String telefone) {
        Optional<Ticket> ticket;
        String consultaModo;
        String legadoMotivo = null;
        if (clienteId != null && contatoWhatsappId != null) {
            ticket = buscarEntidadeAtivaAtendimentoWhatsapp(clienteId, contatoWhatsappId, telefone);
            consultaModo = CONSULTA_ATIVO_MODO_CLIENTE_CONTATO;
        } else {
            // Sprint F6/F7: sem par Cliente+Contato não decide ticket ativo operacional.
            ticket = Optional.empty();
            consultaModo = CONSULTA_ATIVO_MODO_REJEITADO;
            if (isConsultaAtivoLegadoPorClienteSemContato(clienteId, contatoWhatsappId)
                    && !temTelefone(telefone)) {
                legadoMotivo = REJEITADO_MOTIVO_CLIENTE_SEM_CONTATO_F6;
            } else {
                legadoMotivo = REJEITADO_MOTIVO_SEM_PAR_CLIENTE_CONTATO_F7;
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
        if (CONSULTA_ATIVO_MODO_REJEITADO.equals(consultaModo)) {
            dto.setConsultaAtivoLegadoDeprecated(true);
            dto.setConsultaAtivoLegadoMotivo(legadoMotivo);
        } else if (CONSULTA_ATIVO_MODO_LEGADO.equals(consultaModo)) {
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
    public Optional<Ticket> buscarEntidadeAtiva(Integer clienteId, Integer contatoWhatsappId, String telefone) {
        if (clienteId != null && contatoWhatsappId != null) {
            return ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                    clienteId,
                    contatoWhatsappId,
                    LISTA_STATUS_ATIVOS);
        }
        return Optional.empty();
    }

    /**
     * Sprint F1/F7: fluxo WhatsApp — apenas par Cliente+Contato; demais entradas retornam vazio.
     */
    @Transactional(readOnly = true)
    public Optional<Ticket> buscarEntidadeAtivaAtendimentoWhatsapp(
            Integer clienteId,
            Integer contatoWhatsappId,
            String telefone) {
        return buscarEntidadeAtiva(clienteId, contatoWhatsappId, telefone);
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

    private static boolean temTelefone(String telefone) {
        return normalizarTelefone(telefone) != null;
    }
}

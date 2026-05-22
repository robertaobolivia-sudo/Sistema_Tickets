package com.suporte.tickets.service;

import com.suporte.tickets.dto.InteracaoPendenteDecisaoResponseDTO;
import com.suporte.tickets.dto.InteracaoPendenteDecisaoResultadoDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.InteracaoPendenteDecisao;
import com.suporte.tickets.entity.InteracaoPendenteDecisaoStatus;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.WhatsappMatriz;
import com.suporte.tickets.repository.InteracaoPendenteDecisaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteracaoPendenteDecisaoService {

    private final InteracaoPendenteDecisaoRepository pendenteRepository;
    private final TicketInteracaoService ticketInteracaoService;
    private final TicketService ticketService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<InteracaoPendenteDecisaoResponseDTO> listarPendentes() {
        return pendenteRepository
                .findByStatusOrderByCriadaEmDesc(InteracaoPendenteDecisaoStatus.PENDENTE)
                .stream()
                .map(InteracaoPendenteDecisaoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public InteracaoPendenteDecisao criarPendencia(
            Cliente cliente,
            Contato contato,
            Ticket ticketAnterior,
            WhatsappMatriz whatsappMatriz,
            String mensagem,
            String canal,
            String origemExternaId) {
        if (cliente == null || contato == null || ticketAnterior == null) {
            throw new IllegalArgumentException("Cliente, contato e ticket anterior sao obrigatorios.");
        }
        if (pendenteRepository.existsByContato_IdAndStatus(
                contato.getId(), InteracaoPendenteDecisaoStatus.PENDENTE)) {
            throw new IllegalStateException(
                    "Ja existe pendencia de decisao aberta para este contato.");
        }
        InteracaoPendenteDecisao p = new InteracaoPendenteDecisao();
        p.setCliente(cliente);
        p.setContato(contato);
        p.setTicketAnterior(ticketAnterior);
        p.setWhatsappMatriz(whatsappMatriz);
        p.setMensagem(mensagem);
        p.setCanal(canal);
        p.setOrigemExternaId(origemExternaId);
        p.setStatus(InteracaoPendenteDecisaoStatus.PENDENTE);
        return pendenteRepository.save(p);
    }

    @Transactional
    public InteracaoPendenteDecisaoResultadoDTO vincularAoTicketAnterior(Long pendenciaId, Long analistaId) {
        InteracaoPendenteDecisao p = buscarPendente(pendenciaId);
        Ticket anterior = p.getTicketAnterior();
        ticketInteracaoService.registrarMensagemEntradaExterna(
                anterior,
                p.getMensagem(),
                p.getOrigemExternaId());
        p.setStatus(InteracaoPendenteDecisaoStatus.VINCULADA_ANTERIOR);
        p.setDecididaEm(LocalDateTime.now());
        p.setDecididaPorAnalistaId(analistaId);
        pendenteRepository.save(p);
        auditoriaService.registrar(
                AuditoriaService.ACAO_INTERACAO_PENDENTE_VINCULAR,
                AuditoriaService.ENTIDADE_TICKET,
                anterior.getNumeroTicket(),
                "Mensagem pos-encerramento vinculada ao ticket anterior (pendencia " + pendenciaId + ")",
                analistaId);
        return new InteracaoPendenteDecisaoResultadoDTO(
                p.getId(),
                p.getStatus().name(),
                anterior.getNumeroTicket(),
                anterior.getNumeroTicket(),
                false);
    }

    @Transactional
    public InteracaoPendenteDecisaoResultadoDTO gerarNovoTicket(Long pendenciaId, Long analistaId) {
        InteracaoPendenteDecisao p = buscarPendente(pendenciaId);
        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(p.getCliente().getId());
        dto.setCliente(p.getCliente().getNome());
        dto.setConexao(p.getCliente().getNome());
        dto.setTelefone(p.getContato().getWhatsapp());
        dto.setNomeContato(p.getContato().getNome());
        dto.setMensagem(p.getMensagem());
        dto.setCanal(p.getCanal() != null ? p.getCanal() : "WHATSAPP");
        if (p.getWhatsappMatriz() != null) {
            dto.setWhatsappMatrizId(p.getWhatsappMatriz().getId());
        }
        TicketResponseDTO criado = ticketService.criarTicketPorWebhook(dto);
        Ticket novo = ticketService.buscarEntidadePorNumeroOuFalha(criado.getNumeroTicket());
        p.setTicketGerado(novo);
        p.setStatus(InteracaoPendenteDecisaoStatus.NOVO_TICKET);
        p.setDecididaEm(LocalDateTime.now());
        p.setDecididaPorAnalistaId(analistaId);
        pendenteRepository.save(p);
        auditoriaService.registrar(
                AuditoriaService.ACAO_INTERACAO_PENDENTE_NOVO_TICKET,
                AuditoriaService.ENTIDADE_TICKET,
                criado.getNumeroTicket(),
                "Novo ticket gerado apos encerramento (pendencia " + pendenciaId + ")",
                analistaId);
        return new InteracaoPendenteDecisaoResultadoDTO(
                p.getId(),
                p.getStatus().name(),
                p.getTicketAnterior().getNumeroTicket(),
                criado.getNumeroTicket(),
                true);
    }

    private InteracaoPendenteDecisao buscarPendente(Long pendenciaId) {
        if (pendenciaId == null) {
            throw new IllegalArgumentException("Pendencia invalida.");
        }
        return pendenteRepository
                .findByIdAndStatus(pendenciaId, InteracaoPendenteDecisaoStatus.PENDENTE)
                .orElseThrow(() -> new IllegalStateException(
                        "Pendencia nao encontrada ou ja decidida: " + pendenciaId));
    }
}

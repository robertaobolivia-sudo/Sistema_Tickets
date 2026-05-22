package com.suporte.tickets.service;

import com.suporte.tickets.dto.IntegracaoMensagemEntradaResponseDTO;
import com.suporte.tickets.dto.IntegracaoWhatsappMensagemRequestDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.InteracaoPendenteDecisao;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.entity.WhatsappMatriz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Entrada preparatória de mensagens externas (WhatsApp/API), sem provedor real.
 */
@Service
@RequiredArgsConstructor
public class IntegracaoMensagemEntradaService {

    private final TicketAtivoService ticketAtivoService;
    private final TicketService ticketService;
    private final TicketInteracaoService ticketInteracaoService;
    private final WhatsappMatrizService whatsappMatrizService;
    private final ContatoService contatoService;
    private final InteracaoPendenteDecisaoService interacaoPendenteDecisaoService;

    @Transactional
    public IntegracaoMensagemEntradaResponseDTO processarMensagemWhatsapp(IntegracaoWhatsappMensagemRequestDTO request) {
        String telefoneNorm = TicketAtivoService.normalizarTelefone(request.getTelefone());
        if (telefoneNorm == null) {
            throw new IllegalArgumentException("Telefone invalido");
        }
        String mensagem = request.getMensagem() != null ? request.getMensagem().trim() : "";
        if (mensagem.isEmpty()) {
            throw new IllegalArgumentException("Mensagem e obrigatoria");
        }

        ResolucaoMatriz resolucao = resolverMatrizOuLegado(request);

        Integer clienteIdBusca = resolucao.clienteContratante != null
                ? resolucao.clienteContratante.getId()
                : request.getClienteId();

        Contato contatoWhatsapp = resolverContatoWhatsapp(clienteIdBusca, telefoneNorm, request);

        Integer contatoWhatsappId = contatoWhatsapp != null ? contatoWhatsapp.getId() : null;

        Optional<Ticket> ticketAtivo = ticketAtivoService.buscarEntidadeAtiva(
                clienteIdBusca,
                contatoWhatsappId,
                request.getContatoSolicitanteId(),
                telefoneNorm);

        if (ticketAtivo.isPresent()) {
            Ticket ticket = ticketAtivo.get();
            ticketInteracaoService.registrarMensagemEntradaExterna(
                    ticket,
                    mensagem,
                    request.getOrigemExternaId());
            return montarResposta(false, ticket.getNumeroTicket(), ticket.getStatus(), true, false, null);
        }

        if (clienteIdBusca != null && contatoWhatsapp != null) {
            Optional<Ticket> ultimoEncerrado = ticketAtivoService.buscarUltimoEncerradoPorClienteEContato(
                    clienteIdBusca,
                    contatoWhatsapp.getId());
            if (ultimoEncerrado.isPresent()) {
                InteracaoPendenteDecisao pendencia = interacaoPendenteDecisaoService.criarPendencia(
                        ultimoEncerrado.get().getCliente(),
                        contatoWhatsapp,
                        ultimoEncerrado.get(),
                        resolucao.whatsappMatriz,
                        mensagem,
                        resolverCanal(request.getCanal()),
                        request.getOrigemExternaId());
                return montarResposta(
                        false,
                        ultimoEncerrado.get().getNumeroTicket(),
                        ultimoEncerrado.get().getStatus(),
                        false,
                        true,
                        pendencia.getId());
            }
        }

        if (clienteIdBusca != null && contatoWhatsapp != null) {
            Optional<Ticket> recheckAtivo = ticketAtivoService.buscarEntidadeAtiva(
                    clienteIdBusca,
                    contatoWhatsapp.getId(),
                    request.getContatoSolicitanteId(),
                    telefoneNorm);
            if (recheckAtivo.isPresent()) {
                Ticket ticket = recheckAtivo.get();
                ticketInteracaoService.registrarMensagemEntradaExterna(
                        ticket,
                        mensagem,
                        request.getOrigemExternaId());
                return montarResposta(false, ticket.getNumeroTicket(), ticket.getStatus(), true, false, null);
            }
        }

        TicketWebhookRequestDTO webhook = new TicketWebhookRequestDTO();
        if (resolucao.clienteContratante != null) {
            webhook.setClienteContratanteId(resolucao.clienteContratante.getId());
            webhook.setCliente(resolucao.clienteContratante.getNome());
            webhook.setConexao(resolucao.clienteContratante.getNome());
        } else {
            webhook.setCliente(resolverNomeClienteLegado(request));
        }
        webhook.setNomeContato(request.getNomeContato());
        webhook.setTelefone(telefoneNorm);
        webhook.setMensagem(mensagem);
        webhook.setCanal(resolverCanal(request.getCanal()));
        webhook.setContatoSolicitanteId(request.getContatoSolicitanteId());
        if (resolucao.whatsappMatriz != null) {
            webhook.setWhatsappMatrizId(resolucao.whatsappMatriz.getId());
        }

        TicketResponseDTO criado = ticketService.criarTicketPorWebhook(webhook);
        return montarResposta(
                true,
                criado.getNumeroTicket(),
                parseStatusResposta(criado.getStatus()),
                true,
                false,
                null);
    }

    private Contato resolverContatoWhatsapp(
            Integer clienteId,
            String telefoneNorm,
            IntegracaoWhatsappMensagemRequestDTO request) {
        if (clienteId == null) {
            return null;
        }
        String nomeContato = request.getNomeContato() != null && !request.getNomeContato().isBlank()
                ? request.getNomeContato().trim()
                : resolverNomeClienteLegado(request);
        ContatoResponseDTO resp = contatoService.criarSeNaoExistir(
                clienteId,
                telefoneNorm,
                nomeContato);
        return contatoService.buscarEntidade(resp.getId());
    }

    private ResolucaoMatriz resolverMatrizOuLegado(IntegracaoWhatsappMensagemRequestDTO request) {
        if (request.getWhatsappMatrizId() != null) {
            WhatsappMatriz matriz = whatsappMatrizService.buscarEntidadeAtivaPorId(request.getWhatsappMatrizId());
            return new ResolucaoMatriz(matriz.getCliente(), matriz);
        }
        if (request.getNumeroMatriz() != null && !request.getNumeroMatriz().isBlank()) {
            WhatsappMatriz matriz = whatsappMatrizService.resolverMatrizAtivaPorNumero(request.getNumeroMatriz());
            return new ResolucaoMatriz(matriz.getCliente(), matriz);
        }
        return new ResolucaoMatriz(null, null);
    }

    private static final class ResolucaoMatriz {
        final Cliente clienteContratante;
        final WhatsappMatriz whatsappMatriz;

        ResolucaoMatriz(Cliente clienteContratante, WhatsappMatriz whatsappMatriz) {
            this.clienteContratante = clienteContratante;
            this.whatsappMatriz = whatsappMatriz;
        }
    }

    private static TicketStatus parseStatusResposta(String status) {
        if (status != null && TicketStatus.isValido(status)) {
            return TicketStatus.valueOf(status.trim().toUpperCase());
        }
        return TicketStatus.ABERTO;
    }

    private static String resolverNomeClienteLegado(IntegracaoWhatsappMensagemRequestDTO request) {
        if (request.getNomeContato() != null && !request.getNomeContato().isBlank()) {
            return request.getNomeContato().trim();
        }
        return "Contato WhatsApp";
    }

    private static String resolverCanal(String canal) {
        if (canal != null && !canal.isBlank()) {
            return canal.trim();
        }
        return "WHATSAPP";
    }

    private static IntegracaoMensagemEntradaResponseDTO montarResposta(
            boolean ticketCriado,
            String numeroTicket,
            TicketStatus status,
            boolean mensagemRegistrada,
            boolean aguardandoDecisao,
            Long pendenciaDecisaoId) {
        IntegracaoMensagemEntradaResponseDTO dto = new IntegracaoMensagemEntradaResponseDTO();
        dto.setTicketCriado(ticketCriado);
        dto.setNumeroTicket(numeroTicket);
        dto.setStatus(status != null ? status.name() : null);
        dto.setMensagemRegistrada(mensagemRegistrada);
        dto.setAguardandoDecisao(aguardandoDecisao);
        dto.setPendenciaDecisaoId(pendenciaDecisaoId);
        return dto;
    }
}

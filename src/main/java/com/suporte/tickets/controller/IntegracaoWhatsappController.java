package com.suporte.tickets.controller;

import com.suporte.tickets.dto.IntegracaoMensagemEntradaResponseDTO;
import com.suporte.tickets.dto.IntegracaoWhatsappMensagemRequestDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.service.AuditoriaService;
import com.suporte.tickets.service.IntegracaoMensagemEntradaService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints preparatórios para integração WhatsApp/API (sem provedor real nesta sprint).
 */
@RestController
@RequestMapping("/api/integracoes/whatsapp")
@RequiredArgsConstructor
public class IntegracaoWhatsappController {

    public static final String ACAO_INTEGRACAO_MENSAGEM_ENTRADA = "INTEGRACAO_WHATSAPP_MENSAGEM";

    private final IntegracaoMensagemEntradaService integracaoMensagemEntradaService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;
    private final AuditoriaService auditoriaService;

    @PostMapping("/mensagens")
    public ResponseEntity<IntegracaoMensagemEntradaResponseDTO> receberMensagem(
            @Valid @RequestBody IntegracaoWhatsappMensagemRequestDTO request,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        IntegracaoMensagemEntradaResponseDTO resposta =
                integracaoMensagemEntradaService.processarMensagemWhatsapp(request);
        auditoriaService.registrar(
                ACAO_INTEGRACAO_MENSAGEM_ENTRADA,
                AuditoriaService.ENTIDADE_TICKET,
                resposta.getNumeroTicket(),
                resposta.isTicketCriado()
                        ? "Ticket criado por entrada WhatsApp preparatoria"
                        : "Mensagem anexada a ticket ativo (entrada WhatsApp preparatoria)",
                executor);
        HttpStatus status = resposta.isTicketCriado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resposta);
    }
}

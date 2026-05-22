package com.suporte.tickets.controller;

import com.suporte.tickets.dto.ChatsHistoricoResumoDTO;
import com.suporte.tickets.dto.InteracaoPendenteDecisaoResponseDTO;
import com.suporte.tickets.dto.InteracaoPendenteDecisaoResultadoDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.service.ChatsHistoricoResumoService;
import com.suporte.tickets.service.InteracaoPendenteDecisaoService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatsController {

    private final ChatsHistoricoResumoService chatsHistoricoResumoService;
    private final InteracaoPendenteDecisaoService interacaoPendenteDecisaoService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    /**
     * Histórico resumido do cliente para o painel Chats (leve, sem ticket atual).
     */
    @GetMapping("/{numeroTicket}/historico-resumido")
    public ResponseEntity<ChatsHistoricoResumoDTO> historicoResumido(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(chatsHistoricoResumoService.buscarPorNumeroTicket(numeroTicket));
    }

    @GetMapping("/interacoes-pendentes")
    public ResponseEntity<List<InteracaoPendenteDecisaoResponseDTO>> listarInteracoesPendentes(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(interacaoPendenteDecisaoService.listarPendentes());
    }

    @PostMapping("/interacoes-pendentes/{id}/vincular-anterior")
    public ResponseEntity<InteracaoPendenteDecisaoResultadoDTO> vincularInteracaoAoTicketAnterior(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(
                interacaoPendenteDecisaoService.vincularAoTicketAnterior(id, executor.getId()));
    }

    @PostMapping("/interacoes-pendentes/{id}/gerar-ticket")
    public ResponseEntity<InteracaoPendenteDecisaoResultadoDTO> gerarTicketAPartirDaPendencia(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(interacaoPendenteDecisaoService.gerarNovoTicket(id, executor.getId()));
    }
}

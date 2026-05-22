package com.suporte.tickets.controller;

import com.suporte.tickets.dto.NotificacaoContadorDTO;
import com.suporte.tickets.dto.NotificacaoInternaDTO;
import com.suporte.tickets.dto.NotificacaoSlaVerificacaoResultadoDTO;
import com.suporte.tickets.service.NotificacaoInternaService;
import com.suporte.tickets.service.NotificacaoSlaService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoInternaService notificacaoInternaService;
    private final NotificacaoSlaService notificacaoSlaService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @GetMapping
    public ResponseEntity<List<NotificacaoInternaDTO>> listar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(notificacaoInternaService.listarRecentes());
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<List<NotificacaoInternaDTO>> listarNaoLidas(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(notificacaoInternaService.listarNaoLidas());
    }

    @GetMapping("/contador-nao-lidas")
    public ResponseEntity<NotificacaoContadorDTO> contadorNaoLidas(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(notificacaoInternaService.contarNaoLidas());
    }

    @PutMapping("/{id}/marcar-lida")
    public ResponseEntity<NotificacaoInternaDTO> marcarLida(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(notificacaoInternaService.marcarComoLida(id));
    }

    @PutMapping("/marcar-todas-lidas")
    public ResponseEntity<Map<String, Long>> marcarTodasLidas(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        long atualizadas = notificacaoInternaService.marcarTodasComoLidas();
        return ResponseEntity.ok(Map.of("atualizadas", atualizadas));
    }

    @PostMapping("/sla/verificar")
    public ResponseEntity<NotificacaoSlaVerificacaoResultadoDTO> verificarSlaCritico(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(notificacaoSlaService.verificarTicketsCriticosSla());
    }
}

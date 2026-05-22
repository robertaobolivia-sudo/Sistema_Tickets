package com.suporte.tickets.controller;

import com.suporte.tickets.dto.SlaMetaDTO;
import com.suporte.tickets.dto.SlaMetaSeedResultadoDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.service.AuditoriaService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.SlaMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sla-metas")
@RequiredArgsConstructor
public class SlaMetaController {

    private final SlaMetaService slaMetaService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<SlaMetaDTO>> listar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(slaMetaService.listarTodos());
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<SlaMetaDTO>> listarAtivas(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(slaMetaService.listarAtivas());
    }

    @GetMapping("/prioridade/{prioridade}")
    public ResponseEntity<SlaMetaDTO> obterPorPrioridade(
            @PathVariable String prioridade,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(slaMetaService.obterPorPrioridade(prioridade));
    }

    @PutMapping("/prioridade/{prioridade}")
    public ResponseEntity<SlaMetaDTO> atualizarPorPrioridade(
            @PathVariable String prioridade,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestBody SlaMetaDTO dto) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        Analista executor = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        SlaMetaDTO atualizado = slaMetaService.atualizarPorPrioridade(prioridade, dto);
        auditoriaService.registrar(
                AuditoriaService.ACAO_CONFIG_SLA_META,
                AuditoriaService.ENTIDADE_CONFIG,
                prioridade,
                "Meta SLA atualizada: " + prioridade,
                executor);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/seed-default")
    public ResponseEntity<SlaMetaSeedResultadoDTO> seedDefault(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(slaMetaService.seedDefault());
    }
}

package com.suporte.tickets.controller;

import com.suporte.tickets.dto.FeriadoDTO;
import com.suporte.tickets.dto.FeriadoSeedResultadoDTO;
import com.suporte.tickets.dto.FeriadoVerificacaoDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.service.AuditoriaService;
import com.suporte.tickets.service.FeriadoService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feriados")
@RequiredArgsConstructor
public class FeriadoController {

    private final FeriadoService feriadoService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<FeriadoDTO>> listar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(feriadoService.listarTodos());
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<FeriadoDTO>> listarAtivos(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(feriadoService.listarAtivos());
    }

    @GetMapping("/verificar")
    public ResponseEntity<FeriadoVerificacaoDTO> verificar(
            @RequestParam String data,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(feriadoService.verificar(data));
    }

    @PostMapping
    public ResponseEntity<FeriadoDTO> criar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestBody FeriadoDTO dto) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        Analista executor = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        FeriadoDTO criado = feriadoService.criar(dto);
        auditoriaService.registrar(
                AuditoriaService.ACAO_CONFIG_FERIADO,
                AuditoriaService.ENTIDADE_CONFIG,
                String.valueOf(criado.getId()),
                "Feriado criado: " + criado.getData(),
                executor);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeriadoDTO> atualizar(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestBody FeriadoDTO dto) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        Analista executor = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        FeriadoDTO atualizado = feriadoService.atualizar(id, dto);
        auditoriaService.registrar(
                AuditoriaService.ACAO_CONFIG_FERIADO,
                AuditoriaService.ENTIDADE_CONFIG,
                String.valueOf(id),
                "Feriado atualizado",
                executor);
        return ResponseEntity.ok(atualizado);
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<FeriadoDTO> ativar(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        Analista executor = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        FeriadoDTO atualizado = feriadoService.ativar(id);
        auditoriaService.registrar(
                AuditoriaService.ACAO_CONFIG_FERIADO,
                AuditoriaService.ENTIDADE_CONFIG,
                String.valueOf(id),
                "Feriado ativado",
                executor);
        return ResponseEntity.ok(atualizado);
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<FeriadoDTO> inativar(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        Analista executor = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        FeriadoDTO atualizado = feriadoService.inativar(id);
        auditoriaService.registrar(
                AuditoriaService.ACAO_CONFIG_FERIADO,
                AuditoriaService.ENTIDADE_CONFIG,
                String.valueOf(id),
                "Feriado inativado",
                executor);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/seed/2026-sao-paulo")
    public ResponseEntity<FeriadoSeedResultadoDTO> seedSaoPaulo2026(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(feriadoService.seedSaoPaulo2026());
    }
}

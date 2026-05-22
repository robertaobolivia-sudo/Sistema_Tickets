package com.suporte.tickets.controller;

import com.suporte.tickets.dto.SubgrupoCategoriaRequestDTO;
import com.suporte.tickets.dto.SubgrupoCategoriaResponseDTO;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.SubgrupoCategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/subgrupos-categoria")
@RequiredArgsConstructor
public class SubgrupoCategoriaController {

    private final SubgrupoCategoriaService subgrupoCategoriaService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @PostMapping
    public ResponseEntity<SubgrupoCategoriaResponseDTO> criar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody SubgrupoCategoriaRequestDTO dto) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(subgrupoCategoriaService.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<SubgrupoCategoriaResponseDTO>> listar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(subgrupoCategoriaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubgrupoCategoriaResponseDTO> buscarPorId(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(subgrupoCategoriaService.buscarPorId(id));
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<SubgrupoCategoriaResponseDTO>> listarPorGrupo(
            @PathVariable Long grupoId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(subgrupoCategoriaService.listarPorGrupo(grupoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubgrupoCategoriaResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody SubgrupoCategoriaRequestDTO dto) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(subgrupoCategoriaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        subgrupoCategoriaService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}

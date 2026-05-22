package com.suporte.tickets.controller;

import com.suporte.tickets.dto.ContatoRequestDTO;
import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.dto.EtiquetaResponseDTO;
import com.suporte.tickets.dto.TicketEtiquetasRequestDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.service.AuditoriaService;
import com.suporte.tickets.service.ContatoEtiquetaService;
import com.suporte.tickets.service.ContatoService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/contatos")
@RequiredArgsConstructor
public class ContatoController {

    private final ContatoService contatoService;
    private final ContatoEtiquetaService contatoEtiquetaService;
    private final AuditoriaService auditoriaService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @PostMapping
    public ResponseEntity<ContatoResponseDTO> criar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody ContatoRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(contatoService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<ContatoResponseDTO>> listar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestParam Integer clienteId) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoService.listarPorCliente(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContatoResponseDTO> buscarPorId(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoService.buscarPorId(id));
    }

    @GetMapping("/busca")
    public ResponseEntity<ContatoResponseDTO> buscarPorClienteEWhatsapp(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestParam Integer clienteId,
            @RequestParam String whatsapp) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoService.buscarPorClienteEWhatsapp(clienteId, whatsapp));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContatoResponseDTO> atualizar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id,
            @Valid @RequestBody ContatoRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<ContatoResponseDTO> ativar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoService.ativar(id));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<ContatoResponseDTO> inativar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoService.inativar(id));
    }

    @GetMapping("/{id}/etiquetas")
    public ResponseEntity<List<EtiquetaResponseDTO>> listarEtiquetasDoContato(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoEtiquetaService.listarPorContatoId(id));
    }

    @PutMapping("/{id}/etiquetas")
    public ResponseEntity<List<EtiquetaResponseDTO>> substituirEtiquetasDoContato(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id,
            @Valid @RequestBody(required = false) TicketEtiquetasRequestDTO requestDTO) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        List<Long> ids = requestDTO != null && requestDTO.getEtiquetaIds() != null
                ? requestDTO.getEtiquetaIds()
                : List.of();
        List<EtiquetaResponseDTO> atualizadas = contatoEtiquetaService.substituirVinculosAtivos(id, ids);
        auditoriaService.registrar(
                AuditoriaService.ACAO_CONTATO_ETIQUETAS,
                AuditoriaService.ENTIDADE_CONTATO,
                String.valueOf(id),
                "Etiquetas do contato atualizadas",
                executor);
        return ResponseEntity.ok(atualizadas);
    }
}

package com.suporte.tickets.controller;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.dto.AnalistaFilaDTO;
import com.suporte.tickets.dto.AnalistaLoginRequestDTO;
import com.suporte.tickets.dto.AnalistaResponseDTO;
import com.suporte.tickets.dto.AtualizarAnalistaRequestDTO;
import com.suporte.tickets.dto.AtualizarPerfilAcessoRequestDTO;
import com.suporte.tickets.dto.AtualizarStatusOperadorRequestDTO;
import com.suporte.tickets.dto.CriarAnalistaRequestDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.service.AnalistaService;
import com.suporte.tickets.service.AuditoriaService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/analistas")
@RequiredArgsConstructor
public class AnalistaController {

    private final AnalistaService analistaService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<AnalistaResponseDTO>> listarTodos(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(analistaService.listarTodos());
    }

    @GetMapping("/online")
    public ResponseEntity<List<AnalistaResponseDTO>> listarOnline(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(analistaService.listarOnline());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalistaResponseDTO> buscarPorId(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirProprioAnalistaOuAdmin(analistaId, analistaToken, id);
        return ResponseEntity.ok(analistaService.buscarResponsePorId(id));
    }

    @GetMapping("/{id}/tickets")
    public ResponseEntity<List<TicketResponseDTO>> listarTicketsPorAnalista(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirProprioAnalistaOuAdmin(analistaId, analistaToken, id);
        return ResponseEntity.ok(analistaService.listarTicketsPorAnalista(id));
    }

    @GetMapping("/filas")
    public ResponseEntity<List<AnalistaFilaDTO>> listarFilas(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(analistaService.listarFilasAnalistasOnline());
    }

    @PostMapping
    public ResponseEntity<AnalistaResponseDTO> criar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody CriarAnalistaRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        Analista executor = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        AnalistaResponseDTO criado = analistaService.criar(request);
        auditoriaService.registrar(
                AuditoriaService.ACAO_ANALISTA_CRIAR,
                AuditoriaService.ENTIDADE_ANALISTA,
                String.valueOf(criado.getId()),
                "Analista criado: " + criado.getEmail(),
                executor);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/login")
    public ResponseEntity<AnalistaResponseDTO> login(@Valid @RequestBody AnalistaLoginRequestDTO request) {
        AnalistaResponseDTO response = analistaService.autenticar(request);
        auditoriaService.registrar(
                AuditoriaService.ACAO_LOGIN_SUCESSO,
                AuditoriaService.ENTIDADE_ANALISTA,
                String.valueOf(response.getId()),
                "Login bem-sucedido",
                response.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        Analista analista = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        auditoriaService.registrar(
                AuditoriaService.ACAO_LOGOUT,
                AuditoriaService.ENTIDADE_ANALISTA,
                String.valueOf(analista.getId()),
                "Logout",
                analista);
        analistaService.encerrarSessao(analista.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnalistaResponseDTO> atualizarCadastro(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody AtualizarAnalistaRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        Analista executor = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        AnalistaResponseDTO atualizado = analistaService.atualizarCadastro(id, request);
        String desc = request.getAtivo() != null && !request.getAtivo()
                ? "Analista inativado: " + atualizado.getEmail()
                : "Analista atualizado: " + atualizado.getEmail();
        auditoriaService.registrar(
                AuditoriaService.ACAO_ANALISTA_EDITAR,
                AuditoriaService.ENTIDADE_ANALISTA,
                String.valueOf(id),
                desc,
                executor);
        return ResponseEntity.ok(atualizado);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AnalistaResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody AtualizarStatusOperadorRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirProprioAnalistaOuAdmin(analistaId, analistaToken, id);
        return ResponseEntity.ok(analistaService.atualizarStatusOperador(id, request.getStatusOperador()));
    }

    @PutMapping("/{id}/perfil-acesso")
    public ResponseEntity<AnalistaResponseDTO> atualizarPerfilAcesso(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody AtualizarPerfilAcessoRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        Analista executor = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        AnalistaResponseDTO atualizado = analistaService.atualizarPerfilAcesso(id, request.getPerfilAcesso());
        auditoriaService.registrar(
                AuditoriaService.ACAO_ANALISTA_PERFIL_ACESSO,
                AuditoriaService.ENTIDADE_ANALISTA,
                String.valueOf(id),
                "Perfil de acesso alterado para " + request.getPerfilAcesso(),
                executor);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<AnalistaResponseDTO> uploadFoto(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestParam("foto") MultipartFile foto) {
        perfilAcessoAutorizacaoService.exigirProprioAnalistaOuAdmin(analistaId, analistaToken, id);
        return ResponseEntity.ok(analistaService.salvarFotoPerfil(id, foto));
    }

    @DeleteMapping("/{id}/foto")
    public ResponseEntity<AnalistaResponseDTO> removerFoto(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirProprioAnalistaOuAdmin(analistaId, analistaToken, id);
        return ResponseEntity.ok(analistaService.removerFotoPerfil(id));
    }
}

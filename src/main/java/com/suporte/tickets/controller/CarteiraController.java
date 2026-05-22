package com.suporte.tickets.controller;

import com.suporte.tickets.dto.CarteiraRequestDTO;
import com.suporte.tickets.dto.CarteiraResponseDTO;
import com.suporte.tickets.service.CarteiraService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

/**
 * Cadastro de Conexão/Revenda (entidade técnica {@link com.suporte.tickets.entity.Carteira}).
 */
@RestController
@RequestMapping("/api/carteiras")
@RequiredArgsConstructor
public class CarteiraController {

    private final CarteiraService carteiraService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @GetMapping
    public ResponseEntity<List<CarteiraResponseDTO>> listar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(carteiraService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarteiraResponseDTO> buscar(
            @PathVariable Integer id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(carteiraService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<CarteiraResponseDTO> criar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody CarteiraRequestDTO dto) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(carteiraService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarteiraResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody CarteiraRequestDTO dto) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.ok(carteiraService.atualizar(id, dto));
    }

    @PostMapping(value = "/{id}/arte-header-chats", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CarteiraResponseDTO> uploadArteHeaderChats(
            @PathVariable Integer id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestParam("arte") MultipartFile arte) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.ok(carteiraService.salvarArteHeaderChats(id, arte));
    }
}

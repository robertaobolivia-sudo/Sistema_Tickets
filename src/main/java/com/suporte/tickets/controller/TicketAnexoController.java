package com.suporte.tickets.controller;

import com.suporte.tickets.dto.TicketAnexoResponseDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.TicketAnexoOrigem;
import com.suporte.tickets.service.AuditoriaService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.TicketAnexoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tickets/{numeroTicket}/anexos")
@RequiredArgsConstructor
public class TicketAnexoController {

    private final TicketAnexoService ticketAnexoService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<TicketAnexoResponseDTO>> listar(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(ticketAnexoService.listarPorNumeroTicket(numeroTicket));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketAnexoResponseDTO> enviar(
            @PathVariable String numeroTicket,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketAnexoResponseDTO criado = ticketAnexoService.salvarArquivo(
                numeroTicket,
                arquivo,
                executor,
                TicketAnexoOrigem.MANUAL);
        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_ANEXO,
                AuditoriaService.ENTIDADE_TICKET,
                numeroTicket,
                "Anexo adicionado: " + criado.getNomeArquivo(),
                executor);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/{anexoId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable String numeroTicket,
            @PathVariable Long anexoId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        Resource resource = ticketAnexoService.download(numeroTicket, anexoId);
        String nome = ticketAnexoService.nomeDownload(numeroTicket, anexoId);
        MediaType mediaType = ticketAnexoService.mediaTypeDoAnexo(numeroTicket, anexoId);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionAttachment(nome))
                .body(resource);
    }

    private static String contentDispositionAttachment(String fileName) {
        String safe = fileName != null ? fileName.replace("\"", "") : "anexo";
        return "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encodeUtf8(safe);
    }

    private static String encodeUtf8(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}

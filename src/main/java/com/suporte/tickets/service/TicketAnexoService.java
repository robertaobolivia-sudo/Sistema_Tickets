package com.suporte.tickets.service;

import com.suporte.tickets.config.UploadStorageProperties;
import com.suporte.tickets.dto.TicketAnexoResponseDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketAnexo;
import com.suporte.tickets.entity.TicketAnexoOrigem;
import com.suporte.tickets.repository.TicketAnexoRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketAnexoService {

    public static final long MAX_TAMANHO_BYTES = 10L * 1024L * 1024L;

    private final TicketRepository ticketRepository;
    private final TicketAnexoRepository ticketAnexoRepository;
    private final UploadStorageProperties uploadStorageProperties;

    @Transactional(readOnly = true)
    public List<TicketAnexoResponseDTO> listarPorNumeroTicket(String numeroTicket) {
        Ticket ticket = buscarTicket(numeroTicket);
        return ticketAnexoRepository.findByTicketOrderByCriadoEmDesc(ticket)
                .stream()
                .map(a -> TicketAnexoResponseDTO.fromEntity(a, arquivoExiste(a)))
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketAnexoResponseDTO salvarArquivo(
            String numeroTicket,
            MultipartFile arquivo,
            Analista executor,
            TicketAnexoOrigem origem) {
        validarArquivoUpload(arquivo);
        Ticket ticket = buscarTicket(numeroTicket);
        TicketAnexoOrigem origemFinal = origem != null ? origem : TicketAnexoOrigem.MANUAL;

        String nomeOriginal = sanitizarNomeArquivo(arquivo.getOriginalFilename());
        String extensao = obterExtensao(nomeOriginal);
        String nomeArmazenado = UUID.randomUUID() + extensao;

        try {
            Path destinoDir = uploadStorageProperties.getTicketDir(numeroTicket);
            Files.createDirectories(destinoDir);
            Path destino = destinoDir.resolve(nomeArmazenado);
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            TicketAnexo anexo = new TicketAnexo();
            anexo.setTicket(ticket);
            anexo.setNomeArquivo(nomeOriginal);
            anexo.setTipoConteudo(normalizarTipoConteudo(arquivo.getContentType()));
            anexo.setTamanhoBytes(arquivo.getSize());
            anexo.setIdentificadorArquivo(uploadStorageProperties.toTicketIdentificador(numeroTicket, nomeArmazenado));
            anexo.setOrigem(origemFinal);
            if (executor != null) {
                anexo.setCriadoPorAnalistaId(executor.getId());
                anexo.setCriadoPorNome(executor.getNome());
            }
            TicketAnexo salvo = ticketAnexoRepository.save(anexo);
            return TicketAnexoResponseDTO.fromEntity(salvo, arquivoExiste(salvo));
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar anexo do ticket", e);
        }
    }

    @Transactional(readOnly = true)
    public Resource download(String numeroTicket, Long anexoId) {
        Ticket ticket = buscarTicket(numeroTicket);
        TicketAnexo anexo = ticketAnexoRepository.findByIdAndTicket(anexoId, ticket)
                .orElseThrow(() -> new IllegalArgumentException("Anexo nao encontrado para este ticket"));
        Path path = uploadStorageProperties.resolveTicketAnexo(anexo.getIdentificadorArquivo());
        if (path == null || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Arquivo do anexo nao esta disponivel para download");
        }
        return new FileSystemResource(path);
    }

    @Transactional(readOnly = true)
    public MediaType mediaTypeDoAnexo(String numeroTicket, Long anexoId) {
        Ticket ticket = buscarTicket(numeroTicket);
        TicketAnexo anexo = ticketAnexoRepository.findByIdAndTicket(anexoId, ticket)
                .orElseThrow(() -> new IllegalArgumentException("Anexo nao encontrado para este ticket"));
        String tipo = anexo.getTipoConteudo();
        if (tipo != null && !tipo.isBlank()) {
            try {
                return MediaType.parseMediaType(tipo);
            } catch (Exception ignored) {
                // fallback abaixo
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    public String nomeDownload(String numeroTicket, Long anexoId) {
        Ticket ticket = buscarTicket(numeroTicket);
        TicketAnexo anexo = ticketAnexoRepository.findByIdAndTicket(anexoId, ticket)
                .orElseThrow(() -> new IllegalArgumentException("Anexo nao encontrado para este ticket"));
        return anexo.getNomeArquivo();
    }

    static void validarArquivoUpload(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo e obrigatorio");
        }
        if (arquivo.getSize() > MAX_TAMANHO_BYTES) {
            throw new IllegalArgumentException("Arquivo excede o tamanho maximo de 10 MB");
        }
        String nome = sanitizarNomeArquivo(arquivo.getOriginalFilename());
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("Nome do arquivo invalido");
        }
    }

    static String sanitizarNomeArquivo(String nome) {
        if (nome == null) {
            return "arquivo";
        }
        String base = Path.of(nome).getFileName().toString().trim();
        base = base.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (base.isEmpty()) {
            return "arquivo";
        }
        return base.length() > 200 ? base.substring(0, 200) : base;
    }

    private static String obterExtensao(String nome) {
        int idx = nome.lastIndexOf('.');
        if (idx < 0 || idx == nome.length() - 1) {
            return "";
        }
        String ext = nome.substring(idx).toLowerCase(Locale.ROOT);
        if (ext.length() > 12) {
            return "";
        }
        return ext.replaceAll("[^a-z0-9.]", "");
    }

    private static String normalizarTipoConteudo(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return contentType.trim().length() > 120 ? contentType.trim().substring(0, 120) : contentType.trim();
    }

    private boolean arquivoExiste(TicketAnexo anexo) {
        Path path = uploadStorageProperties.resolveTicketAnexo(anexo.getIdentificadorArquivo());
        return path != null && Files.isRegularFile(path);
    }

    private Ticket buscarTicket(String numeroTicket) {
        return ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));
    }
}

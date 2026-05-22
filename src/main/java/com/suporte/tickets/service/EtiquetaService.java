package com.suporte.tickets.service;

import com.suporte.tickets.dto.EtiquetaRequestDTO;
import com.suporte.tickets.dto.EtiquetaResponseDTO;
import com.suporte.tickets.entity.Etiqueta;
import com.suporte.tickets.repository.EtiquetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;

    @Transactional(readOnly = true)
    public List<EtiquetaResponseDTO> listarTodos() {
        return etiquetaRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(EtiquetaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EtiquetaResponseDTO> listarAtivas() {
        return etiquetaRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(EtiquetaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public EtiquetaResponseDTO criar(EtiquetaRequestDTO dto) {
        validarNomeDuplicado(dto.getNome(), null);
        Etiqueta etiqueta = new Etiqueta();
        preencher(etiqueta, dto);
        etiqueta.setAtivo(true);
        return EtiquetaResponseDTO.fromEntity(etiquetaRepository.save(etiqueta));
    }

    @Transactional
    public EtiquetaResponseDTO atualizar(Long id, EtiquetaRequestDTO dto) {
        Etiqueta etiqueta = buscarEntidade(id);
        validarNomeDuplicado(dto.getNome(), id);
        preencher(etiqueta, dto);
        return EtiquetaResponseDTO.fromEntity(etiquetaRepository.save(etiqueta));
    }

    @Transactional
    public EtiquetaResponseDTO ativar(Long id) {
        Etiqueta etiqueta = buscarEntidade(id);
        etiqueta.setAtivo(true);
        return EtiquetaResponseDTO.fromEntity(etiquetaRepository.save(etiqueta));
    }

    @Transactional
    public EtiquetaResponseDTO inativar(Long id) {
        Etiqueta etiqueta = buscarEntidade(id);
        etiqueta.setAtivo(false);
        return EtiquetaResponseDTO.fromEntity(etiquetaRepository.save(etiqueta));
    }

    private void preencher(Etiqueta etiqueta, EtiquetaRequestDTO dto) {
        etiqueta.setNome(dto.getNome().trim());
        etiqueta.setDescricao(trimOrNull(dto.getDescricao()));
        etiqueta.setCor(normalizarCor(dto.getCor()));
    }

    private static String normalizarCor(String cor) {
        if (cor == null || cor.isBlank()) {
            return null;
        }
        String c = cor.trim();
        if (c.length() > 20) {
            c = c.substring(0, 20);
        }
        return c;
    }

    private Etiqueta buscarEntidade(Long id) {
        return etiquetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etiqueta nao encontrada: " + id));
    }

    private void validarNomeDuplicado(String nome, Long idAtual) {
        etiquetaRepository.findByNomeIgnoreCase(nome.trim())
                .filter(e -> idAtual == null || !e.getId().equals(idAtual))
                .ifPresent(e -> {
                    throw new IllegalArgumentException("Etiqueta ja cadastrada: " + nome);
                });
    }

    private static String trimOrNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

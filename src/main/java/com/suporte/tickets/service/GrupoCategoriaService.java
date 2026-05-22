package com.suporte.tickets.service;

import com.suporte.tickets.dto.GrupoCategoriaRequestDTO;
import com.suporte.tickets.dto.GrupoCategoriaResponseDTO;
import com.suporte.tickets.entity.GrupoCategoria;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrupoCategoriaService {

    private final GrupoCategoriaRepository grupoCategoriaRepository;

    @Transactional
    public GrupoCategoriaResponseDTO criar(GrupoCategoriaRequestDTO dto) {
        validarNomeDuplicado(dto.getNome(), null);
        GrupoCategoria grupo = new GrupoCategoria();
        grupo.setNome(dto.getNome().trim());
        grupo.setDescricao(trimOrNull(dto.getDescricao()));
        grupo.setAtivo(true);
        return GrupoCategoriaResponseDTO.fromEntity(grupoCategoriaRepository.save(grupo));
    }

    @Transactional(readOnly = true)
    public List<GrupoCategoriaResponseDTO> listar() {
        return grupoCategoriaRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(GrupoCategoriaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GrupoCategoria buscarEntidadeAtiva(Long id) {
        GrupoCategoria grupo = buscarEntidade(id);
        if (!Boolean.TRUE.equals(grupo.getAtivo())) {
            throw new IllegalArgumentException("Grupo inativo: " + id);
        }
        return grupo;
    }

    @Transactional(readOnly = true)
    public GrupoCategoriaResponseDTO buscarPorId(Long id) {
        return GrupoCategoriaResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public GrupoCategoriaResponseDTO atualizar(Long id, GrupoCategoriaRequestDTO dto) {
        GrupoCategoria grupo = buscarEntidade(id);
        validarNomeDuplicado(dto.getNome(), id);
        grupo.setNome(dto.getNome().trim());
        grupo.setDescricao(trimOrNull(dto.getDescricao()));
        return GrupoCategoriaResponseDTO.fromEntity(grupoCategoriaRepository.save(grupo));
    }

    @Transactional
    public void inativar(Long id) {
        GrupoCategoria grupo = buscarEntidade(id);
        grupo.setAtivo(false);
        grupoCategoriaRepository.save(grupo);
    }

    private GrupoCategoria buscarEntidade(Long id) {
        return grupoCategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo nao encontrado: " + id));
    }

    private void validarNomeDuplicado(String nome, Long idAtual) {
        grupoCategoriaRepository.findByNomeIgnoreCase(nome.trim())
                .filter(grupo -> idAtual == null || !grupo.getId().equals(idAtual))
                .ifPresent(grupo -> {
                    throw new IllegalArgumentException("Grupo ja cadastrado: " + nome);
                });
    }

    private String trimOrNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

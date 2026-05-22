package com.suporte.tickets.service;

import com.suporte.tickets.dto.SubgrupoCategoriaRequestDTO;
import com.suporte.tickets.dto.SubgrupoCategoriaResponseDTO;
import com.suporte.tickets.entity.GrupoCategoria;
import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.repository.SubgrupoCategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubgrupoCategoriaService {

    private final SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    private final GrupoCategoriaService grupoCategoriaService;

    @Transactional
    public SubgrupoCategoriaResponseDTO criar(SubgrupoCategoriaRequestDTO dto) {
        GrupoCategoria grupo = grupoCategoriaService.buscarEntidadeAtiva(dto.getGrupoId());
        validarDuplicidade(dto.getGrupoId(), dto.getNome());

        SubgrupoCategoria subgrupo = new SubgrupoCategoria();
        subgrupo.setGrupoCategoria(grupo);
        subgrupo.setNome(dto.getNome().trim());
        subgrupo.setDescricao(trimOrNull(dto.getDescricao()));
        subgrupo.setAtivo(true);
        return SubgrupoCategoriaResponseDTO.fromEntity(subgrupoCategoriaRepository.save(subgrupo));
    }

    @Transactional(readOnly = true)
    public List<SubgrupoCategoriaResponseDTO> listar() {
        return subgrupoCategoriaRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(SubgrupoCategoriaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubgrupoCategoriaResponseDTO> listarPorGrupo(Long grupoId) {
        grupoCategoriaService.buscarEntidadeAtiva(grupoId);
        return subgrupoCategoriaRepository.findByGrupoCategoriaIdAndAtivoTrueOrderByNomeAsc(grupoId)
                .stream()
                .map(SubgrupoCategoriaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubgrupoCategoria buscarEntidadeAtiva(Long id) {
        SubgrupoCategoria subgrupo = buscarEntidade(id);
        if (!Boolean.TRUE.equals(subgrupo.getAtivo())) {
            throw new IllegalArgumentException("Subgrupo inativo: " + id);
        }
        return subgrupo;
    }

    @Transactional(readOnly = true)
    public SubgrupoCategoriaResponseDTO buscarPorId(Long id) {
        return SubgrupoCategoriaResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public SubgrupoCategoriaResponseDTO atualizar(Long id, SubgrupoCategoriaRequestDTO dto) {
        SubgrupoCategoria subgrupo = buscarEntidade(id);
        GrupoCategoria grupo = grupoCategoriaService.buscarEntidadeAtiva(dto.getGrupoId());

        boolean mudouContexto = !subgrupo.getGrupoCategoria().getId().equals(dto.getGrupoId())
                || !subgrupo.getNome().equalsIgnoreCase(dto.getNome().trim());
        if (mudouContexto) {
            validarDuplicidade(dto.getGrupoId(), dto.getNome());
        }

        subgrupo.setGrupoCategoria(grupo);
        subgrupo.setNome(dto.getNome().trim());
        subgrupo.setDescricao(trimOrNull(dto.getDescricao()));
        return SubgrupoCategoriaResponseDTO.fromEntity(subgrupoCategoriaRepository.save(subgrupo));
    }

    @Transactional
    public void inativar(Long id) {
        SubgrupoCategoria subgrupo = buscarEntidade(id);
        subgrupo.setAtivo(false);
        subgrupoCategoriaRepository.save(subgrupo);
    }

    private SubgrupoCategoria buscarEntidade(Long id) {
        return subgrupoCategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subgrupo nao encontrado: " + id));
    }

    private void validarDuplicidade(Long grupoId, String nome) {
        if (subgrupoCategoriaRepository.existsByGrupoCategoriaIdAndNomeIgnoreCase(grupoId, nome.trim())) {
            throw new IllegalArgumentException("Subgrupo ja cadastrado para o grupo informado: " + nome);
        }
    }

    private String trimOrNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

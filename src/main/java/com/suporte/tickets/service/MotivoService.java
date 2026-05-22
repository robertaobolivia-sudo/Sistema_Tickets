package com.suporte.tickets.service;

import com.suporte.tickets.dto.MotivoRequestDTO;
import com.suporte.tickets.dto.MotivoResponseDTO;
import com.suporte.tickets.entity.Motivo;
import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.repository.MotivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MotivoService {

    private final MotivoRepository motivoRepository;
    private final SubgrupoCategoriaService subgrupoCategoriaService;

    @Transactional
    public MotivoResponseDTO criar(MotivoRequestDTO dto) {
        SubgrupoCategoria subgrupo = subgrupoCategoriaService.buscarEntidadeAtiva(dto.getSubgrupoId());
        validarDuplicidade(subgrupo.getId(), dto.getNome());

        Motivo motivo = new Motivo();
        motivo.setSubgrupoCategoria(subgrupo);
        motivo.setNome(dto.getNome().trim());
        motivo.setDescricao(trimOrNull(dto.getDescricao()));
        motivo.setAtivo(true);
        return MotivoResponseDTO.fromEntity(motivoRepository.save(motivo));
    }

    @Transactional(readOnly = true)
    public List<MotivoResponseDTO> listar(Long subgrupoId) {
        if (subgrupoId != null) {
            return listarPorSubgrupo(subgrupoId);
        }
        return motivoRepository.findAll()
                .stream()
                .map(MotivoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MotivoResponseDTO> listarPorSubgrupo(Long subgrupoId) {
        subgrupoCategoriaService.buscarEntidadeAtiva(subgrupoId);
        return motivoRepository.findBySubgrupoCategoriaIdAndAtivoTrueOrderByNomeAsc(subgrupoId)
                .stream()
                .map(MotivoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MotivoResponseDTO buscarPorId(Long id) {
        return MotivoResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Motivo buscarEntidadeAtiva(Long id) {
        Motivo motivo = buscarEntidade(id);
        if (!Boolean.TRUE.equals(motivo.getAtivo())) {
            throw new IllegalArgumentException("Motivo inativo: " + id);
        }
        return motivo;
    }

    @Transactional
    public MotivoResponseDTO atualizar(Long id, MotivoRequestDTO dto) {
        Motivo motivo = buscarEntidade(id);
        SubgrupoCategoria subgrupo = subgrupoCategoriaService.buscarEntidadeAtiva(dto.getSubgrupoId());

        boolean mudouContexto = !motivo.getSubgrupoCategoria().getId().equals(dto.getSubgrupoId())
                || !motivo.getNome().equalsIgnoreCase(dto.getNome().trim());
        if (mudouContexto) {
            validarDuplicidade(dto.getSubgrupoId(), dto.getNome());
        }

        motivo.setSubgrupoCategoria(subgrupo);
        motivo.setNome(dto.getNome().trim());
        motivo.setDescricao(trimOrNull(dto.getDescricao()));
        return MotivoResponseDTO.fromEntity(motivoRepository.save(motivo));
    }

    @Transactional
    public void ativar(Long id) {
        Motivo motivo = buscarEntidade(id);
        motivo.setAtivo(true);
        motivoRepository.save(motivo);
    }

    @Transactional
    public void inativar(Long id) {
        Motivo motivo = buscarEntidade(id);
        motivo.setAtivo(false);
        motivoRepository.save(motivo);
    }

    private Motivo buscarEntidade(Long id) {
        return motivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Motivo nao encontrado: " + id));
    }

    private void validarDuplicidade(Long subgrupoId, String nome) {
        if (motivoRepository.existsBySubgrupoCategoriaIdAndNomeIgnoreCase(subgrupoId, nome.trim())) {
            throw new IllegalArgumentException("Motivo ja cadastrado para a subcategoria: " + nome);
        }
    }

    private String trimOrNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

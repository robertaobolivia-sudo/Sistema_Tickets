package com.suporte.tickets.dto;

import com.suporte.tickets.entity.Motivo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotivoResponseDTO {

    private Long id;
    private Long subgrupoId;
    private String subgrupoNome;
    private Long grupoId;
    private String grupoNome;
    private String nome;
    private String descricao;
    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static MotivoResponseDTO fromEntity(Motivo m) {
        MotivoResponseDTO dto = new MotivoResponseDTO();
        dto.setId(m.getId());
        dto.setNome(m.getNome());
        dto.setDescricao(m.getDescricao());
        dto.setAtivo(m.getAtivo());
        dto.setCriadoEm(m.getCriadoEm());
        dto.setAtualizadoEm(m.getAtualizadoEm());
        if (m.getSubgrupoCategoria() != null) {
            dto.setSubgrupoId(m.getSubgrupoCategoria().getId());
            dto.setSubgrupoNome(m.getSubgrupoCategoria().getNome());
            if (m.getSubgrupoCategoria().getGrupoCategoria() != null) {
                dto.setGrupoId(m.getSubgrupoCategoria().getGrupoCategoria().getId());
                dto.setGrupoNome(m.getSubgrupoCategoria().getGrupoCategoria().getNome());
            }
        }
        return dto;
    }
}

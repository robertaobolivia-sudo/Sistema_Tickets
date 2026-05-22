package com.suporte.tickets.dto;

import com.suporte.tickets.entity.SubgrupoCategoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubgrupoCategoriaResponseDTO {

    private Long id;
    private Long grupoId;
    private String grupoNome;
    private String nome;
    private String descricao;
    private Boolean ativo;
    private LocalDateTime dataCadastro;

    public static SubgrupoCategoriaResponseDTO fromEntity(SubgrupoCategoria subgrupo) {
        return new SubgrupoCategoriaResponseDTO(
                subgrupo.getId(),
                subgrupo.getGrupoCategoria().getId(),
                subgrupo.getGrupoCategoria().getNome(),
                subgrupo.getNome(),
                subgrupo.getDescricao(),
                subgrupo.getAtivo(),
                subgrupo.getDataCadastro()
        );
    }
}

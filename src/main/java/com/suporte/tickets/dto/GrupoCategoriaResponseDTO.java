package com.suporte.tickets.dto;

import com.suporte.tickets.entity.GrupoCategoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoCategoriaResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private Boolean ativo;
    private LocalDateTime dataCadastro;

    public static GrupoCategoriaResponseDTO fromEntity(GrupoCategoria grupo) {
        return new GrupoCategoriaResponseDTO(
                grupo.getId(),
                grupo.getNome(),
                grupo.getDescricao(),
                grupo.getAtivo(),
                grupo.getDataCadastro()
        );
    }
}

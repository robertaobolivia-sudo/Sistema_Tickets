package com.suporte.tickets.dto;

import com.suporte.tickets.entity.Etiqueta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetaResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private String cor;
    private Boolean ativo;
    private LocalDateTime criadoEm;

    public static EtiquetaResponseDTO fromEntity(Etiqueta etiqueta) {
        return new EtiquetaResponseDTO(
                etiqueta.getId(),
                etiqueta.getNome(),
                etiqueta.getDescricao(),
                etiqueta.getCor(),
                etiqueta.getAtivo(),
                etiqueta.getCriadoEm()
        );
    }
}

package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeriadoDTO {

    private Long id;
    private String data;
    private String descricao;
    private String tipo;
    private String escopo;
    private Boolean ativo;
}

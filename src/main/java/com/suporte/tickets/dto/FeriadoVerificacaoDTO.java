package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeriadoVerificacaoDTO {

    private String data;
    private boolean feriado;
    private String descricao;
    private String escopo;
}

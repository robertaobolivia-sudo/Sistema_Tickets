package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlaMetaSeedResultadoDTO {

    private String mensagem;
    private int criados;
    private int ignorados;
}

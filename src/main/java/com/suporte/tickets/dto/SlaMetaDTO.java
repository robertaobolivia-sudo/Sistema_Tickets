package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlaMetaDTO {

    private Long id;
    private String prioridade;
    private Integer prazoPrimeiroAtendimentoMinutos;
    private Integer prazoResolucaoMinutos;
    private Boolean ativo;
}

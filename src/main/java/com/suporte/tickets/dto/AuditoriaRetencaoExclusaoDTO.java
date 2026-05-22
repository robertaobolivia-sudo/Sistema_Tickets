package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaRetencaoExclusaoDTO {

    private LocalDate antesDe;
    private long excluidos;
}

package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSatisfacaoEvolucaoDiaDTO {

    private LocalDate data;
    private long totalAvaliacoes;
    private Double mediaNota;
    private long positivas;
    private long negativas;
}

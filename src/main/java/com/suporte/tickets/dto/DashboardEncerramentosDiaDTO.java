package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEncerramentosDiaDTO {

    private long finalizados;
    private long naoResolvidos;
    private long escalonados;
    private long abandonados;
    private List<DashboardRecorrenciaDiaDTO> recorrencias = new ArrayList<>();
}

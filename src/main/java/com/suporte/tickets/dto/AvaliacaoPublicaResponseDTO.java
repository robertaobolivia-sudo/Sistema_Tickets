package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoPublicaResponseDTO {

    private String status;
    private boolean expirada;
    private boolean jaRespondida;
    private String clienteNome;
    private String protocoloMascarado;
    private String mensagemOrientativa;
}

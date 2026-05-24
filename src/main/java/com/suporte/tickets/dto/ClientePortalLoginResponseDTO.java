package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientePortalLoginResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private Integer clienteId;
    private String clienteNome;
    private String authToken;
}

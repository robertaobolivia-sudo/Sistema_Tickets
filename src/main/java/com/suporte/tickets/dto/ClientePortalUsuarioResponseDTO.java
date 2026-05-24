package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientePortalUsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private Integer clienteId;
    private String clienteNome;
    private Boolean ativo;
    private LocalDateTime criadoEm;
}

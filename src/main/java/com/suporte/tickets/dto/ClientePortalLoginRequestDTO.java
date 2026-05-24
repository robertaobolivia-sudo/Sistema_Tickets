package com.suporte.tickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientePortalLoginRequestDTO {
    @Email @NotBlank
    private String email;
    @NotBlank
    private String senha;
}

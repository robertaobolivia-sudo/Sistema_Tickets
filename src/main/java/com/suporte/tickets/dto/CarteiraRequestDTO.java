package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CarteiraRequestDTO {

    @NotBlank(message = "O nome da conexão/revenda é obrigatório")
    @Size(max = 100)
    private String nome;
}

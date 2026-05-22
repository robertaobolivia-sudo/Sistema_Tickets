package com.suporte.tickets.dto;

import com.suporte.tickets.entity.Carteira;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarteiraResponseDTO {

    private Integer id;
    private String nome;
    private String arteHeaderChatsUrl;

    public static CarteiraResponseDTO fromEntity(Carteira carteira) {
        if (carteira == null) {
            return null;
        }
        return new CarteiraResponseDTO(
                carteira.getId(),
                carteira.getNome(),
                carteira.getArteHeaderChatsUrl());
    }
}

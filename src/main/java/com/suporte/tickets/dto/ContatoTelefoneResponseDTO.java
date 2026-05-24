package com.suporte.tickets.dto;

import com.suporte.tickets.entity.ContatoTelefone;
import lombok.Data;

@Data
public class ContatoTelefoneResponseDTO {

    private Long id;
    private Integer contatoId;
    private Integer clienteId;
    private String telefone;
    private String telefoneNormalizado;
    private Boolean principal;
    private String origem;

    public static ContatoTelefoneResponseDTO fromEntity(ContatoTelefone entity) {
        ContatoTelefoneResponseDTO dto = new ContatoTelefoneResponseDTO();
        dto.setId(entity.getId());
        dto.setContatoId(entity.getContato() != null ? entity.getContato().getId() : null);
        dto.setClienteId(entity.getCliente() != null ? entity.getCliente().getId() : null);
        dto.setTelefone(entity.getTelefone());
        dto.setTelefoneNormalizado(entity.getTelefoneNormalizado());
        dto.setPrincipal(entity.getPrincipal());
        dto.setOrigem(entity.getOrigem());
        return dto;
    }
}

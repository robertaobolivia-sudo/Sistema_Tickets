package com.suporte.tickets.dto;

import com.suporte.tickets.entity.WhatsappMatriz;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappMatrizResponseDTO {

    private Integer id;
    private Integer clienteId;
    private String clienteNome;
    private String nome;
    private String numero;
    private String numeroNormalizado;
    private Boolean ativo;
    private String provedor;
    private String identificadorExterno;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static WhatsappMatrizResponseDTO fromEntity(WhatsappMatriz m) {
        WhatsappMatrizResponseDTO dto = new WhatsappMatrizResponseDTO();
        dto.setId(m.getId());
        if (m.getCliente() != null) {
            dto.setClienteId(m.getCliente().getId());
            dto.setClienteNome(m.getCliente().getNome());
        }
        dto.setNome(m.getNome());
        dto.setNumero(m.getNumero());
        dto.setNumeroNormalizado(m.getNumeroNormalizado());
        dto.setAtivo(m.getAtivo());
        dto.setProvedor(m.getProvedor());
        dto.setIdentificadorExterno(m.getIdentificadorExterno());
        dto.setCriadoEm(m.getCriadoEm());
        dto.setAtualizadoEm(m.getAtualizadoEm());
        return dto;
    }
}

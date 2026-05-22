package com.suporte.tickets.dto;

import com.suporte.tickets.entity.InteracaoPendenteDecisao;
import com.suporte.tickets.entity.InteracaoPendenteDecisaoStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteracaoPendenteDecisaoResponseDTO {

    private Long id;
    private String status;
    private Integer clienteId;
    private String clienteNome;
    private Integer contatoId;
    private String contatoNome;
    private String contatoWhatsapp;
    private String numeroTicketAnterior;
    private String mensagem;
    private String canal;
    private LocalDateTime criadaEm;
    private String numeroTicketGerado;

    public static InteracaoPendenteDecisaoResponseDTO fromEntity(InteracaoPendenteDecisao p) {
        InteracaoPendenteDecisaoResponseDTO dto = new InteracaoPendenteDecisaoResponseDTO();
        dto.setId(p.getId());
        dto.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        if (p.getCliente() != null) {
            dto.setClienteId(p.getCliente().getId());
            dto.setClienteNome(p.getCliente().getNome());
        }
        if (p.getContato() != null) {
            dto.setContatoId(p.getContato().getId());
            dto.setContatoNome(p.getContato().getNome());
            dto.setContatoWhatsapp(p.getContato().getWhatsapp());
        }
        if (p.getTicketAnterior() != null) {
            dto.setNumeroTicketAnterior(p.getTicketAnterior().getNumeroTicket());
        }
        dto.setMensagem(p.getMensagem());
        dto.setCanal(p.getCanal());
        dto.setCriadaEm(p.getCriadaEm());
        if (p.getTicketGerado() != null) {
            dto.setNumeroTicketGerado(p.getTicketGerado().getNumeroTicket());
        }
        return dto;
    }
}

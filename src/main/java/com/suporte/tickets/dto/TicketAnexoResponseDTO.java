package com.suporte.tickets.dto;

import com.suporte.tickets.entity.TicketAnexo;
import com.suporte.tickets.entity.TicketAnexoOrigem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketAnexoResponseDTO {

    private Long id;
    private String numeroTicket;
    private String nomeArquivo;
    private String tipoConteudo;
    private Long tamanhoBytes;
    private String origem;
    private LocalDateTime criadoEm;
    private Long criadoPorAnalistaId;
    private String criadoPorNome;
    private boolean downloadDisponivel;

    public static TicketAnexoResponseDTO fromEntity(TicketAnexo anexo, boolean downloadDisponivel) {
        TicketAnexoOrigem origem = anexo.getOrigem();
        return new TicketAnexoResponseDTO(
                anexo.getId(),
                anexo.getTicket() != null ? anexo.getTicket().getNumeroTicket() : null,
                anexo.getNomeArquivo(),
                anexo.getTipoConteudo(),
                anexo.getTamanhoBytes(),
                origem != null ? origem.name() : TicketAnexoOrigem.MANUAL.name(),
                anexo.getCriadoEm(),
                anexo.getCriadoPorAnalistaId(),
                anexo.getCriadoPorNome(),
                downloadDisponivel
        );
    }
}

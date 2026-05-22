package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncerrarTicketRequestDTO {

    @NotNull(message = "Grupo e obrigatorio")
    private Long grupoId;

    @NotNull(message = "Subgrupo e obrigatorio")
    private Long subgrupoId;

    @NotNull(message = "Motivo e obrigatorio")
    private Long motivoId;

    @NotBlank(message = "Comentario de encerramento e obrigatorio")
    private String comentarioEncerramento;

    /** Opt-in da pesquisa pós-RESOLVIDO; null tratado como false no service. */
    private Boolean enviarPesquisaSatisfacao;
}

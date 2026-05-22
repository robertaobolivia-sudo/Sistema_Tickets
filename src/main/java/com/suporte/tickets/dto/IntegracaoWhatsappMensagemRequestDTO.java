package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegracaoWhatsappMensagemRequestDTO {

    @NotBlank(message = "Telefone e obrigatorio")
    private String telefone;

    private String nomeContato;

    @NotBlank(message = "Mensagem e obrigatoria")
    private String mensagem;

    private String canal;

    private Integer clienteId;

    /** Resolve Cliente contratante pelo cadastro matriz (Sprint 191). */
    private Integer whatsappMatrizId;

    /** Numero matriz receptor; alternativa a whatsappMatrizId. */
    private String numeroMatriz;

    private Integer contatoSolicitanteId;

    private String origemExternaId;
}

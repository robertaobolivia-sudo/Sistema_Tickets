package com.suporte.tickets.dto;

import com.suporte.tickets.entity.ContatoCliente;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoClienteResponseDTO {

    private Integer id;
    private Integer clienteId;
    private String clienteNome;
    private String nome;
    private String cargo;
    private String telefone;
    private String celular;
    private String email;
    private Boolean principal;
    private Boolean ativo;
    private String observacoes;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;

    public static ContatoClienteResponseDTO fromEntity(ContatoCliente contato) {
        String nomeCliente = contato.getCliente() != null ? contato.getCliente().getNome() : null;
        Integer clienteId = contato.getCliente() != null ? contato.getCliente().getId() : null;
        return new ContatoClienteResponseDTO(
                contato.getId(),
                clienteId,
                valorOuPadrao(nomeCliente),
                valorOuPadrao(contato.getNome()),
                valorOuPadrao(contato.getCargo()),
                valorOuPadrao(contato.getTelefone()),
                valorOuPadrao(contato.getCelular()),
                valorOuPadrao(contato.getEmail()),
                Boolean.TRUE.equals(contato.getPrincipal()),
                Boolean.TRUE.equals(contato.getAtivo()),
                valorOuPadrao(contato.getObservacoes()),
                contato.getDataCadastro(),
                contato.getDataAtualizacao()
        );
    }

    private static String valorOuPadrao(String valor) {
        if (valor == null || valor.isBlank()) {
            return "-";
        }
        return valor.trim();
    }
}

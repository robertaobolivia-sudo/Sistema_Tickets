package com.suporte.tickets.dto;

import com.suporte.tickets.entity.ClassificacaoCliente;
import com.suporte.tickets.entity.Cliente;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {

    private Integer id;
    private String nome;
    private String telefone;
    private String telefoneContato;
    private String email;
    private String empresa;
    private String cnpj;
    private String cidade;
    private String uf;
    private String endereco;
    private Integer carteiraId;
    private String carteira;
    private String status;
    private String classificacaoCliente;
    private String observacoes;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
    private String arteHeaderChatsUrl;

    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        String nomeCarteira = cliente.getCarteira() != null ? cliente.getCarteira().getNome() : null;
        Integer carteiraId = cliente.getCarteira() != null ? cliente.getCarteira().getId() : null;
        return new ClienteResponseDTO(
                cliente.getId(),
                valorOuPadrao(cliente.getNome()),
                valorOuPadrao(cliente.getTelefone()),
                valorOuPadrao(cliente.getTelefoneContato()),
                valorOuPadrao(cliente.getEmail()),
                valorOuPadrao(cliente.getEmpresa()),
                valorOuPadrao(cliente.getCnpj()),
                valorOuPadrao(cliente.getCidade()),
                valorOuPadrao(cliente.getUf()),
                valorOuPadrao(cliente.getEndereco()),
                carteiraId,
                valorOuPadrao(nomeCarteira),
                valorOuPadrao(cliente.getStatus()),
                ClassificacaoCliente.effective(cliente.getClassificacaoCliente()).name(),
                valorOuPadrao(cliente.getObservacoes()),
                cliente.getAtivo(),
                cliente.getDataCadastro(),
                cliente.getDataAtualizacao(),
                arteUrlPublica(cliente.getArteHeaderChatsUrl())
        );
    }

    private static String arteUrlPublica(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return url.trim();
    }

    private static String valorOuPadrao(String valor) {
        if (valor == null || valor.isBlank()) {
            return "-";
        }
        return valor.trim();
    }
}
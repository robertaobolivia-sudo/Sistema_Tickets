package com.suporte.tickets.dto;

import com.suporte.tickets.entity.ClassificacaoCliente;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.util.ClienteCamposLegadoSupport;
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
    private String razaoSocial;
    private String responsavel;
    private String telefone;
    private String whatsapp;
    private String telefoneContato;
    private String email;
    private String empresa;
    private String cnpj;
    private String inscricaoEstadual;
    private String cidade;
    private String uf;
    private String endereco;
    private String cep;
    private String site;
    private String horarioFuncionamento;
    private String status;
    private String classificacaoCliente;
    private String observacoes;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
    private String arteHeaderChatsUrl;

    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        ClienteCamposLegadoSupport.CamposLegadoObservacoes legado =
                ClienteCamposLegadoSupport.parseObservacoes(cliente.getObservacoes());

        String razaoSocial = primeiroNaoVazio(cliente.getRazaoSocial(), cliente.getEmpresa());
        String responsavel = primeiroNaoVazio(cliente.getResponsavel(), cliente.getNome());
        String whatsapp = primeiroNaoVazio(cliente.getWhatsapp(), cliente.getTelefone());
        String ie = primeiroNaoVazio(cliente.getInscricaoEstadual(), legado.inscricaoEstadual());
        String cep = primeiroNaoVazio(cliente.getCep(), legado.cep());
        String site = primeiroNaoVazio(cliente.getSite(), legado.site());
        String horario = primeiroNaoVazio(cliente.getHorarioFuncionamento(), legado.horarioFuncionamento());
        String obsLivre = legado.observacoesLivres();
        if (obsLivre == null || obsLivre.isBlank()) {
            obsLivre = observacoesSemPrefixos(cliente.getObservacoes()) ? null : cliente.getObservacoes();
        }

        return new ClienteResponseDTO(
                cliente.getId(),
                valorOuPadrao(responsavel),
                valorOuPadrao(razaoSocial),
                valorOuPadrao(responsavel),
                valorOuPadrao(whatsapp),
                valorOuPadrao(whatsapp),
                valorOuPadrao(cliente.getTelefoneContato()),
                valorOuPadrao(cliente.getEmail()),
                valorOuPadrao(razaoSocial),
                valorOuPadrao(cliente.getCnpj()),
                valorOuPadrao(ie),
                valorOuPadrao(cliente.getCidade()),
                valorOuPadrao(cliente.getUf()),
                valorOuPadrao(cliente.getEndereco()),
                valorOuPadrao(cep),
                valorOuPadrao(site),
                valorOuPadrao(horario),
                valorOuPadrao(cliente.getStatus()),
                ClassificacaoCliente.effective(cliente.getClassificacaoCliente()).name(),
                valorOuPadrao(obsLivre),
                cliente.getAtivo(),
                cliente.getDataCadastro(),
                cliente.getDataAtualizacao(),
                arteUrlPublica(cliente.getArteHeaderChatsUrl())
        );
    }

    private static boolean observacoesSemPrefixos(String observacoes) {
        if (observacoes == null || observacoes.isBlank()) {
            return true;
        }
        var p = ClienteCamposLegadoSupport.parseObservacoes(observacoes);
        return p.observacoesLivres() == null || p.observacoesLivres().isBlank();
    }

    private static String primeiroNaoVazio(String... valores) {
        if (valores == null) {
            return null;
        }
        for (String v : valores) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
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

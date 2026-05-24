package com.suporte.tickets.dto;

import com.suporte.tickets.entity.Contato;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoResponseDTO {

    private Integer id;
    private Integer clienteId;
    private String clienteNome;
    private String nome;
    private String whatsapp;
    private String email;
    private String empresaLocal;
    private String cidade;
    private String uf;
    private String observacoes;
    private Boolean ativo;
    private Boolean criadoAutomaticamente;
    private LocalDateTime primeiraInteracaoEm;
    private LocalDateTime ultimaInteracaoEm;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static ContatoResponseDTO fromEntity(Contato contato) {
        ContatoResponseDTO dto = new ContatoResponseDTO();
        dto.setId(contato.getId());
        if (contato.getCliente() != null) {
            dto.setClienteId(contato.getCliente().getId());
            String rotulo = contato.getCliente().getRazaoSocial();
            if (rotulo == null || rotulo.isBlank()) {
                rotulo = contato.getCliente().getEmpresa();
            }
            if (rotulo == null || rotulo.isBlank()) {
                rotulo = contato.getCliente().getNome();
            }
            dto.setClienteNome(rotulo);
        }
        dto.setNome(contato.getNome());
        dto.setWhatsapp(contato.getWhatsapp());
        dto.setEmail(contato.getEmail());
        dto.setEmpresaLocal(contato.getEmpresaLocal());
        dto.setCidade(contato.getCidade());
        dto.setUf(contato.getUf());
        dto.setObservacoes(contato.getObservacoes());
        dto.setAtivo(contato.getAtivo());
        dto.setCriadoAutomaticamente(contato.getCriadoAutomaticamente());
        dto.setPrimeiraInteracaoEm(contato.getPrimeiraInteracaoEm());
        dto.setUltimaInteracaoEm(contato.getUltimaInteracaoEm());
        dto.setCriadoEm(contato.getCriadoEm());
        dto.setAtualizadoEm(contato.getAtualizadoEm());
        return dto;
    }
}

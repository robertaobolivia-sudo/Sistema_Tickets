package com.suporte.tickets.service;

import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketInteracao;
import com.suporte.tickets.repository.ContatoTelefoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Sprint 292 — classifica telefone de entrada (principal vs adicional do Contato).
 */
@Service
@RequiredArgsConstructor
public class ContatoAtendimentoOrigemService {

    public static final String TIPO_PRINCIPAL = "PRINCIPAL";
    public static final String TIPO_ADICIONAL = "ADICIONAL";

    private final ContatoTelefoneRepository contatoTelefoneRepository;

    /**
     * @param telefoneEntrada bruto ou normalizado da mensagem WhatsApp
     */
    public OrigemAtendimentoTelefone resolver(Contato contato, String telefoneEntrada) {
        if (contato == null) {
            return null;
        }
        String norm = TicketAtivoService.normalizarTelefone(telefoneEntrada);
        if (norm == null) {
            return null;
        }
        String exibicao = telefoneEntrada != null && !telefoneEntrada.isBlank()
                ? telefoneEntrada.trim()
                : norm;
        String tipo = classificarTipo(contato, norm);
        return new OrigemAtendimentoTelefone(exibicao, norm, tipo);
    }

    public void aplicarOrigemNaInteracao(TicketInteracao interacao, Contato contato, String telefoneEntrada) {
        OrigemAtendimentoTelefone origem = resolver(contato, telefoneEntrada);
        if (interacao == null || origem == null) {
            return;
        }
        interacao.setTelefoneOrigem(origem.telefoneExibicao());
        interacao.setTelefoneOrigemNormalizado(origem.telefoneNormalizado());
        interacao.setTelefoneOrigemTipo(origem.tipo());
    }

    public void aplicarOrigemNoTicket(Ticket ticket, Contato contato, String telefoneEntrada) {
        OrigemAtendimentoTelefone origem = resolver(contato, telefoneEntrada);
        if (ticket == null || origem == null) {
            return;
        }
        ticket.setAtendimentoTelefone(origem.telefoneExibicao());
        ticket.setAtendimentoTelefoneNormalizado(origem.telefoneNormalizado());
        ticket.setAtendimentoTelefoneTipo(origem.tipo());
    }

    private String classificarTipo(Contato contato, String telefoneNorm) {
        if (telefoneNorm.equals(contato.getWhatsappNormalizado())) {
            return TIPO_PRINCIPAL;
        }
        if (contatoTelefoneRepository.existsByContato_IdAndTelefoneNormalizado(
                contato.getId(), telefoneNorm)) {
            return TIPO_ADICIONAL;
        }
        return TIPO_ADICIONAL;
    }

    public record OrigemAtendimentoTelefone(String telefoneExibicao, String telefoneNormalizado, String tipo) {}
}

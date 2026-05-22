package com.suporte.tickets.service;

import com.suporte.tickets.config.AppPublicUrlProperties;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoEnvioStatus;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import com.suporte.tickets.service.whatsapp.WhatsAppMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PesquisaSatisfacaoEnvioService {

    static final String MENSAGEM_TEMPLATE =
            "Ola! Sua opiniao e muito importante para melhorarmos nosso atendimento. "
                    + "Avalie o chamado %s: %s";

    private final AppPublicUrlProperties appPublicUrlProperties;
    private final WhatsAppMessageSender whatsAppMessageSender;
    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;
    private final AuditoriaService auditoriaService;

    /**
     * Tenta enviar link da pesquisa. Não propaga exceção (encerramento não deve falhar).
     *
     * @param tokenOpaco token em texto claro (não persistido)
     */
    @Transactional
    public void enviarPesquisa(TicketSatisfacao satisfacao, String tokenOpaco, Long analistaId) {
        if (satisfacao == null || satisfacao.getStatus() != TicketSatisfacaoStatus.PENDENTE) {
            return;
        }
        if (tokenOpaco == null || tokenOpaco.isBlank()) {
            registrarFalha(satisfacao, "Token de avaliacao indisponivel.");
            return;
        }

        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        satisfacao.setUltimaTentativaEnvioEm(agora);

        try {
            Ticket ticket = satisfacao.getTicket();
            Contato contato = ticket != null ? ticket.getContato() : null;
            String whatsapp = resolverWhatsappDestino(contato);
            if (whatsapp == null) {
                registrarFalha(satisfacao, "Contato sem WhatsApp para envio da pesquisa.");
                return;
            }

            String link = montarLinkAvaliacao(tokenOpaco);
            String protocolo = TicketSatisfacaoService.mascararProtocolo(ticket);
            String mensagem = montarMensagem(protocolo, link);

            boolean aceito = whatsAppMessageSender.enviar(whatsapp, mensagem);
            if (aceito) {
                satisfacao.setEnvioStatus(TicketSatisfacaoEnvioStatus.SIMULADO);
                satisfacao.setErroEnvio(null);
                ticketSatisfacaoRepository.save(satisfacao);
                auditoriaService.registrar(
                        AuditoriaService.ACAO_AVALIACAO_ENVIO_WHATSAPP,
                        AuditoriaService.ENTIDADE_TICKET,
                        ticket != null ? ticket.getNumeroTicket() : null,
                        "Pesquisa de satisfacao enfileirada (envio simulado). Protocolo: " + protocolo,
                        analistaId);
            } else {
                registrarFalha(satisfacao, "Provedor WhatsApp nao aceitou o envio.");
            }
        } catch (Exception e) {
            log.warn("Falha ao enviar pesquisa WhatsApp: {}", e.getMessage());
            registrarFalha(satisfacao, truncarErro(e.getMessage()));
        }
    }

    public String montarLinkAvaliacao(String tokenOpaco) {
        String base = normalizarBaseUrl(appPublicUrlProperties.getPublicBaseUrl());
        return UriComponentsBuilder.fromHttpUrl(base)
                .path("/")
                .queryParam("page", "avaliacao")
                .queryParam("token", tokenOpaco.trim())
                .build()
                .toUriString();
    }

    public static String montarMensagem(String protocoloMascarado, String link) {
        String protocolo = protocoloMascarado != null ? protocoloMascarado : "Chamado";
        String url = link != null ? link : "";
        return String.format(MENSAGEM_TEMPLATE, protocolo, url);
    }

    static String normalizarBaseUrl(String base) {
        if (base == null || base.isBlank()) {
            return "http://localhost:8080";
        }
        String trimmed = base.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private void registrarFalha(TicketSatisfacao satisfacao, String erro) {
        satisfacao.setEnvioStatus(TicketSatisfacaoEnvioStatus.FALHA);
        satisfacao.setErroEnvio(truncarErro(erro));
        ticketSatisfacaoRepository.save(satisfacao);
        Ticket ticket = satisfacao.getTicket();
        auditoriaService.registrar(
                AuditoriaService.ACAO_AVALIACAO_ENVIO_WHATSAPP,
                AuditoriaService.ENTIDADE_TICKET,
                ticket != null ? ticket.getNumeroTicket() : null,
                "Falha envio pesquisa: " + truncarErro(erro),
                satisfacao.getSolicitadaPorAnalistaId());
    }

    static String resolverWhatsappDestino(Contato contato) {
        if (contato == null) {
            return null;
        }
        String norm = contato.getWhatsappNormalizado();
        if (norm != null && !norm.isBlank()) {
            return norm.trim();
        }
        String wa = contato.getWhatsapp();
        if (wa == null || wa.isBlank()) {
            return null;
        }
        return wa.replaceAll("\\D", "");
    }

    private static String truncarErro(String msg) {
        if (msg == null) {
            return "Erro desconhecido";
        }
        String t = msg.trim();
        return t.length() > 500 ? t.substring(0, 500) : t;
    }
}

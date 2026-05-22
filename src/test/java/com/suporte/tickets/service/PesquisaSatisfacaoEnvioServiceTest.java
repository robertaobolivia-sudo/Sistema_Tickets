package com.suporte.tickets.service;

import com.suporte.tickets.config.AppPublicUrlProperties;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoEnvioStatus;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import com.suporte.tickets.service.whatsapp.WhatsAppMessageSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PesquisaSatisfacaoEnvioServiceTest {

    @Mock
    private AppPublicUrlProperties appPublicUrlProperties;
    @Mock
    private WhatsAppMessageSender whatsAppMessageSender;
    @Mock
    private TicketSatisfacaoRepository ticketSatisfacaoRepository;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PesquisaSatisfacaoEnvioService pesquisaSatisfacaoEnvioService;

    @Test
    void montarLink_removeBarraFinal() {
        when(appPublicUrlProperties.getPublicBaseUrl()).thenReturn("http://localhost:8080/");
        String link = pesquisaSatisfacaoEnvioService.montarLinkAvaliacao("tok123");
        assertTrue(link.startsWith("http://localhost:8080"));
        assertTrue(link.contains("page=avaliacao"));
        assertTrue(link.contains("token=tok123"));
    }

    @Test
    void montarMensagem_contemProtocoloELink() {
        String msg = PesquisaSatisfacaoEnvioService.montarMensagem("Chamado ••••1234", "http://x/link");
        assertTrue(msg.contains("Chamado ••••1234"));
        assertTrue(msg.contains("http://x/link"));
    }

    @Test
    void enviarPesquisa_pendenteComWhatsapp_simulado() {
        when(appPublicUrlProperties.getPublicBaseUrl()).thenReturn("http://localhost:8080");
        Ticket ticket = new Ticket();
        ticket.setNumeroTicket("TK-10");
        Contato c = new Contato();
        c.setWhatsappNormalizado("5511999999999");
        ticket.setContato(c);

        TicketSatisfacao s = new TicketSatisfacao();
        s.setStatus(TicketSatisfacaoStatus.PENDENTE);
        s.setTicket(ticket);
        s.setSolicitadaPorAnalistaId(1L);

        when(whatsAppMessageSender.enviar(eq("5511999999999"), anyString())).thenReturn(true);
        when(ticketSatisfacaoRepository.save(any(TicketSatisfacao.class))).thenAnswer(i -> i.getArgument(0));

        pesquisaSatisfacaoEnvioService.enviarPesquisa(s, "token-abc", 1L);

        assertEquals(TicketSatisfacaoEnvioStatus.SIMULADO, s.getEnvioStatus());
        verify(whatsAppMessageSender).enviar(eq("5511999999999"), anyString());
    }

    @Test
    void enviarPesquisa_semWhatsapp_falha() {
        Ticket ticket = new Ticket();
        ticket.setNumeroTicket("TK-11");
        Contato c = new Contato();
        c.setWhatsappNormalizado("");
        ticket.setContato(c);

        TicketSatisfacao s = new TicketSatisfacao();
        s.setStatus(TicketSatisfacaoStatus.PENDENTE);
        s.setTicket(ticket);

        when(ticketSatisfacaoRepository.save(any(TicketSatisfacao.class))).thenAnswer(i -> i.getArgument(0));

        pesquisaSatisfacaoEnvioService.enviarPesquisa(s, "tok", null);

        assertEquals(TicketSatisfacaoEnvioStatus.FALHA, s.getEnvioStatus());
        verify(whatsAppMessageSender, never()).enviar(anyString(), anyString());
    }

    @Test
    void normalizarBaseUrl() {
        assertEquals("http://host", PesquisaSatisfacaoEnvioService.normalizarBaseUrl("http://host///"));
    }
}

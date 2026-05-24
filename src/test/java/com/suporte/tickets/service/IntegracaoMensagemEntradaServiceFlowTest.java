package com.suporte.tickets.service;

import com.suporte.tickets.dto.IntegracaoMensagemEntradaResponseDTO;
import com.suporte.tickets.dto.IntegracaoWhatsappMensagemRequestDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegracaoMensagemEntradaServiceFlowTest {

    @Mock
    private TicketAtivoService ticketAtivoService;

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketInteracaoService ticketInteracaoService;

    @Mock
    private WhatsappMatrizService whatsappMatrizService;

    @Mock
    private ContatoService contatoService;

    @Mock
    private InteracaoPendenteDecisaoService interacaoPendenteDecisaoService;

    @InjectMocks
    private IntegracaoMensagemEntradaService service;

    @Test
    void reutilizaTicketAtivo_semCriarNovo() {
        Ticket ticket = new Ticket();
        ticket.setNumeroTicket("TK-100");
        ticket.setStatus(TicketStatus.EM_ATENDIMENTO);
        Cliente cliente = new Cliente();
        cliente.setId(1);
        ticket.setCliente(cliente);

        when(ticketAtivoService.buscarEntidadeAtivaAtendimentoWhatsapp(null, null, "11999990000"))
                .thenReturn(Optional.of(ticket));

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setTelefone("(11) 99999-0000");
        req.setMensagem("Oi");

        IntegracaoMensagemEntradaResponseDTO res = service.processarMensagemWhatsapp(req);

        assertFalse(res.isTicketCriado());
        assertEquals("TK-100", res.getNumeroTicket());
        assertTrue(res.isMensagemRegistrada());
        verify(ticketInteracaoService)
                .registrarMensagemEntradaExterna(eq(ticket), eq("Oi"), eq(null), eq("11999990000"));
        verify(ticketService, never()).criarTicketPorWebhook(any());
    }

    @Test
    void semTicketAtivo_criaNovo() {
        when(ticketAtivoService.buscarEntidadeAtivaAtendimentoWhatsapp(null, null, "11988887777"))
                .thenReturn(Optional.empty());

        TicketResponseDTO criado = new TicketResponseDTO();
        criado.setNumeroTicket("TK-200");
        criado.setStatus("ABERTO");
        when(ticketService.criarTicketPorWebhook(any())).thenReturn(criado);

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setTelefone("11988887777");
        req.setMensagem("Nova conversa");

        IntegracaoMensagemEntradaResponseDTO res = service.processarMensagemWhatsapp(req);

        assertTrue(res.isTicketCriado());
        assertEquals("TK-200", res.getNumeroTicket());
        verify(ticketService).criarTicketPorWebhook(any());
        verify(ticketInteracaoService, never()).registrarMensagemEntradaExterna(any(), any(), any(), any());
    }
}

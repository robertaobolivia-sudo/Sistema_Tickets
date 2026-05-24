package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.dto.IntegracaoWhatsappMensagemRequestDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.entity.WhatsappMatriz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sprint 211: antes de criar ticket, reconsulta ativo Cliente+Contato.
 */
@ExtendWith(MockitoExtension.class)
class IntegracaoMensagemRecheckAtivoTest {

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
    void recheckAntesDeCriar_reutilizaTicketAtivo() {
        Cliente cliente = new Cliente();
        cliente.setId(69);
        WhatsappMatriz matriz = new WhatsappMatriz();
        matriz.setId(2);
        matriz.setCliente(cliente);
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(2)).thenReturn(matriz);

        ContatoResponseDTO contatoDto = new ContatoResponseDTO();
        contatoDto.setId(8);
        when(contatoService.criarSeNaoExistir(eq(69), eq("5511963978963"), any())).thenReturn(contatoDto);
        Contato contato = new Contato();
        contato.setId(8);
        when(contatoService.buscarEntidade(8)).thenReturn(contato);

        Ticket ativo = new Ticket();
        ativo.setNumeroTicket("TK-000086");
        ativo.setStatus(TicketStatus.ABERTO);
        when(ticketAtivoService.buscarEntidadeAtivaAtendimentoWhatsapp(eq(69), eq(8), eq("5511963978963")))
                .thenReturn(Optional.of(ativo));

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setWhatsappMatrizId(2);
        req.setTelefone("5511963978963");
        req.setMensagem("msg recheck");

        var res = service.processarMensagemWhatsapp(req);

        assertFalse(res.isTicketCriado());
        verify(ticketService, never()).criarTicketPorWebhook(any());
        verify(ticketInteracaoService).registrarMensagemEntradaExterna(eq(ativo), eq("msg recheck"), any(), any());
    }
}

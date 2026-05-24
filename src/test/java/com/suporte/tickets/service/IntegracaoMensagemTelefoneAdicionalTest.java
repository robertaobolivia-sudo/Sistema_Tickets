package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.dto.IntegracaoMensagemEntradaResponseDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sprint 288: mensagem em telefone adicional resolve o mesmo contato_id (via criarSeNaoExistir).
 */
@ExtendWith(MockitoExtension.class)
class IntegracaoMensagemTelefoneAdicionalTest {

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
    void telefoneAdicional_reutilizaTicketDoContatoPrincipal() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        WhatsappMatriz matriz = new WhatsappMatriz();
        matriz.setId(2);
        matriz.setCliente(cliente);
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(2)).thenReturn(matriz);

        ContatoResponseDTO dto = new ContatoResponseDTO();
        dto.setId(100);
        when(contatoService.criarSeNaoExistir(eq(10), eq("5522222222222"), eq("Maria")))
                .thenReturn(dto);
        Contato contato = new Contato();
        contato.setId(100);
        when(contatoService.buscarEntidade(100)).thenReturn(contato);

        Ticket ativo = new Ticket();
        ativo.setNumeroTicket("TK-UNICO");
        ativo.setStatus(TicketStatus.EM_ATENDIMENTO);
        when(ticketAtivoService.buscarEntidadeAtivaAtendimentoWhatsapp(eq(10), eq(100), eq("5522222222222")))
                .thenReturn(Optional.of(ativo));

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setWhatsappMatrizId(2);
        req.setTelefone("5522222222222");
        req.setNomeContato("Maria");
        req.setMensagem("Msg pelo segundo numero");

        IntegracaoMensagemEntradaResponseDTO res = service.processarMensagemWhatsapp(req);

        assertFalse(res.isTicketCriado());
        assertEquals("TK-UNICO", res.getNumeroTicket());
        verify(ticketInteracaoService)
                .registrarMensagemEntradaExterna(eq(ativo), eq("Msg pelo segundo numero"), eq(null), eq("5522222222222"));
        verify(contatoService).criarSeNaoExistir(eq(10), eq("5522222222222"), eq("Maria"));
    }
}

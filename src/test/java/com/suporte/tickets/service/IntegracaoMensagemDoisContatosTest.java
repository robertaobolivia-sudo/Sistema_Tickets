package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.dto.IntegracaoMensagemEntradaResponseDTO;
import com.suporte.tickets.dto.IntegracaoWhatsappMensagemRequestDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.entity.WhatsappMatriz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sprint 206: segundo Contato no mesmo Cliente nao reutiliza ticket do primeiro.
 */
@ExtendWith(MockitoExtension.class)
class IntegracaoMensagemDoisContatosTest {

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
    @Mock
    private ContatoEtiquetaService contatoEtiquetaService;

    @InjectMocks
    private IntegracaoMensagemEntradaService service;

    @Test
    void contatoDiferente_criaSegundoTicket() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        cliente.setNome("Cliente X");
        WhatsappMatriz matriz = new WhatsappMatriz();
        matriz.setId(2);
        matriz.setCliente(cliente);
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(2)).thenReturn(matriz);

        ContatoResponseDTO dtoB = new ContatoResponseDTO();
        dtoB.setId(200);
        when(contatoService.criarSeNaoExistir(eq(10), eq("5511999000002"), any())).thenReturn(dtoB);
        Contato contatoB = new Contato();
        contatoB.setId(200);
        when(contatoService.buscarEntidade(200)).thenReturn(contatoB);

        when(ticketAtivoService.buscarEntidadeAtivaAtendimentoWhatsapp(eq(10), eq(200), eq("5511999000002")))
                .thenReturn(Optional.empty());
        when(contatoEtiquetaService.resolverClassificacaoOperacional(contatoB)).thenReturn(null);
        when(ticketAtivoService.buscarUltimoEncerradoPorClienteEContato(10, 200)).thenReturn(Optional.empty());

        TicketResponseDTO criado = new TicketResponseDTO();
        criado.setNumeroTicket("TK-B");
        criado.setStatus("ABERTO");
        when(ticketService.criarTicketPorWebhook(any())).thenReturn(criado);

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setWhatsappMatrizId(2);
        req.setTelefone("5511999000002");
        req.setNomeContato("Contato B");
        req.setMensagem("Oi contato B");

        IntegracaoMensagemEntradaResponseDTO res = service.processarMensagemWhatsapp(req);

        assertTrue(res.isTicketCriado());
        assertEquals("TK-B", res.getNumeroTicket());
        verify(ticketService, times(1)).criarTicketPorWebhook(any());
        verify(ticketInteracaoService, never()).registrarMensagemEntradaExterna(any(), any(), any(), any());
    }

    @Test
    void mesmoContato_reutilizaTicketA() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        WhatsappMatriz matriz = new WhatsappMatriz();
        matriz.setId(2);
        matriz.setCliente(cliente);
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(2)).thenReturn(matriz);

        ContatoResponseDTO dtoA = new ContatoResponseDTO();
        dtoA.setId(100);
        when(contatoService.criarSeNaoExistir(eq(10), eq("5511999000001"), any())).thenReturn(dtoA);
        Contato contatoA = new Contato();
        contatoA.setId(100);
        when(contatoService.buscarEntidade(100)).thenReturn(contatoA);

        Ticket ativo = new Ticket();
        ativo.setNumeroTicket("TK-A");
        ativo.setStatus(TicketStatus.ABERTO);
        ativo.setCliente(cliente);
        when(ticketAtivoService.buscarEntidadeAtivaAtendimentoWhatsapp(eq(10), eq(100), eq("5511999000001")))
                .thenReturn(Optional.of(ativo));

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setWhatsappMatrizId(2);
        req.setTelefone("5511999000001");
        req.setMensagem("Segunda msg");

        IntegracaoMensagemEntradaResponseDTO res = service.processarMensagemWhatsapp(req);

        assertEquals(false, res.isTicketCriado());
        assertEquals("TK-A", res.getNumeroTicket());
        verify(ticketInteracaoService).registrarMensagemEntradaExterna(eq(ativo), eq("Segunda msg"), eq(null), any());
    }
}

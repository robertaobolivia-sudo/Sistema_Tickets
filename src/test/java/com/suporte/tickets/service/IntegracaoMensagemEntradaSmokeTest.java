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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Smoke técnico automatizado do fluxo entrada WhatsApp (Sprint 204).
 */
@ExtendWith(MockitoExtension.class)
class IntegracaoMensagemEntradaSmokeTest {

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

    private Cliente clienteContratante() {
        Cliente c = new Cliente();
        c.setId(50);
        c.setNome("Fenix Teste");
        return c;
    }

    private WhatsappMatriz matrizAtiva() {
        WhatsappMatriz m = new WhatsappMatriz();
        m.setId(3);
        m.setCliente(clienteContratante());
        m.setAtivo(true);
        m.setNumero("5511888000000");
        m.setNumeroNormalizado("5511888000000");
        return m;
    }

    private void stubContatoNovo(int contatoId) {
        ContatoResponseDTO dto = new ContatoResponseDTO();
        dto.setId(contatoId);
        when(contatoService.criarSeNaoExistir(eq(50), any(), any())).thenReturn(dto);
        Contato ent = new Contato();
        ent.setId(contatoId);
        when(contatoService.buscarEntidade(contatoId)).thenReturn(ent);
        when(ticketAtivoService.buscarEntidadeAtiva(eq(50), eq(contatoId), eq(null), any()))
                .thenReturn(Optional.empty());
        when(ticketAtivoService.buscarUltimoEncerradoPorClienteEContato(50, contatoId))
                .thenReturn(Optional.empty());
    }

    @Test
    void cenarioA_whatsappMatrizId_contatoNovo_criaTicket() {
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(3)).thenReturn(matrizAtiva());
        stubContatoNovo(701);
        TicketResponseDTO criado = new TicketResponseDTO();
        criado.setNumeroTicket("TK-S204-A");
        criado.setStatus("ABERTO");
        criado.setClienteId(50);
        criado.setContatoId(701);
        criado.setWhatsappMatrizId(3);
        when(ticketService.criarTicketPorWebhook(any())).thenReturn(criado);

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setWhatsappMatrizId(3);
        req.setTelefone("5511999002041");
        req.setNomeContato("Smoke Contato Novo");
        req.setMensagem("Mensagem smoke 204 cenário A");

        IntegracaoMensagemEntradaResponseDTO res = service.processarMensagemWhatsapp(req);

        assertTrue(res.isTicketCriado());
        assertEquals("TK-S204-A", res.getNumeroTicket());
        assertTrue(res.isMensagemRegistrada());

        ArgumentCaptor<TicketWebhookRequestDTO> cap = ArgumentCaptor.forClass(TicketWebhookRequestDTO.class);
        verify(ticketService).criarTicketPorWebhook(cap.capture());
        assertEquals(50, cap.getValue().getClienteContratanteId());
        assertEquals(3, cap.getValue().getWhatsappMatrizId());
        assertEquals("5511999002041", cap.getValue().getTelefone());
    }

    @Test
    void cenarioB_mesmoContato_reutilizaTicketAtivo() {
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(3)).thenReturn(matrizAtiva());
        ContatoResponseDTO dto = new ContatoResponseDTO();
        dto.setId(702);
        when(contatoService.criarSeNaoExistir(eq(50), eq("5511999002042"), eq("Smoke Contato Existente")))
                .thenReturn(dto);
        Contato ent = new Contato();
        ent.setId(702);
        when(contatoService.buscarEntidade(702)).thenReturn(ent);
        Ticket ativo = new Ticket();
        ativo.setNumeroTicket("TK-S204-B");
        ativo.setStatus(TicketStatus.ABERTO);
        ativo.setCliente(clienteContratante());
        when(ticketAtivoService.buscarEntidadeAtiva(eq(50), eq(702), eq(null), eq("5511999002042")))
                .thenReturn(Optional.of(ativo));

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setWhatsappMatrizId(3);
        req.setTelefone("5511999002042");
        req.setNomeContato("Smoke Contato Existente");
        req.setMensagem("Segunda mensagem smoke 204");

        IntegracaoMensagemEntradaResponseDTO res = service.processarMensagemWhatsapp(req);

        assertFalse(res.isTicketCriado());
        assertEquals("TK-S204-B", res.getNumeroTicket());
        verify(ticketInteracaoService).registrarMensagemEntradaExterna(eq(ativo), eq("Segunda mensagem smoke 204"), eq(null));
        verify(ticketService, times(0)).criarTicketPorWebhook(any());
        verify(contatoService, times(1)).criarSeNaoExistir(eq(50), eq("5511999002042"), eq("Smoke Contato Existente"));
    }

    @Test
    void cenarioC_numeroMatriz_resolveCliente() {
        when(whatsappMatrizService.resolverMatrizAtivaPorNumero("5511888000000")).thenReturn(matrizAtiva());
        stubContatoNovo(703);
        TicketResponseDTO criado = new TicketResponseDTO();
        criado.setNumeroTicket("TK-S204-C");
        criado.setStatus("ABERTO");
        when(ticketService.criarTicketPorWebhook(any())).thenReturn(criado);

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setNumeroMatriz("5511888000000");
        req.setTelefone("5511999002043");
        req.setNomeContato("Smoke Numero Matriz");
        req.setMensagem("Cenário C numeroMatriz");

        IntegracaoMensagemEntradaResponseDTO res = service.processarMensagemWhatsapp(req);

        assertTrue(res.isTicketCriado());
        verify(whatsappMatrizService).resolverMatrizAtivaPorNumero("5511888000000");
        verify(whatsappMatrizService, times(0)).buscarEntidadeAtivaPorId(any());
    }
}

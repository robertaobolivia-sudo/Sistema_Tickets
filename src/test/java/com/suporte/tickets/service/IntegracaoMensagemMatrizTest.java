package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.dto.IntegracaoWhatsappMensagemRequestDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.WhatsappMatriz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegracaoMensagemMatrizTest {

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
    void comWhatsappMatrizId_usaClienteContratante() {
        Cliente contratante = new Cliente();
        contratante.setId(50);
        contratante.setNome("Fenix");
        contratante.setEmail("f@x.com");
        contratante.setTelefone("1100000000");
        contratante.setTelefoneContato("1100000001");

        WhatsappMatriz matriz = new WhatsappMatriz();
        matriz.setId(3);
        matriz.setCliente(contratante);
        matriz.setAtivo(true);
        matriz.setNumero("5511888000000");
        matriz.setNumeroNormalizado("5511888000000");

        when(whatsappMatrizService.buscarEntidadeAtivaPorId(3)).thenReturn(matriz);
        ContatoResponseDTO contatoDto = new ContatoResponseDTO();
        contatoDto.setId(7);
        when(contatoService.criarSeNaoExistir(eq(50), eq("11999991111"), eq("Maria"))).thenReturn(contatoDto);
        Contato contato = new Contato();
        contato.setId(7);
        when(contatoService.buscarEntidade(7)).thenReturn(contato);
        when(ticketAtivoService.buscarEntidadeAtiva(eq(50), eq(7), eq(null), eq("11999991111")))
                .thenReturn(Optional.empty());
        when(ticketAtivoService.buscarUltimoEncerradoPorClienteEContato(50, 7)).thenReturn(Optional.empty());

        TicketResponseDTO criado = new TicketResponseDTO();
        criado.setNumeroTicket("TK-300");
        criado.setStatus("ABERTO");
        criado.setContatoId(7);
        when(ticketService.criarTicketPorWebhook(any())).thenReturn(criado);

        IntegracaoWhatsappMensagemRequestDTO req = new IntegracaoWhatsappMensagemRequestDTO();
        req.setWhatsappMatrizId(3);
        req.setTelefone("11999991111");
        req.setNomeContato("Maria");
        req.setMensagem("Oi matriz");

        service.processarMensagemWhatsapp(req);

        ArgumentCaptor<TicketWebhookRequestDTO> cap = ArgumentCaptor.forClass(TicketWebhookRequestDTO.class);
        verify(ticketService).criarTicketPorWebhook(cap.capture());
        assertEquals(50, cap.getValue().getClienteContratanteId());
        assertEquals(3, cap.getValue().getWhatsappMatrizId());
    }
}

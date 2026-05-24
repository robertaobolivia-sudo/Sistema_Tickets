package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import com.suporte.tickets.repository.SubgrupoCategoriaRepository;
import com.suporte.tickets.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceVinculoContatoF8Test {

    @Mock private TicketRepository ticketRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private GrupoCategoriaRepository grupoCategoriaRepository;
    @Mock private SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    @Mock private TicketInteracaoService ticketInteracaoService;
    @Mock private AnalistaService analistaService;
    @Mock private ContatoService contatoService;
    @Mock private WhatsappMatrizService whatsappMatrizService;
    @Mock private TicketSlaPrimeiroAtendimentoService ticketSlaPrimeiroAtendimentoService;
    @Mock private TicketSlaResolucaoService ticketSlaResolucaoService;
    @Mock private TicketSlaPausaService ticketSlaPausaService;
    @Mock private NotificacaoInternaService notificacaoInternaService;
    @Mock private TicketSatisfacaoService ticketSatisfacaoService;
    @Mock private PesquisaSatisfacaoEnvioService pesquisaSatisfacaoEnvioService;
    @Mock private MotivoService motivoService;
    @Mock private ContatoAtendimentoOrigemService contatoAtendimentoOrigemService;

    @InjectMocks
    private TicketService ticketService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(10);
        cliente.setNome("Cliente A");
        cliente.setTelefone("5511999000000");
    }

    @Test
    void webhookSemContatoWhatsapp_rejeita() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setMensagem("Legado");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> ticketService.criarTicketPorWebhook(dto));
        assertTrue(ex.getMessage().contains("contatoWhatsappId"));
        verify(contatoService, never()).criarSeNaoExistir(any(), any(), any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void contatoWhatsappId_preencheTicketContato_semCriarPorSolicitante() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(2);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(2);
            return t;
        });

        Contato contato = new Contato();
        contato.setId(200);
        contato.setCliente(cliente);
        when(contatoService.buscarEntidade(200)).thenReturn(contato);
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(200), eq(List.copyOf(TicketAtivoService.STATUS_ATIVOS))))
                .thenReturn(Optional.empty());

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setContatoWhatsappId(200);
        dto.setMensagem("Manual");

        ticketService.criarTicketPorWebhook(dto);

        ArgumentCaptor<Ticket> cap = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(cap.capture());
        assertTrue(cap.getValue().getContato() != null);
        assertTrue(cap.getValue().getContato().getId().equals(200));
        verify(contatoService, never()).criarSeNaoExistir(any(), any(), any());
    }
}

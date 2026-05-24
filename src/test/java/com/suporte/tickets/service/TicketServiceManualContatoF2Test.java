package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoResponseDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sprint F2: abertura manual alinhada ao par Cliente + Contato WhatsApp.
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceManualContatoF2Test {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private GrupoCategoriaRepository grupoCategoriaRepository;
    @Mock
    private SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    @Mock
    private TicketInteracaoService ticketInteracaoService;
    @Mock
    private AnalistaService analistaService;
    @Mock
    private ContatoService contatoService;
    @Mock
    private WhatsappMatrizService whatsappMatrizService;
    @Mock
    private TicketSlaPrimeiroAtendimentoService ticketSlaPrimeiroAtendimentoService;
    @Mock
    private TicketSlaResolucaoService ticketSlaResolucaoService;
    @Mock
    private TicketSlaPausaService ticketSlaPausaService;
    @Mock
    private NotificacaoInternaService notificacaoInternaService;
    @Mock
    private TicketSatisfacaoService ticketSatisfacaoService;
    @Mock
    private PesquisaSatisfacaoEnvioService pesquisaSatisfacaoEnvioService;
    @Mock
    private MotivoService motivoService;
    @Mock
    private ContatoAtendimentoOrigemService contatoAtendimentoOrigemService;

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
    void contatoWhatsappExplicito_gravaContatoNoTicket() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(1);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1);
            return t;
        });

        Contato contato2 = new Contato();
        contato2.setId(200);
        contato2.setNome("Contato 2");
        contato2.setCliente(cliente);
        when(contatoService.buscarEntidade(200)).thenReturn(contato2);
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(200), eq(List.copyOf(TicketAtivoService.STATUS_ATIVOS))))
                .thenReturn(Optional.empty());

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setCliente("Cliente A");
        dto.setClienteContratanteId(10);
        dto.setContatoWhatsappId(200);
        dto.setMensagem("Abertura manual");

        ticketService.criarTicketPorWebhook(dto);

        ArgumentCaptor<Ticket> cap = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(cap.capture());
        assertEquals(200, cap.getValue().getContato().getId());
        verify(contatoService, never()).criarSeNaoExistir(any(), any(), any());
    }

    @Test
    void contatoWhatsapp2_consultaAtivoSoDoPar() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(2);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(2);
            return t;
        });

        Contato contato2 = new Contato();
        contato2.setId(200);
        contato2.setCliente(cliente);
        when(contatoService.buscarEntidade(200)).thenReturn(contato2);
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(200), eq(List.copyOf(TicketAtivoService.STATUS_ATIVOS))))
                .thenReturn(Optional.empty());

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setCliente("Cliente A");
        dto.setContatoWhatsappId(200);
        dto.setMensagem("Novo");

        ticketService.criarTicketPorWebhook(dto);

        verify(ticketRepository).save(any());
        verify(ticketRepository, never()).findFirstByCliente_IdAndStatusInOrderByDataAberturaDesc(any(), any());
    }

    @Test
    void aberturaManualSemContatoWhatsapp_bloqueia_F6() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setCliente("Cliente A");
        dto.setTelefone("5511999000000");
        dto.setMensagem("Manual sem contatoWhatsappId");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> ticketService.criarTicketPorWebhook(dto));
        assertTrue(ex.getMessage().contains("contatoWhatsappId"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void contatoWhatsappAtivoJaExiste_bloqueiaCriacao() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));

        Contato contato1 = new Contato();
        contato1.setId(100);
        contato1.setCliente(cliente);
        when(contatoService.buscarEntidade(100)).thenReturn(contato1);

        Ticket ativo = new Ticket();
        ativo.setNumeroTicket("TK-ABERTO");
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(100), eq(List.copyOf(TicketAtivoService.STATUS_ATIVOS))))
                .thenReturn(Optional.of(ativo));

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setCliente("Cliente A");
        dto.setContatoWhatsappId(100);
        dto.setMensagem("Duplicar");

        assertThrows(IllegalArgumentException.class, () -> ticketService.criarTicketPorWebhook(dto));
        verify(ticketRepository, never()).save(any());
    }
}

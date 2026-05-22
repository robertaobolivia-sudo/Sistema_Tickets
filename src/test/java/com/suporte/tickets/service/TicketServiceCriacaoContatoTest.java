package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.repository.CarteiraRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceCriacaoContatoTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private CarteiraRepository carteiraRepository;
    @Mock
    private GrupoCategoriaRepository grupoCategoriaRepository;
    @Mock
    private SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    @Mock
    private TicketInteracaoService ticketInteracaoService;
    @Mock
    private AnalistaService analistaService;
    @Mock
    private ContatoClienteService contatoClienteService;
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
    @InjectMocks
    private TicketService ticketService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(10);
        cliente.setNome("Contratante X");
        cliente.setEmail("c@x.com");
        cliente.setTelefone("1100000000");
        cliente.setTelefoneContato("1100000001");
    }

    @Test
    void criarTicketPorWebhook_comTelefone_vinculaContato() {
        when(clienteRepository.findByTelefone("11987654321")).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(99);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1);
            return t;
        });

        ContatoResponseDTO contatoDto = new ContatoResponseDTO();
        contatoDto.setId(50);
        contatoDto.setNome("Maria");
        contatoDto.setWhatsapp("11987654321");
        when(contatoService.criarSeNaoExistir(eq(10), eq("11987654321"), any())).thenReturn(contatoDto);

        Contato contatoEnt = new Contato();
        contatoEnt.setId(50);
        contatoEnt.setNome("Maria");
        contatoEnt.setWhatsapp("11987654321");
        contatoEnt.setWhatsappNormalizado("11987654321");
        contatoEnt.setCliente(cliente);
        when(contatoService.buscarEntidade(50)).thenReturn(contatoEnt);
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(50), eq(List.copyOf(TicketAtivoService.STATUS_ATIVOS))))
                .thenReturn(Optional.empty());

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setCliente("Maria");
        dto.setTelefone("11987654321");
        dto.setMensagem("Oi");
        dto.setNomeContato("Maria");

        TicketResponseDTO res = ticketService.criarTicketPorWebhook(dto);

        assertEquals(50, res.getContatoId());
        assertEquals("Maria", res.getContatoNome());
        ArgumentCaptor<Ticket> cap = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(cap.capture());
        assertNotNull(cap.getValue().getContato());
        assertEquals(50, cap.getValue().getContato().getId());
    }

    @Test
    void criarTicketPorWebhook_semTelefone_naoVinculaContato() {
        cliente.setTelefone(null);
        cliente.setTelefoneContato(null);
        when(clienteRepository.findByNome("Sem Tel")).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(1);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(2);
            return t;
        });

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setCliente("Sem Tel");
        dto.setMensagem("Msg");

        ticketService.criarTicketPorWebhook(dto);

        verify(contatoService, never()).criarSeNaoExistir(any(), any(), any());
        ArgumentCaptor<Ticket> cap = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(cap.capture());
        assertNull(cap.getValue().getContato());
    }
}

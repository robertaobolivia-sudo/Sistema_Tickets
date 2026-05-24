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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sprint F14/F15: limpeza de fallbacks legados.
 * Garante que cliente.telefone nao e usado como origem do atendimento
 * e que nome do contato nao cai para nome do cliente contratante.
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceF14F15LimpezaTest {

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
        cliente.setNome("Contratante LTDA");
        cliente.setTelefone("5511111110000");
        cliente.setTelefoneContato("5511111110001");
    }

    /**
     * F14: receptivo WhatsApp sem nomeContato no payload nao usa nome do cliente contratante.
     * criarSeNaoExistir deve receber nome null, nao "Contratante LTDA".
     */
    @Test
    void receptivo_semNomeContato_naoCaiFallbackNomeCliente() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(1);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1);
            return t;
        });
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(5))
                .thenReturn(new com.suporte.tickets.entity.WhatsappMatriz());

        ContatoResponseDTO cr = new ContatoResponseDTO();
        cr.setId(50);
        when(contatoService.criarSeNaoExistir(eq(10), eq("5511999000001"), isNull()))
                .thenReturn(cr);
        Contato contatoEnt = new Contato();
        contatoEnt.setId(50);
        contatoEnt.setCliente(cliente);
        when(contatoService.buscarEntidade(50)).thenReturn(contatoEnt);
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(50), eq(List.copyOf(TicketAtivoService.STATUS_ATIVOS))))
                .thenReturn(Optional.empty());

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setTelefone("5511999000001");
        dto.setMensagem("Oi");
        dto.setWhatsappMatrizId(5);
        // nomeContato nao informado — antes caia para cliente.getNome()

        ticketService.criarTicketPorWebhook(dto);

        verify(contatoService).criarSeNaoExistir(eq(10), eq("5511999000001"), isNull());
    }

    /**
     * F14: receptivo WhatsApp com whatsappMatrizId mas sem telefone nao cria Contato
     * e portanto nao chama aplicarOrigemNoTicket. Garante que cliente.telefone
     * nao e usado como fallback para criar Contato nem para origem.
     */
    @Test
    void receptivo_comMatrizSemTelefone_naoVinculaContatoNemChegaAOrigem() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(2);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(2);
            return t;
        });
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(5))
                .thenReturn(new com.suporte.tickets.entity.WhatsappMatriz());

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setWhatsappMatrizId(5);
        dto.setMensagem("Sem telefone");
        // telefone ausente: telefoneBrutoMensagemWhatsapp retorna null
        // => nao cria Contato => nao chama aplicarOrigemNoTicket

        ticketService.criarTicketPorWebhook(dto);

        verify(contatoService, never()).criarSeNaoExistir(any(), any(), any());
        verify(contatoAtendimentoOrigemService, never()).aplicarOrigemNoTicket(any(), any(), any());
    }

    /**
     * F14: abertura manual com nomeContato explícito usa esse nome, nao nome do cliente.
     */
    @Test
    void manual_comNomeContato_usaNomeContato() {
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(3);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(3);
            return t;
        });

        Contato contato = new Contato();
        contato.setId(200);
        contato.setCliente(cliente);
        contato.setWhatsapp("5511999000002");
        when(contatoService.buscarEntidade(200)).thenReturn(contato);
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(200), eq(List.copyOf(TicketAtivoService.STATUS_ATIVOS))))
                .thenReturn(Optional.empty());

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setContatoWhatsappId(200);
        dto.setTelefone("5511999000002");
        dto.setNomeContato("Carlos Souza");
        dto.setMensagem("Manual");

        ticketService.criarTicketPorWebhook(dto);

        // origem = telefone do Contato (dto.telefone), nao cliente.telefone
        verify(contatoAtendimentoOrigemService).aplicarOrigemNoTicket(
                any(), eq(contato), eq("5511999000002"));
    }
}

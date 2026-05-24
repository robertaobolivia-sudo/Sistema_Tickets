package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketClassificacaoOperacional;
import com.suporte.tickets.entity.TicketOrigem;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.entity.WhatsappMatriz;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import com.suporte.tickets.repository.SubgrupoCategoriaRepository;
import com.suporte.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketOrigemCriacaoF16Test {

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

    @Test
    void aberturaManual_gravaAtivoManual() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(1);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1);
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
        org.mockito.Mockito.verify(ticketRepository).save(cap.capture());
        assertEquals(TicketOrigem.ATIVO_MANUAL, cap.getValue().getOrigemTicket());
    }

    @Test
    void receptivoWhatsapp_gravaReceptivoWhatsapp() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(2);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(2);
            return t;
        });
        WhatsappMatriz matriz = new WhatsappMatriz();
        matriz.setId(5);
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(5)).thenReturn(matriz);
        Contato contato = new Contato();
        contato.setId(100);
        contato.setCliente(cliente);
        when(contatoService.buscarEntidade(100)).thenReturn(contato);
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(100), eq(List.copyOf(TicketAtivoService.STATUS_ATIVOS))))
                .thenReturn(Optional.empty());

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setContatoWhatsappId(100);
        dto.setWhatsappMatrizId(5);
        dto.setTelefone("5511999000001");
        dto.setMensagem("Entrada");

        ticketService.criarTicketPorWebhook(dto);

        ArgumentCaptor<Ticket> cap = ArgumentCaptor.forClass(Ticket.class);
        org.mockito.Mockito.verify(ticketRepository).save(cap.capture());
        assertEquals(TicketOrigem.RECEPTIVO_WHATSAPP, cap.getValue().getOrigemTicket());
    }

    @Test
    void indevidoReceptivo_mantemOrigemReceptivo() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
        when(ticketRepository.getNextSequence()).thenReturn(3);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(3);
            return t;
        });
        WhatsappMatriz matriz = new WhatsappMatriz();
        matriz.setId(5);
        when(whatsappMatrizService.buscarEntidadeAtivaPorId(5)).thenReturn(matriz);
        Contato contato = new Contato();
        contato.setId(100);
        contato.setCliente(cliente);
        when(contatoService.buscarEntidade(100)).thenReturn(contato);

        TicketWebhookRequestDTO dto = new TicketWebhookRequestDTO();
        dto.setClienteContratanteId(10);
        dto.setContatoWhatsappId(100);
        dto.setWhatsappMatrizId(5);
        dto.setTelefone("5511999000001");
        dto.setMensagem("Indevido");

        TicketResponseDTO res = ticketService.criarTicketEntradaOperacional(
                dto, TicketClassificacaoOperacional.CONTATO_PESSOAL);

        assertEquals(TicketStatus.INDEVIDO.name(), res.getStatus());
        ArgumentCaptor<Ticket> cap = ArgumentCaptor.forClass(Ticket.class);
        org.mockito.Mockito.verify(ticketRepository).save(cap.capture());
        assertEquals(TicketOrigem.RECEPTIVO_WHATSAPP, cap.getValue().getOrigemTicket());
    }

    @Test
    void resolverOrigemNaCriacao_regras() {
        TicketWebhookRequestDTO manual = new TicketWebhookRequestDTO();
        manual.setClienteContratanteId(1);
        assertEquals(TicketOrigem.ATIVO_MANUAL, TicketOrigemResolver.resolverOrigemNaCriacao(manual));

        TicketWebhookRequestDTO receptivo = new TicketWebhookRequestDTO();
        receptivo.setWhatsappMatrizId(5);
        assertEquals(TicketOrigem.RECEPTIVO_WHATSAPP, TicketOrigemResolver.resolverOrigemNaCriacao(receptivo));
    }
}

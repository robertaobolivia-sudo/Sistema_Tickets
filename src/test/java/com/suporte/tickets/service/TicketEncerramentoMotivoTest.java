package com.suporte.tickets.service;

import com.suporte.tickets.dto.EncerrarTicketRequestDTO;
import com.suporte.tickets.entity.GrupoCategoria;
import com.suporte.tickets.entity.Motivo;
import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import com.suporte.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketEncerramentoMotivoTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private GrupoCategoriaRepository grupoCategoriaRepository;
    @Mock
    private com.suporte.tickets.repository.SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    @Mock
    private MotivoService motivoService;
    @Mock
    private TicketSlaPausaService ticketSlaPausaService;
    @Mock
    private TicketSlaResolucaoService ticketSlaResolucaoService;
    @Mock
    private TicketInteracaoService ticketInteracaoService;
    @Mock
    private com.suporte.tickets.repository.ClienteRepository clienteRepository;
    @Mock
    private AnalistaService analistaService;
    @Mock
    private ContatoService contatoService;
    @Mock
    private WhatsappMatrizService whatsappMatrizService;
    @Mock
    private TicketSlaPrimeiroAtendimentoService ticketSlaPrimeiroAtendimentoService;
    @Mock
    private NotificacaoInternaService notificacaoInternaService;
    @Mock
    private TicketSatisfacaoService ticketSatisfacaoService;
    @Mock
    private PesquisaSatisfacaoEnvioService pesquisaSatisfacaoEnvioService;
    @Mock
    private TicketStatusTransicaoService ticketStatusTransicaoService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void encerrar_exigeMotivo() {
        Ticket ticket = ticket("TK-1");
        when(ticketRepository.findByNumeroTicket("TK-1")).thenReturn(Optional.of(ticket));
        GrupoCategoria grupo = grupo(1L);
        SubgrupoCategoria sub = subgrupo(2L, grupo);
        when(grupoCategoriaRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(subgrupoCategoriaRepository.findById(2L)).thenReturn(Optional.of(sub));

        EncerrarTicketRequestDTO dto = new EncerrarTicketRequestDTO();
        dto.setGrupoId(1L);
        dto.setSubgrupoId(2L);
        dto.setMotivoId(null);
        dto.setComentarioEncerramento("ok");

        assertThrows(IllegalArgumentException.class, () -> ticketService.encerrarTicket("TK-1", dto));
    }

    @Test
    void encerrar_rejeitaMotivoDeOutroSubgrupo() {
        Ticket ticket = ticket("TK-2");
        when(ticketRepository.findByNumeroTicket("TK-2")).thenReturn(Optional.of(ticket));
        GrupoCategoria grupo = grupo(1L);
        SubgrupoCategoria sub = subgrupo(2L, grupo);
        SubgrupoCategoria outroSub = subgrupo(99L, grupo);
        when(grupoCategoriaRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(subgrupoCategoriaRepository.findById(2L)).thenReturn(Optional.of(sub));

        Motivo motivo = new Motivo();
        motivo.setId(5L);
        motivo.setAtivo(true);
        motivo.setSubgrupoCategoria(outroSub);
        when(motivoService.buscarEntidadeAtiva(5L)).thenReturn(motivo);

        EncerrarTicketRequestDTO dto = new EncerrarTicketRequestDTO();
        dto.setGrupoId(1L);
        dto.setSubgrupoId(2L);
        dto.setMotivoId(5L);
        dto.setComentarioEncerramento("fim");

        assertThrows(IllegalArgumentException.class, () -> ticketService.encerrarTicket("TK-2", dto));
    }

    @Test
    void encerrar_comMotivoValido() {
        Ticket ticket = ticket("TK-3");
        when(ticketRepository.findByNumeroTicket("TK-3")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            if (t.getId() == null) {
                t.setId(1);
            }
            return t;
        });

        GrupoCategoria grupo = grupo(1L);
        SubgrupoCategoria sub = subgrupo(2L, grupo);
        when(grupoCategoriaRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(subgrupoCategoriaRepository.findById(2L)).thenReturn(Optional.of(sub));

        Motivo motivo = new Motivo();
        motivo.setId(7L);
        motivo.setNome("Configuracao");
        motivo.setAtivo(true);
        motivo.setSubgrupoCategoria(sub);
        when(motivoService.buscarEntidadeAtiva(7L)).thenReturn(motivo);
        when(ticketStatusTransicaoService.isStatusAtivoOperacional(TicketStatus.EM_ATENDIMENTO)).thenReturn(true);

        EncerrarTicketRequestDTO dto = new EncerrarTicketRequestDTO();
        dto.setGrupoId(1L);
        dto.setSubgrupoId(2L);
        dto.setMotivoId(7L);
        dto.setComentarioEncerramento("resolvido");

        ticketService.encerrarTicket("TK-3", dto, 42L);

        assertEquals(TicketStatus.RESOLVIDO, ticket.getStatus());
        assertEquals(motivo, ticket.getMotivo());
        verify(ticketInteracaoService).registrarEncerramentoAutomatico(ticket, "resolvido");
        verify(ticketSatisfacaoService).registrarDecisaoPosEncerramento(ticket, false, 42L);
    }

    private static Ticket ticket(String numero) {
        Ticket t = new Ticket();
        t.setNumeroTicket(numero);
        t.setStatus(TicketStatus.EM_ATENDIMENTO);
        return t;
    }

    private static GrupoCategoria grupo(Long id) {
        GrupoCategoria g = new GrupoCategoria();
        g.setId(id);
        g.setNome("G");
        g.setAtivo(true);
        return g;
    }

    private static SubgrupoCategoria subgrupo(Long id, GrupoCategoria grupo) {
        SubgrupoCategoria s = new SubgrupoCategoria();
        s.setId(id);
        s.setNome("S");
        s.setAtivo(true);
        s.setGrupoCategoria(grupo);
        return s;
    }
}

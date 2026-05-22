package com.suporte.tickets.service;

import com.suporte.tickets.dto.AvaliacaoPublicaResponseDTO;
import com.suporte.tickets.dto.TicketSatisfacaoRespostaRequestDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketSatisfacaoTokenPublicoTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketSatisfacaoRepository ticketSatisfacaoRepository;

    @InjectMocks
    private TicketSatisfacaoService ticketSatisfacaoService;

    @Test
    void pendente_recebeHashAoGerarToken() {
        TicketSatisfacao s = new TicketSatisfacao();
        s.setStatus(TicketSatisfacaoStatus.PENDENTE);
        String plain = TicketSatisfacaoService.atribuirNovoTokenResposta(
                s, LocalDateTime.now());
        assertNotNull(plain);
        assertNotNull(s.getTokenRespostaHash());
        assertEquals(64, s.getTokenRespostaHash().length());
    }

    @Test
    void consultarPublico_porTokenValido() {
        String plain = TicketSatisfacaoTokenUtil.gerarTokenOpaco();
        String hash = TicketSatisfacaoTokenUtil.hashToken(plain);
        Ticket ticket = ticket("ACME-99", "Acme Ltda");
        TicketSatisfacao s = pendenteComTicket(ticket, hash);
        when(ticketSatisfacaoRepository.findByTokenRespostaHash(hash)).thenReturn(Optional.of(s));

        AvaliacaoPublicaResponseDTO dto = ticketSatisfacaoService.consultarAvaliacaoPublica(plain);

        assertEquals("PENDENTE", dto.getStatus());
        assertEquals("Acme Ltda", dto.getClienteNome());
        assertEquals("Chamado ••••E-99", dto.getProtocoloMascarado());
        assertEquals(false, dto.isJaRespondida());
    }

    @Test
    void tokenInvalido_naoVazaDados() {
        assertThrows(IllegalArgumentException.class,
                () -> ticketSatisfacaoService.consultarAvaliacaoPublica("token-inexistente"));
    }

    @Test
    void responderPorToken_respondida() {
        String plain = TicketSatisfacaoTokenUtil.gerarTokenOpaco();
        String hash = TicketSatisfacaoTokenUtil.hashToken(plain);
        Ticket ticket = ticket("T-200", "Cliente");
        TicketSatisfacao s = pendenteComTicket(ticket, hash);
        s.setExpiraEm(LocalDateTime.now().plusDays(1));
        when(ticketSatisfacaoRepository.findByTokenRespostaHash(hash)).thenReturn(Optional.of(s));
        when(ticketSatisfacaoRepository.save(any(TicketSatisfacao.class))).thenAnswer(i -> i.getArgument(0));

        TicketSatisfacaoRespostaRequestDTO req = new TicketSatisfacaoRespostaRequestDTO(5, "ok");
        AvaliacaoPublicaResponseDTO dto = ticketSatisfacaoService.responderAvaliacaoPublica(plain, req);

        assertEquals(TicketSatisfacaoStatus.RESPONDIDA, s.getStatus());
        assertEquals(5, s.getNota());
        assertNotNull(s.getTokenUsadoEm());
        assertEquals(true, dto.isJaRespondida());
    }

    @Test
    void segundaResposta_bloqueada() {
        String plain = TicketSatisfacaoTokenUtil.gerarTokenOpaco();
        String hash = TicketSatisfacaoTokenUtil.hashToken(plain);
        TicketSatisfacao s = pendenteComTicket(ticket("X", "C"), hash);
        s.setStatus(TicketSatisfacaoStatus.RESPONDIDA);
        when(ticketSatisfacaoRepository.findByTokenRespostaHash(hash)).thenReturn(Optional.of(s));

        assertThrows(IllegalArgumentException.class, () -> ticketSatisfacaoService.responderAvaliacaoPublica(
                plain, new TicketSatisfacaoRespostaRequestDTO(3, null)));
    }

    @Test
    void hashUtil_consistente() {
        String t = "abc123";
        assertEquals(TicketSatisfacaoTokenUtil.hashToken(t), TicketSatisfacaoTokenUtil.hashToken(t));
    }

    private static Ticket ticket(String numero, String nomeCliente) {
        Ticket t = new Ticket();
        t.setNumeroTicket(numero);
        t.setStatus(TicketStatus.RESOLVIDO);
        Cliente c = new Cliente();
        c.setNome(nomeCliente);
        t.setCliente(c);
        return t;
    }

    private static TicketSatisfacao pendenteComTicket(Ticket ticket, String hash) {
        TicketSatisfacao s = new TicketSatisfacao();
        s.setStatus(TicketSatisfacaoStatus.PENDENTE);
        s.setTicket(ticket);
        s.setTokenRespostaHash(hash);
        return s;
    }
}

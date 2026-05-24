package com.suporte.tickets.service;

import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.repository.ContatoTelefoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContatoAtendimentoOrigemServiceTest {

    @Mock
    private ContatoTelefoneRepository contatoTelefoneRepository;

    @InjectMocks
    private ContatoAtendimentoOrigemService service;

    @Test
    void resolve_principal_quandoIgualWhatsappContato() {
        Contato contato = new Contato();
        contato.setId(1);
        contato.setWhatsappNormalizado("5511999999999");

        var origem = service.resolver(contato, "5511999999999");
        assertNotNull(origem);
        assertEquals(ContatoAtendimentoOrigemService.TIPO_PRINCIPAL, origem.tipo());
    }

    @Test
    void resolve_adicional_quandoCadastradoEmContatoTelefone() {
        Contato contato = new Contato();
        contato.setId(1);
        contato.setWhatsappNormalizado("5511999999999");
        when(contatoTelefoneRepository.existsByContato_IdAndTelefoneNormalizado(eq(1), eq("5512942833853")))
                .thenReturn(true);

        var origem = service.resolver(contato, "5512942833853");
        assertNotNull(origem);
        assertEquals(ContatoAtendimentoOrigemService.TIPO_ADICIONAL, origem.tipo());
    }

    @Test
    void aplicarOrigemNoTicket_preencheCampos() {
        Contato contato = new Contato();
        contato.setId(2);
        contato.setWhatsappNormalizado("5511111111111");
        Ticket ticket = new Ticket();

        service.aplicarOrigemNoTicket(ticket, contato, "5511111111111");

        assertEquals("5511111111111", ticket.getAtendimentoTelefone());
        assertEquals("5511111111111", ticket.getAtendimentoTelefoneNormalizado());
        assertEquals(ContatoAtendimentoOrigemService.TIPO_PRINCIPAL, ticket.getAtendimentoTelefoneTipo());
    }
}

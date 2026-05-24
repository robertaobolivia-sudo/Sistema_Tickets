package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoTelefoneRequestDTO;
import com.suporte.tickets.dto.ContatoTelefoneResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.ContatoTelefone;
import com.suporte.tickets.repository.ContatoRepository;
import com.suporte.tickets.repository.ContatoTelefoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContatoTelefoneServiceTest {

    @Mock
    private ContatoTelefoneRepository contatoTelefoneRepository;

    @Mock
    private ContatoRepository contatoRepository;

    @InjectMocks
    private ContatoTelefoneService contatoTelefoneService;

    @Test
    void adicionar_persisteTelefoneAdicional() {
        Contato contato = contato(10, "5511999000001");
        when(contatoRepository.findById(10)).thenReturn(Optional.of(contato));
        when(contatoRepository.existsByCliente_IdAndWhatsappNormalizado(1, "22222222222"))
                .thenReturn(false);
        when(contatoTelefoneRepository.existsByCliente_IdAndTelefoneNormalizado(1, "22222222222"))
                .thenReturn(false);
        when(contatoTelefoneRepository.save(any(ContatoTelefone.class))).thenAnswer(inv -> {
            ContatoTelefone ct = inv.getArgument(0);
            ct.setId(99L);
            return ct;
        });

        ContatoTelefoneRequestDTO req = new ContatoTelefoneRequestDTO();
        req.setTelefone("(22) 22222-2222");
        req.setOrigem("CADASTRO_MANUAL");

        ContatoTelefoneResponseDTO resp = contatoTelefoneService.adicionar(10, req);

        assertEquals(99L, resp.getId());
        assertEquals(10, resp.getContatoId());
        assertEquals("22222222222", resp.getTelefoneNormalizado());
        assertEquals(false, resp.getPrincipal());
        ArgumentCaptor<ContatoTelefone> cap = ArgumentCaptor.forClass(ContatoTelefone.class);
        verify(contatoTelefoneRepository).save(cap.capture());
        assertEquals(10, cap.getValue().getContato().getId());
        assertEquals(1, cap.getValue().getCliente().getId());
    }

    @Test
    void adicionar_bloqueiaDuplicidadeNoCliente() {
        Contato contato = contato(10, "5511999000001");
        when(contatoRepository.findById(10)).thenReturn(Optional.of(contato));
        when(contatoRepository.existsByCliente_IdAndWhatsappNormalizado(1, "5522222222222"))
                .thenReturn(true);

        ContatoTelefoneRequestDTO req = new ContatoTelefoneRequestDTO();
        req.setTelefone("5522222222222");

        assertThrows(IllegalArgumentException.class, () -> contatoTelefoneService.adicionar(10, req));
        verify(contatoTelefoneRepository, never()).save(any());
    }

    @Test
    void adicionar_naoPermiteIgualAoPrincipal() {
        Contato contato = contato(10, "5511999000001");
        when(contatoRepository.findById(10)).thenReturn(Optional.of(contato));

        ContatoTelefoneRequestDTO req = new ContatoTelefoneRequestDTO();
        req.setTelefone("5511999000001");

        assertThrows(IllegalArgumentException.class, () -> contatoTelefoneService.adicionar(10, req));
        verify(contatoTelefoneRepository, never()).save(any());
    }

    private static Contato contato(int id, String whatsappNorm) {
        Cliente cliente = new Cliente();
        cliente.setId(1);
        cliente.setNome("Cliente A");
        Contato c = new Contato();
        c.setId(id);
        c.setCliente(cliente);
        c.setNome("Contato");
        c.setWhatsapp(whatsappNorm);
        c.setWhatsappNormalizado(whatsappNorm);
        c.setAtivo(true);
        return c;
    }
}

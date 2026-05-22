package com.suporte.tickets.service;

import com.suporte.tickets.dto.WhatsappMatrizRequestDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.WhatsappMatriz;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.WhatsappMatrizRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsappMatrizServiceTest {

    @Mock
    private WhatsappMatrizRepository whatsappMatrizRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private WhatsappMatrizService service;

    @Test
    void criar_bloqueiaNumeroDuplicado() {
        Cliente c = cliente(1);
        when(clienteRepository.findById(1)).thenReturn(Optional.of(c));
        when(whatsappMatrizRepository.existsByNumeroNormalizado("11887776655")).thenReturn(true);

        WhatsappMatrizRequestDTO dto = new WhatsappMatrizRequestDTO();
        dto.setClienteId(1);
        dto.setNumero("11887776655");

        assertThrows(IllegalArgumentException.class, () -> service.criar(dto));
        verify(whatsappMatrizRepository, never()).save(any());
    }

    @Test
    void resolverClientePorWhatsappMatrizId() {
        Cliente c = cliente(2);
        WhatsappMatriz m = new WhatsappMatriz();
        m.setId(9);
        m.setCliente(c);
        m.setAtivo(true);
        m.setNumero("1100000000");
        m.setNumeroNormalizado("1100000000");
        when(whatsappMatrizRepository.findById(9)).thenReturn(Optional.of(m));

        Cliente res = service.resolverClientePorWhatsappMatrizId(9);

        assertEquals(2, res.getId());
    }

    private static Cliente cliente(int id) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNome("Cli " + id);
        c.setEmail("a@b.com");
        c.setTelefone("1100000000");
        c.setTelefoneContato("1100000001");
        return c;
    }
}

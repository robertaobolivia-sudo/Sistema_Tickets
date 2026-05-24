package com.suporte.tickets.service;

import com.suporte.tickets.config.UploadStorageProperties;
import com.suporte.tickets.dto.ClienteRequestDTO;
import com.suporte.tickets.dto.ClienteResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Sprint F40: Cliente contratante sem FK Carteira. */
@ExtendWith(MockitoExtension.class)
class ClienteServiceClientePuroF40Test {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UploadStorageProperties uploadStorageProperties;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void criar_persisteClienteContratante() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNome("Resp");
        dto.setTelefone("5511999999999");
        dto.setTelefoneContato("5511999999999");
        dto.setEmail("a@b.com");

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(1);
            return c;
        });

        ClienteResponseDTO resp = clienteService.criar(dto);

        verify(clienteRepository).save(any(Cliente.class));
        assertEquals(1, resp.getId());
    }
}

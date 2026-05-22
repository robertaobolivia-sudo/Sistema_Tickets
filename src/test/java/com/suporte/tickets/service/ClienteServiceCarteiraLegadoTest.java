package com.suporte.tickets.service;

import com.suporte.tickets.config.UploadStorageProperties;
import com.suporte.tickets.dto.ClienteRequestDTO;
import com.suporte.tickets.entity.Carteira;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.repository.CarteiraRepository;
import com.suporte.tickets.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceCarteiraLegadoTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CarteiraRepository carteiraRepository;

    @Mock
    private UploadStorageProperties uploadStorageProperties;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void criar_naoAssociaCarteiraPorNome_noPayloadTela() {
        ClienteRequestDTO dto = dtoMinimo();
        dto.setCarteira("Carteira Nova Via Nome");

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        clienteService.criar(dto);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertNull(captor.getValue().getCarteira());
        verify(carteiraRepository, never()).save(any());
        verify(carteiraRepository, never()).findByNome(any());
    }

    @Test
    void criar_associaCarteiraSomenteQuandoCarteiraIdInformado() {
        ClienteRequestDTO dto = dtoMinimo();
        dto.setCarteiraId(7);

        Carteira carteira = new Carteira();
        carteira.setId(7);
        carteira.setNome("Legado Norte");
        when(carteiraRepository.findById(7)).thenReturn(Optional.of(carteira));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        clienteService.criar(dto);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertEquals(7, captor.getValue().getCarteira().getId());
    }

    private static ClienteRequestDTO dtoMinimo() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNome("Fênix");
        dto.setEmail("contato@fenix.example");
        dto.setTelefone("11999990000");
        dto.setTelefoneContato("11999990001");
        dto.setStatus("ATIVO");
        return dto;
    }
}

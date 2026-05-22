package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoRequestDTO;
import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.ContatoRepository;
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
class ContatoServiceTest {

    @Mock
    private ContatoRepository contatoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ContatoService contatoService;

    @Test
    void criar_persisteWhatsappNormalizado() {
        Cliente cliente = cliente(1, "Fenix");
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(contatoRepository.existsByCliente_IdAndWhatsappNormalizado(1, "11999990001")).thenReturn(false);
        when(contatoRepository.save(any(Contato.class))).thenAnswer(inv -> {
            Contato c = inv.getArgument(0);
            c.setId(10);
            return c;
        });

        ContatoRequestDTO dto = new ContatoRequestDTO();
        dto.setClienteId(1);
        dto.setWhatsapp("(11) 99999-0001");
        dto.setNome("Carlos");

        ContatoResponseDTO resp = contatoService.criar(dto);

        assertEquals(10, resp.getId());
        ArgumentCaptor<Contato> cap = ArgumentCaptor.forClass(Contato.class);
        verify(contatoRepository).save(cap.capture());
        assertEquals("11999990001", cap.getValue().getWhatsappNormalizado());
    }

    @Test
    void criar_bloqueiaDuplicidadeMesmoCliente() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente(1, "A")));
        when(contatoRepository.existsByCliente_IdAndWhatsappNormalizado(eq(1), eq("5511888000000")))
                .thenReturn(true);

        ContatoRequestDTO dto = new ContatoRequestDTO();
        dto.setClienteId(1);
        dto.setWhatsapp("5511888000000");
        dto.setNome("X");

        assertThrows(IllegalArgumentException.class, () -> contatoService.criar(dto));
        verify(contatoRepository, never()).save(any());
    }

    @Test
    void criar_permiteMesmoWhatsappEmOutroCliente() {
        when(clienteRepository.findById(2)).thenReturn(Optional.of(cliente(2, "B")));
        when(contatoRepository.existsByCliente_IdAndWhatsappNormalizado(2, "5511888000000")).thenReturn(false);
        when(contatoRepository.save(any(Contato.class))).thenAnswer(inv -> inv.getArgument(0));

        ContatoRequestDTO dto = new ContatoRequestDTO();
        dto.setClienteId(2);
        dto.setWhatsapp("5511888000000");
        dto.setNome("Y");

        contatoService.criar(dto);

        verify(contatoRepository).save(any(Contato.class));
    }

    @Test
    void atualizar_naoPermiteAlterarWhatsapp() {
        Contato existente = new Contato();
        existente.setId(5);
        existente.setCliente(cliente(1, "A"));
        existente.setWhatsapp("(11) 8888-0000");
        existente.setWhatsappNormalizado("551188880000");
        existente.setNome("Antigo");
        existente.setAtivo(true);
        when(contatoRepository.findById(5)).thenReturn(Optional.of(existente));

        ContatoRequestDTO dto = new ContatoRequestDTO();
        dto.setWhatsapp("5511999999999");
        dto.setNome("Novo nome");

        assertThrows(IllegalArgumentException.class, () -> contatoService.atualizar(5, dto));
    }

    @Test
    void criarSeNaoExistir_reutilizaExistente() {
        Contato existente = new Contato();
        existente.setId(7);
        existente.setCliente(cliente(1, "A"));
        existente.setNome("Carlos");
        existente.setWhatsapp("11");
        existente.setWhatsappNormalizado("5511");
        when(contatoRepository.findByCliente_IdAndWhatsappNormalizado(1, "5511"))
                .thenReturn(Optional.of(existente));

        ContatoResponseDTO dto = contatoService.criarSeNaoExistir(1, "5511", "Outro");

        assertEquals(7, dto.getId());
        verify(contatoRepository, never()).save(any());
    }

    private static Cliente cliente(int id, String nome) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNome(nome);
        c.setEmail("a@b.com");
        c.setTelefone("11999990000");
        c.setTelefoneContato("11999990001");
        return c;
    }
}

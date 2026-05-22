package com.suporte.tickets.service;

import com.suporte.tickets.dto.EtiquetaRequestDTO;
import com.suporte.tickets.entity.Etiqueta;
import com.suporte.tickets.repository.EtiquetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtiquetaServiceTest {

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @InjectMocks
    private EtiquetaService etiquetaService;

    @Test
    void criar_nomeDuplicadoLancaErro() {
        Etiqueta existente = new Etiqueta();
        existente.setId(1L);
        existente.setNome("VIP");
        when(etiquetaRepository.findByNomeIgnoreCase("VIP")).thenReturn(Optional.of(existente));

        EtiquetaRequestDTO dto = new EtiquetaRequestDTO("VIP", "desc", "#ff0000");
        assertThrows(IllegalArgumentException.class, () -> etiquetaService.criar(dto));
    }

    @Test
    void criar_sucessoQuandoNomeLivre() {
        when(etiquetaRepository.findByNomeIgnoreCase("Nova")).thenReturn(Optional.empty());
        when(etiquetaRepository.save(any(Etiqueta.class))).thenAnswer(inv -> {
            Etiqueta e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        EtiquetaRequestDTO dto = new EtiquetaRequestDTO("Nova", null, null);
        etiquetaService.criar(dto);
    }
}

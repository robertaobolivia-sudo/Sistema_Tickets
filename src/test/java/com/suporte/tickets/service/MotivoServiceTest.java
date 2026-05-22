package com.suporte.tickets.service;

import com.suporte.tickets.dto.MotivoRequestDTO;
import com.suporte.tickets.entity.GrupoCategoria;
import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.repository.MotivoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MotivoServiceTest {

    @Mock
    private MotivoRepository motivoRepository;
    @Mock
    private SubgrupoCategoriaService subgrupoCategoriaService;

    @InjectMocks
    private MotivoService motivoService;

    @Test
    void criar_bloqueiaDuplicidade() {
        SubgrupoCategoria sub = subgrupo(1L);
        when(subgrupoCategoriaService.buscarEntidadeAtiva(1L)).thenReturn(sub);
        when(motivoRepository.existsBySubgrupoCategoriaIdAndNomeIgnoreCase(1L, "X")).thenReturn(true);

        MotivoRequestDTO dto = new MotivoRequestDTO();
        dto.setSubgrupoId(1L);
        dto.setNome("X");

        assertThrows(IllegalArgumentException.class, () -> motivoService.criar(dto));
        verify(motivoRepository, never()).save(any());
    }

    private static SubgrupoCategoria subgrupo(Long id) {
        GrupoCategoria g = new GrupoCategoria();
        g.setId(10L);
        g.setNome("G");
        g.setAtivo(true);
        SubgrupoCategoria s = new SubgrupoCategoria();
        s.setId(id);
        s.setNome("S");
        s.setAtivo(true);
        s.setGrupoCategoria(g);
        return s;
    }
}

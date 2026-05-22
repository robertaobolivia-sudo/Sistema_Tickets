package com.suporte.tickets.config;

import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import com.suporte.tickets.repository.MotivoRepository;
import com.suporte.tickets.repository.SubgrupoCategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaSeedConfigTest {

    @Mock
    private GrupoCategoriaRepository grupoCategoriaRepository;
    @Mock
    private SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    @Mock
    private MotivoRepository motivoRepository;

    @InjectMocks
    private CategoriaSeedConfig categoriaSeedConfig;

    @Test
    void garantirMotivoAtivo_criaOutrosQuandoSubcategoriaSemMotivoAtivo() {
        SubgrupoCategoria sub = new SubgrupoCategoria();
        sub.setId(99L);
        sub.setNome("Custom");
        sub.setAtivo(true);
        when(subgrupoCategoriaRepository.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(sub));
        when(motivoRepository.findBySubgrupoCategoriaIdAndAtivoTrueOrderByNomeAsc(99L)).thenReturn(List.of());
        when(motivoRepository.existsBySubgrupoCategoriaIdAndNomeIgnoreCase(eq(99L), eq("Outros"))).thenReturn(false);

        categoriaSeedConfig.garantirMotivoAtivoEmSubcategoriasAtivas();

        verify(motivoRepository).save(any());
    }

    @Test
    void garantirMotivoAtivo_naoDuplicaQuandoJaExisteMotivoAtivo() {
        SubgrupoCategoria sub = new SubgrupoCategoria();
        sub.setId(1L);
        when(subgrupoCategoriaRepository.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(sub));
        when(motivoRepository.findBySubgrupoCategoriaIdAndAtivoTrueOrderByNomeAsc(1L))
                .thenReturn(List.of(new com.suporte.tickets.entity.Motivo()));

        categoriaSeedConfig.garantirMotivoAtivoEmSubcategoriasAtivas();

        verify(motivoRepository, never()).save(any());
    }
}

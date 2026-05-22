package com.suporte.tickets.service;

import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Etiqueta;
import com.suporte.tickets.repository.ContatoEtiquetaRepository;
import com.suporte.tickets.repository.ContatoRepository;
import com.suporte.tickets.repository.EtiquetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContatoEtiquetaServiceTest {

    @Mock
    private ContatoRepository contatoRepository;

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @Mock
    private ContatoEtiquetaRepository contatoEtiquetaRepository;

    @InjectMocks
    private ContatoEtiquetaService contatoEtiquetaService;

    @Test
    void substituir_rejeitaContatoInexistente() {
        when(contatoRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(
                IllegalArgumentException.class,
                () -> contatoEtiquetaService.substituirVinculosAtivos(99, List.of(1L)));
        verify(contatoEtiquetaRepository, never()).deleteByContato(any());
    }

    @Test
    void substituir_rejeitaEtiquetaInativa() {
        Contato contato = new Contato();
        contato.setId(5);
        when(contatoRepository.findById(5)).thenReturn(Optional.of(contato));
        when(contatoEtiquetaRepository.findByContatoOrderByEtiqueta_NomeAsc(contato)).thenReturn(List.of());
        Etiqueta inativa = new Etiqueta();
        inativa.setId(2L);
        inativa.setAtivo(false);
        when(etiquetaRepository.findById(2L)).thenReturn(Optional.of(inativa));

        assertThrows(
                IllegalArgumentException.class,
                () -> contatoEtiquetaService.substituirVinculosAtivos(5, List.of(2L)));
    }

    @Test
    void substituir_aplicaEtiquetasAtivas() {
        Contato contato = new Contato();
        contato.setId(5);
        when(contatoRepository.findById(5)).thenReturn(Optional.of(contato));
        when(contatoEtiquetaRepository.findByContatoOrderByEtiqueta_NomeAsc(contato)).thenReturn(List.of());
        Etiqueta e1 = etiquetaAtiva(1L, "A");
        when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(e1));
        when(contatoEtiquetaRepository.findByContatoOrderByEtiqueta_NomeAsc(contato))
                .thenReturn(List.of())
                .thenReturn(List.of(vinculo(contato, e1)));

        contatoEtiquetaService.substituirVinculosAtivos(5, List.of(1L, 1L));

        verify(contatoEtiquetaRepository).deleteByContato(contato);
        verify(contatoEtiquetaRepository).save(any());
    }

    private static Etiqueta etiquetaAtiva(Long id, String nome) {
        Etiqueta e = new Etiqueta();
        e.setId(id);
        e.setNome(nome);
        e.setAtivo(true);
        return e;
    }

    private static com.suporte.tickets.entity.ContatoEtiqueta vinculo(Contato c, Etiqueta e) {
        com.suporte.tickets.entity.ContatoEtiqueta v = new com.suporte.tickets.entity.ContatoEtiqueta();
        v.setContato(c);
        v.setEtiqueta(e);
        return v;
    }

    @Test
    void listar_rejeitaContatoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> contatoEtiquetaService.listarPorContatoId(null));
        assertThrows(IllegalArgumentException.class, () -> contatoEtiquetaService.listarPorContatoId(0));
    }

    @Test
    void listar_vazioQuandoSemVinculos() {
        Contato contato = new Contato();
        contato.setId(1);
        when(contatoRepository.findById(1)).thenReturn(Optional.of(contato));
        when(contatoEtiquetaRepository.findByContatoOrderByEtiqueta_NomeAsc(contato)).thenReturn(List.of());
        assertEquals(0, contatoEtiquetaService.listarPorContatoId(1).size());
    }
}

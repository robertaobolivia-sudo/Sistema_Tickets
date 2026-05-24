package com.suporte.tickets.service;

import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.ContatoEtiqueta;
import com.suporte.tickets.entity.Etiqueta;
import com.suporte.tickets.repository.ContatoEtiquetaRepository;
import com.suporte.tickets.repository.ContatoRepository;
import com.suporte.tickets.repository.EtiquetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContatoEtiquetaLegadoBackfillServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private ContatoRepository contatoRepository;
    @Mock private EtiquetaRepository etiquetaRepository;
    @Mock private ContatoEtiquetaRepository contatoEtiquetaRepository;

    @InjectMocks
    private ContatoEtiquetaLegadoBackfillService backfillService;

    private void mockTabelaLegadoExiste(boolean existe) {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("ticket_etiquetas")))
                .thenReturn(existe ? 1 : 0);
    }

    @Test
    void executar_insereQuandoContatoTemEtiquetaNova() {
        mockTabelaLegadoExiste(true);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("contatoId", 10, "etiquetaId", 5L)
        ));
        when(contatoEtiquetaRepository.existsByContato_IdAndEtiqueta_Id(10, 5L)).thenReturn(false);
        Contato contato = new Contato();
        contato.setId(10);
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setId(5L);
        when(contatoRepository.findById(10)).thenReturn(Optional.of(contato));
        when(etiquetaRepository.findById(5L)).thenReturn(Optional.of(etiqueta));

        ContatoEtiquetaLegadoBackfillService.Resultado r = backfillService.executar();

        assertEquals(1, r.inseridos());
        assertEquals(0, r.jaExistiam());
        ArgumentCaptor<ContatoEtiqueta> cap = ArgumentCaptor.forClass(ContatoEtiqueta.class);
        verify(contatoEtiquetaRepository).save(cap.capture());
        assertEquals(contato, cap.getValue().getContato());
        assertEquals(etiqueta, cap.getValue().getEtiqueta());
    }

    @Test
    void executar_naoDuplicaQuandoJaExiste() {
        mockTabelaLegadoExiste(true);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("contatoId", 10, "etiquetaId", 5L)
        ));
        when(contatoEtiquetaRepository.existsByContato_IdAndEtiqueta_Id(10, 5L)).thenReturn(true);

        ContatoEtiquetaLegadoBackfillService.Resultado r = backfillService.executar();

        assertEquals(0, r.inseridos());
        assertEquals(1, r.jaExistiam());
        verify(contatoEtiquetaRepository, never()).save(any());
    }

    @Test
    void executar_semTabelaLegadoRetornaZeros() {
        mockTabelaLegadoExiste(false);

        ContatoEtiquetaLegadoBackfillService.Resultado r = backfillService.executar();

        assertEquals(0, r.candidatos());
        verify(jdbcTemplate, never()).queryForList(anyString());
    }
}

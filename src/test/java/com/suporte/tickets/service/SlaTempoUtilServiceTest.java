package com.suporte.tickets.service;

import com.suporte.tickets.entity.HorarioUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SlaTempoUtilServiceTest {

    @Mock
    private HorarioUtilService horarioUtilService;

    @Mock
    private CalendarioSlaHelper calendarioSlaHelper;

    @InjectMocks
    private SlaTempoUtilService slaTempoUtilService;

    @BeforeEach
    void setUp() {
        HorarioUtil horario = new HorarioUtil();
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFim(LocalTime.of(18, 0));
        horario.setSegunda(true);
        horario.setTerca(true);
        horario.setQuarta(true);
        horario.setQuinta(true);
        horario.setSexta(true);
        horario.setSabado(false);
        horario.setDomingo(false);
        when(horarioUtilService.obterOuCriarEntidadePadrao()).thenReturn(horario);
        when(calendarioSlaHelper.isFeriado(any(LocalDate.class))).thenReturn(false);
    }

    @Test
    void dentroDoExpediente() {
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 18, 10, 0);
        LocalDateTime vencimento = slaTempoUtilService.adicionarMinutosUteis(inicio, 60);
        assertEquals(LocalDateTime.of(2026, 5, 18, 11, 0), vencimento);
    }

    @Test
    void cruzandoFimDoExpediente() {
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 18, 17, 55);
        LocalDateTime vencimento = slaTempoUtilService.adicionarMinutosUteis(inicio, 15);
        assertEquals(LocalDateTime.of(2026, 5, 19, 8, 10), vencimento);
    }

    @Test
    void antesDoExpediente() {
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 18, 7, 30);
        LocalDateTime vencimento = slaTempoUtilService.adicionarMinutosUteis(inicio, 30);
        assertEquals(LocalDateTime.of(2026, 5, 18, 8, 30), vencimento);
    }

    @Test
    void aposExpediente() {
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 18, 18, 30);
        LocalDateTime vencimento = slaTempoUtilService.adicionarMinutosUteis(inicio, 30);
        assertEquals(LocalDateTime.of(2026, 5, 19, 8, 30), vencimento);
    }

    @Test
    void sextaCruzandoFimDeSemana() {
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 22, 17, 30);
        LocalDateTime vencimento = slaTempoUtilService.adicionarMinutosUteis(inicio, 90);
        assertEquals(LocalDateTime.of(2026, 5, 25, 9, 0), vencimento);
    }

    @Test
    void minutosInvalidos() {
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 18, 10, 0);
        assertThrows(IllegalArgumentException.class,
                () -> slaTempoUtilService.adicionarMinutosUteis(inicio, 0));
    }

    @Test
    void calcularMinutosUteisEntreDentroDoExpediente() {
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 18, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 5, 18, 10, 35);
        assertEquals(35, slaTempoUtilService.calcularMinutosUteisEntre(inicio, fim));
    }

    @Test
    void calcularMinutosUteisEntreForaDoExpediente() {
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 18, 19, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 5, 18, 20, 0);
        assertEquals(0, slaTempoUtilService.calcularMinutosUteisEntre(inicio, fim));
    }

    @Test
    void calcularMinutosUteisEntreFimAntesOuIgualInicio() {
        LocalDateTime inicio = LocalDateTime.of(2026, 5, 18, 10, 0);
        assertEquals(0, slaTempoUtilService.calcularMinutosUteisEntre(inicio, inicio));
        assertEquals(0, slaTempoUtilService.calcularMinutosUteisEntre(inicio, inicio.minusMinutes(5)));
    }
}

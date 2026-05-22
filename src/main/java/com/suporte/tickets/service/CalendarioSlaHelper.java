package com.suporte.tickets.service;

import com.suporte.tickets.repository.FeriadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Base de calendário para SLA futuro (fuso America/Sao_Paulo).
 * Não calcula SLA neste sprint.
 */
@Component
@RequiredArgsConstructor
public class CalendarioSlaHelper {

    public static final ZoneId FUSO_SLA = ZoneId.of("America/Sao_Paulo");

    private final FeriadoRepository feriadoRepository;

    public ZoneId getFusoSla() {
        return FUSO_SLA;
    }

    public LocalDate hojeNoFusoSla() {
        return LocalDate.now(FUSO_SLA);
    }

    public boolean isFeriado(LocalDate data) {
        if (data == null) {
            return false;
        }
        return feriadoRepository.findFirstByDataAndAtivoTrue(data).isPresent();
    }

    /**
     * Preparado para SLA futuro: fim de semana ou feriado ativo.
     * Horário útil (seg–sex 08–18) será combinado no motor de SLA.
     */
    public boolean isDiaNaoUtil(LocalDate data) {
        if (data == null) {
            return false;
        }
        DayOfWeek dia = data.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return true;
        }
        return isFeriado(data);
    }
}

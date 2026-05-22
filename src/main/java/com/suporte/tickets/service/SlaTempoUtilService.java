package com.suporte.tickets.service;

import com.suporte.tickets.dto.SlaCalculoTesteResponseDTO;
import com.suporte.tickets.entity.HorarioUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class SlaTempoUtilService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final HorarioUtilService horarioUtilService;
    private final CalendarioSlaHelper calendarioSlaHelper;

    public boolean isDiaUtil(LocalDate data) {
        HorarioUtil horario = carregarHorarioUtil();
        return isDiaUtil(data, horario);
    }

    public boolean isDentroHorarioUtil(LocalDateTime dataHora) {
        HorarioUtil horario = carregarHorarioUtil();
        if (!isDiaUtil(dataHora.toLocalDate(), horario)) {
            return false;
        }
        LocalTime hora = dataHora.toLocalTime();
        return !hora.isBefore(horario.getHoraInicio()) && hora.isBefore(horario.getHoraFim());
    }

    public LocalDateTime proximoInstanteUtil(LocalDateTime dataHora) {
        if (dataHora == null) {
            throw new IllegalArgumentException("Data/hora inicial é obrigatória.");
        }
        HorarioUtil horario = carregarHorarioUtil();
        LocalDate data = dataHora.toLocalDate();
        LocalTime hora = dataHora.toLocalTime();

        if (!isDiaUtil(data, horario)) {
            return inicioProximoDiaUtil(data, horario);
        }
        if (hora.isBefore(horario.getHoraInicio())) {
            return LocalDateTime.of(data, horario.getHoraInicio());
        }
        if (!hora.isBefore(horario.getHoraFim())) {
            return inicioProximoDiaUtil(data.plusDays(1), horario);
        }
        return dataHora;
    }

    public LocalDateTime adicionarMinutosUteis(LocalDateTime inicio, long minutos) {
        if (inicio == null) {
            throw new IllegalArgumentException("Data/hora inicial é obrigatória.");
        }
        if (minutos <= 0) {
            throw new IllegalArgumentException("A quantidade de minutos úteis deve ser maior que zero.");
        }

        LocalDateTime cursor = proximoInstanteUtil(inicio);
        long restante = minutos;

        while (restante > 0) {
            HorarioUtil horario = carregarHorarioUtil();
            LocalDate dataCursor = cursor.toLocalDate();

            if (!isDiaUtil(dataCursor, horario)) {
                cursor = inicioProximoDiaUtil(dataCursor, horario);
                continue;
            }

            LocalDateTime fimExpediente = LocalDateTime.of(dataCursor, horario.getHoraFim());
            long minutosDisponiveisNoDia = Duration.between(cursor, fimExpediente).toMinutes();
            if (minutosDisponiveisNoDia <= 0) {
                cursor = inicioProximoDiaUtil(dataCursor.plusDays(1), horario);
                continue;
            }

            if (restante <= minutosDisponiveisNoDia) {
                return cursor.plusMinutes(restante);
            }

            restante -= minutosDisponiveisNoDia;
            cursor = inicioProximoDiaUtil(dataCursor.plusDays(1), horario);
        }

        return cursor;
    }

    public long calcularMinutosUteisEntre(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null || !fim.isAfter(inicio)) {
            return 0;
        }
        LocalDateTime cursor = proximoInstanteUtil(inicio);
        if (!cursor.isBefore(fim)) {
            return 0;
        }
        long total = 0;
        while (cursor.isBefore(fim)) {
            HorarioUtil horario = carregarHorarioUtil();
            LocalDate dataCursor = cursor.toLocalDate();

            if (!isDiaUtil(dataCursor, horario)) {
                cursor = inicioProximoDiaUtil(dataCursor, horario);
                continue;
            }

            LocalDateTime fimExpediente = LocalDateTime.of(dataCursor, horario.getHoraFim());
            LocalDateTime fimIntervalo = fim.isBefore(fimExpediente) ? fim : fimExpediente;
            if (cursor.isBefore(fimIntervalo)) {
                total += Duration.between(cursor, fimIntervalo).toMinutes();
            }
            if (!fim.isAfter(fimExpediente)) {
                break;
            }
            cursor = inicioProximoDiaUtil(dataCursor.plusDays(1), horario);
        }
        return total;
    }

    public SlaCalculoTesteResponseDTO calcularVencimentoTeste(String inicioStr, long minutos) {
        LocalDateTime inicio = parseDateTime(inicioStr);
        LocalDateTime vencimento = adicionarMinutosUteis(inicio, minutos);
        return new SlaCalculoTesteResponseDTO(
                formatDateTime(inicio),
                minutos,
                formatDateTime(vencimento),
                CalendarioSlaHelper.FUSO_SLA.getId()
        );
    }

    private HorarioUtil carregarHorarioUtil() {
        return horarioUtilService.obterOuCriarEntidadePadrao();
    }

    private boolean isDiaUtil(LocalDate data, HorarioUtil horario) {
        if (data == null) {
            return false;
        }
        if (calendarioSlaHelper.isFeriado(data)) {
            return false;
        }
        return switch (data.getDayOfWeek()) {
            case MONDAY -> Boolean.TRUE.equals(horario.getSegunda());
            case TUESDAY -> Boolean.TRUE.equals(horario.getTerca());
            case WEDNESDAY -> Boolean.TRUE.equals(horario.getQuarta());
            case THURSDAY -> Boolean.TRUE.equals(horario.getQuinta());
            case FRIDAY -> Boolean.TRUE.equals(horario.getSexta());
            case SATURDAY -> Boolean.TRUE.equals(horario.getSabado());
            case SUNDAY -> Boolean.TRUE.equals(horario.getDomingo());
        };
    }

    private LocalDateTime inicioProximoDiaUtil(LocalDate aPartirDe, HorarioUtil horario) {
        LocalDate data = aPartirDe;
        int limiteDias = 370;
        int tentativas = 0;
        while (!isDiaUtil(data, horario)) {
            data = data.plusDays(1);
            tentativas++;
            if (tentativas > limiteDias) {
                throw new IllegalStateException("Não foi possível encontrar próximo dia útil no horizonte esperado.");
            }
        }
        return LocalDateTime.of(data, horario.getHoraInicio());
    }

    private LocalDateTime parseDateTime(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Informe o parâmetro inicio no formato YYYY-MM-DDTHH:mm:ss.");
        }
        try {
            return LocalDateTime.parse(valor.trim(), DATE_TIME_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Data/hora inicial inválida. Use YYYY-MM-DDTHH:mm:ss.");
        }
    }

    private String formatDateTime(LocalDateTime dataHora) {
        return dataHora.format(DATE_TIME_FORMAT);
    }
}

package com.suporte.tickets.service;

import com.suporte.tickets.dto.HorarioUtilDTO;
import com.suporte.tickets.entity.HorarioUtil;
import com.suporte.tickets.repository.HorarioUtilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class HorarioUtilService {

    private static final DateTimeFormatter HORA_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private final HorarioUtilRepository horarioUtilRepository;

    @Transactional
    public HorarioUtilDTO obterPadrao() {
        HorarioUtil entidade = obterOuCriarEntidadePadrao();
        return toDto(entidade);
    }

    @Transactional
    public HorarioUtilDTO atualizarPadrao(HorarioUtilDTO dto) {
        validar(dto);
        if (dto.getAtivo() != null && !dto.getAtivo()) {
            throw new IllegalArgumentException("O horário útil padrão deve permanecer ativo.");
        }
        HorarioUtil entidade = obterOuCriarEntidadePadrao();
        aplicarDto(entidade, dto);
        return toDto(horarioUtilRepository.save(entidade));
    }

    @Transactional
    public HorarioUtil obterOuCriarEntidadePadrao() {
        return horarioUtilRepository.findFirstByAtivoTrueOrderByIdAsc()
                .or(() -> horarioUtilRepository.findFirstByOrderByIdAsc())
                .orElseGet(() -> horarioUtilRepository.save(criarEntidadePadrao()));
    }

    private HorarioUtil criarEntidadePadrao() {
        HorarioUtil padrao = new HorarioUtil();
        padrao.setNome("Horário Comercial");
        padrao.setHoraInicio(LocalTime.of(8, 0));
        padrao.setHoraFim(LocalTime.of(18, 0));
        padrao.setSegunda(true);
        padrao.setTerca(true);
        padrao.setQuarta(true);
        padrao.setQuinta(true);
        padrao.setSexta(true);
        padrao.setSabado(false);
        padrao.setDomingo(false);
        padrao.setAtivo(true);
        return padrao;
    }

    private void validar(HorarioUtilDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados do horário útil são obrigatórios.");
        }
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome da configuração.");
        }
        LocalTime inicio = parseHora(dto.getHoraInicio(), "hora inicial");
        LocalTime fim = parseHora(dto.getHoraFim(), "hora final");
        if (!inicio.isBefore(fim)) {
            throw new IllegalArgumentException("A hora inicial deve ser menor que a hora final.");
        }
        if (!temDiaAtivo(dto)) {
            throw new IllegalArgumentException("Selecione pelo menos um dia da semana.");
        }
    }

    private boolean temDiaAtivo(HorarioUtilDTO dto) {
        return Boolean.TRUE.equals(dto.getSegunda())
                || Boolean.TRUE.equals(dto.getTerca())
                || Boolean.TRUE.equals(dto.getQuarta())
                || Boolean.TRUE.equals(dto.getQuinta())
                || Boolean.TRUE.equals(dto.getSexta())
                || Boolean.TRUE.equals(dto.getSabado())
                || Boolean.TRUE.equals(dto.getDomingo());
    }

    private LocalTime parseHora(String valor, String rotulo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Informe a " + rotulo + ".");
        }
        String normalizado = valor.trim();
        if (normalizado.length() == 5 && normalizado.charAt(2) == ':') {
            // ok
        } else if (normalizado.length() >= 8) {
            normalizado = normalizado.substring(0, 5);
        }
        try {
            return LocalTime.parse(normalizado, HORA_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Formato de " + rotulo + " inválido. Use HH:mm.");
        }
    }

    private void aplicarDto(HorarioUtil entidade, HorarioUtilDTO dto) {
        entidade.setNome(dto.getNome().trim());
        entidade.setHoraInicio(parseHora(dto.getHoraInicio(), "hora inicial"));
        entidade.setHoraFim(parseHora(dto.getHoraFim(), "hora final"));
        entidade.setSegunda(Boolean.TRUE.equals(dto.getSegunda()));
        entidade.setTerca(Boolean.TRUE.equals(dto.getTerca()));
        entidade.setQuarta(Boolean.TRUE.equals(dto.getQuarta()));
        entidade.setQuinta(Boolean.TRUE.equals(dto.getQuinta()));
        entidade.setSexta(Boolean.TRUE.equals(dto.getSexta()));
        entidade.setSabado(Boolean.TRUE.equals(dto.getSabado()));
        entidade.setDomingo(Boolean.TRUE.equals(dto.getDomingo()));
        entidade.setAtivo(true);
    }

    private HorarioUtilDTO toDto(HorarioUtil entidade) {
        HorarioUtilDTO dto = new HorarioUtilDTO();
        dto.setId(entidade.getId());
        dto.setNome(entidade.getNome());
        dto.setHoraInicio(formatHora(entidade.getHoraInicio()));
        dto.setHoraFim(formatHora(entidade.getHoraFim()));
        dto.setSegunda(entidade.getSegunda());
        dto.setTerca(entidade.getTerca());
        dto.setQuarta(entidade.getQuarta());
        dto.setQuinta(entidade.getQuinta());
        dto.setSexta(entidade.getSexta());
        dto.setSabado(entidade.getSabado());
        dto.setDomingo(entidade.getDomingo());
        dto.setAtivo(entidade.getAtivo());
        return dto;
    }

    private String formatHora(LocalTime time) {
        if (time == null) {
            return null;
        }
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}

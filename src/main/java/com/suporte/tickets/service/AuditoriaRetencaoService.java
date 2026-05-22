package com.suporte.tickets.service;

import com.suporte.tickets.dto.AuditoriaRetencaoContagemDTO;
import com.suporte.tickets.dto.AuditoriaRetencaoExclusaoDTO;
import com.suporte.tickets.repository.AuditoriaEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Retenção manual de eventos em {@code auditoria_eventos}: contagem e exclusão por data limite.
 * Não há exclusão automática nem scheduler (Sprint 69).
 */
@Service
@RequiredArgsConstructor
public class AuditoriaRetencaoService {

    /** Eventos dos últimos N dias não podem ser excluídos por limpeza manual. */
    public static final int DIAS_PROTECAO_EXCLUSAO = 30;

    private final AuditoriaEventoRepository auditoriaEventoRepository;

    public AuditoriaRetencaoContagemDTO contarAntigos(LocalDate antesDe) {
        LocalDate data = exigirAntesDe(antesDe);
        LocalDateTime limite = inicioDoDia(data);
        long quantidade = auditoriaEventoRepository.countByDataHoraBefore(limite);
        return new AuditoriaRetencaoContagemDTO(data, quantidade);
    }

    @Transactional
    public AuditoriaRetencaoExclusaoDTO excluirAntigos(LocalDate antesDe, boolean confirmar) {
        if (!confirmar) {
            throw new IllegalArgumentException(
                    "Exclusao nao realizada. Informe confirmar=true para excluir eventos antigos de auditoria.");
        }
        LocalDate data = exigirAntesDe(antesDe);
        validarDataLimitePermitidaParaExclusao(data);
        LocalDateTime limite = inicioDoDia(data);
        long excluidos = auditoriaEventoRepository.deleteByDataHoraBefore(limite);
        return new AuditoriaRetencaoExclusaoDTO(data, excluidos);
    }

    static LocalDate exigirAntesDe(LocalDate antesDe) {
        if (antesDe == null) {
            throw new IllegalArgumentException("Parametro antesDe (YYYY-MM-DD) e obrigatorio.");
        }
        return antesDe;
    }

    static LocalDateTime inicioDoDia(LocalDate dia) {
        return dia.atStartOfDay();
    }

    static void validarDataLimitePermitidaParaExclusao(LocalDate antesDe) {
        LocalDate limiteMaximo = LocalDate.now().minusDays(DIAS_PROTECAO_EXCLUSAO);
        if (antesDe.isAfter(limiteMaximo)) {
            throw new IllegalArgumentException(
                    "Data limite muito recente. A exclusao so e permitida para eventos anteriores a "
                            + limiteMaximo + " (protecao dos ultimos " + DIAS_PROTECAO_EXCLUSAO + " dias).");
        }
    }
}

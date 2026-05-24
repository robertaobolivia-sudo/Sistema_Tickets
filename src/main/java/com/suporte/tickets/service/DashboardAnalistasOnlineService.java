package com.suporte.tickets.service;

import com.suporte.tickets.dto.DashboardAnalistaOnlineCardDTO;
import com.suporte.tickets.dto.DashboardAnalistasOnlineDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.PerfilAcesso;
import com.suporte.tickets.entity.StatusOperador;
import com.suporte.tickets.repository.AnalistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Sprint 281 — quadro de analistas no Dashboard operacional.
 */
@Service
@RequiredArgsConstructor
public class DashboardAnalistasOnlineService {

    public static final String STATUS_EXIBICAO_ONLINE = "ONLINE";
    public static final String STATUS_EXIBICAO_OCUPADO = "OCUPADO";
    public static final String STATUS_EXIBICAO_AUSENTE = "AUSENTE";
    public static final String STATUS_EXIBICAO_OFFLINE = "OFFLINE";

    private static final String ANALISTA_PADRAO_EMAIL = "analista@suporte.local";

    private final AnalistaRepository analistaRepository;

    @Transactional(readOnly = true)
    public DashboardAnalistasOnlineDTO obter() {
        DashboardAnalistasOnlineDTO dto = new DashboardAnalistasOnlineDTO();
        List<Analista> analistas = analistaRepository.findByAtivoTrueOrderByNomeAsc();
        List<DashboardAnalistaOnlineCardDTO> operadores = new ArrayList<>();

        for (Analista analista : analistas) {
            if (analista.getEmail() != null
                    && ANALISTA_PADRAO_EMAIL.equalsIgnoreCase(analista.getEmail().trim())) {
                continue;
            }
            String statusExibicao = resolverStatusExibicaoManual(analista);
            DashboardAnalistaOnlineCardDTO card = new DashboardAnalistaOnlineCardDTO(
                    analista.getId(),
                    textoNome(analista),
                    textoCargo(analista),
                    analista.getFotoUrl(),
                    statusExibicao,
                    0);

            operadores.add(card);
            switch (statusExibicao) {
                case STATUS_EXIBICAO_ONLINE -> dto.getOnline().add(card);
                case STATUS_EXIBICAO_OCUPADO -> dto.getOcupado().add(card);
                case STATUS_EXIBICAO_AUSENTE -> dto.getAusente().add(card);
                default -> dto.getOffline().add(card);
            }
        }

        Comparator<DashboardAnalistaOnlineCardDTO> porStatusENome = Comparator
                .comparingInt((DashboardAnalistaOnlineCardDTO c) -> rankStatusExibicao(c.getStatusExibicao()))
                .thenComparing(
                        c -> c.getNome() != null ? c.getNome().toLowerCase(Locale.ROOT) : "",
                        Comparator.naturalOrder());
        operadores.sort(porStatusENome);
        dto.setOperadores(operadores);
        Comparator<DashboardAnalistaOnlineCardDTO> porNome =
                Comparator.comparing(c -> c.getNome() != null ? c.getNome().toLowerCase(Locale.ROOT) : "");
        dto.getOnline().sort(porNome);
        dto.getOcupado().sort(porNome);
        dto.getAusente().sort(porNome);
        dto.getOffline().sort(porNome);
        return dto;
    }

    /** Sprint 286 — status definido manualmente pelo operador (sem derivação por ticket/sessão). */
    static String resolverStatusExibicaoManual(Analista analista) {
        StatusOperador base = analista.getStatusOperador() != null
                ? analista.getStatusOperador()
                : (Boolean.TRUE.equals(analista.getOnline()) ? StatusOperador.ONLINE : StatusOperador.OFFLINE);
        return switch (base) {
            case ONLINE -> STATUS_EXIBICAO_ONLINE;
            case OCUPADO -> STATUS_EXIBICAO_OCUPADO;
            case AUSENTE -> STATUS_EXIBICAO_AUSENTE;
            default -> STATUS_EXIBICAO_OFFLINE;
        };
    }

    static int rankStatusExibicao(String status) {
        if (STATUS_EXIBICAO_ONLINE.equals(status)) {
            return 0;
        }
        if (STATUS_EXIBICAO_OCUPADO.equals(status)) {
            return 1;
        }
        if (STATUS_EXIBICAO_AUSENTE.equals(status)) {
            return 2;
        }
        return 3;
    }

    static String textoNome(Analista analista) {
        if (analista.getNomeCompleto() != null && !analista.getNomeCompleto().isBlank()) {
            return analista.getNomeCompleto().trim();
        }
        if (analista.getNome() != null && !analista.getNome().isBlank()) {
            return analista.getNome().trim();
        }
        return "Analista";
    }

    static String textoCargo(Analista analista) {
        PerfilAcesso perfil = AnalistaService.resolverPerfilAcesso(analista);
        String perfilLabel = switch (perfil) {
            case ADMIN -> "Administrador";
            case SUPERVISOR -> "Supervisor";
            case ANALISTA -> "Analista";
        };
        String nivel = analista.getNivel();
        if (nivel != null && !nivel.isBlank()) {
            return perfilLabel + " · " + nivel.trim();
        }
        return perfilLabel;
    }
}

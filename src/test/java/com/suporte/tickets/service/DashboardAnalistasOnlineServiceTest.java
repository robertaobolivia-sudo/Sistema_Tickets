package com.suporte.tickets.service;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.StatusOperador;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardAnalistasOnlineServiceTest {

    @Test
    void statusManualOnline() {
        Analista a = new Analista();
        a.setStatusOperador(StatusOperador.ONLINE);
        assertEquals(
                DashboardAnalistasOnlineService.STATUS_EXIBICAO_ONLINE,
                DashboardAnalistasOnlineService.resolverStatusExibicaoManual(a));
    }

    @Test
    void statusManualOcupado() {
        Analista a = new Analista();
        a.setStatusOperador(StatusOperador.OCUPADO);
        assertEquals(
                DashboardAnalistasOnlineService.STATUS_EXIBICAO_OCUPADO,
                DashboardAnalistasOnlineService.resolverStatusExibicaoManual(a));
    }

    @Test
    void statusManualAusente() {
        Analista a = new Analista();
        a.setStatusOperador(StatusOperador.AUSENTE);
        assertEquals(
                DashboardAnalistasOnlineService.STATUS_EXIBICAO_AUSENTE,
                DashboardAnalistasOnlineService.resolverStatusExibicaoManual(a));
    }

    @Test
    void statusManualOffline() {
        Analista a = new Analista();
        a.setStatusOperador(StatusOperador.OFFLINE);
        assertEquals(
                DashboardAnalistasOnlineService.STATUS_EXIBICAO_OFFLINE,
                DashboardAnalistasOnlineService.resolverStatusExibicaoManual(a));
    }

    @Test
    void rankStatusOrdem() {
        assertEquals(0, DashboardAnalistasOnlineService.rankStatusExibicao("ONLINE"));
        assertEquals(1, DashboardAnalistasOnlineService.rankStatusExibicao("OCUPADO"));
        assertEquals(2, DashboardAnalistasOnlineService.rankStatusExibicao("AUSENTE"));
        assertEquals(3, DashboardAnalistasOnlineService.rankStatusExibicao("OFFLINE"));
    }
}

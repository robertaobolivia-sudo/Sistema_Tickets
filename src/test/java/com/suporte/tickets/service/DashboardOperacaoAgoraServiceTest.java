package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardOperacaoAgoraServiceTest {

    @Test
    void formatarMediaVazioRetornaTraco() {
        assertEquals("-", DashboardOperacaoAgoraService.formatarMedia(List.of()));
    }

    @Test
    void formatarMediaUmaHora() {
        assertEquals("01:00:00", DashboardOperacaoAgoraService.formatarMedia(List.of(3600L)));
    }
}

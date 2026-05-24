package com.suporte.tickets.service;

import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketStatusTransicaoServiceTest {

    private TicketStatusTransicaoService service;

    @BeforeEach
    void setUp() {
        service = new TicketStatusTransicaoService();
    }

    @Test
    void manual_aberto_para_emAtendimento() {
        assertDoesNotThrow(() -> service.validarTransicao(
                TicketStatus.ABERTO,
                TicketStatus.EM_ATENDIMENTO,
                TicketStatusTransicaoService.MotivoTransicao.ATUALIZACAO_MANUAL));
    }

    @Test
    void manual_emAtendimento_para_aguardandoCliente() {
        assertDoesNotThrow(() -> service.validarTransicao(
                TicketStatus.EM_ATENDIMENTO,
                TicketStatus.AGUARDANDO_CLIENTE,
                TicketStatusTransicaoService.MotivoTransicao.ATUALIZACAO_MANUAL));
    }

    @Test
    void manual_aguardandoCliente_para_emAtendimento() {
        assertDoesNotThrow(() -> service.validarTransicao(
                TicketStatus.AGUARDANDO_CLIENTE,
                TicketStatus.EM_ATENDIMENTO,
                TicketStatusTransicaoService.MotivoTransicao.ATUALIZACAO_MANUAL));
    }

    @Test
    void encerramento_emAtendimento_para_resolvido() {
        assertDoesNotThrow(() -> service.validarTransicao(
                TicketStatus.EM_ATENDIMENTO,
                TicketStatus.RESOLVIDO,
                TicketStatusTransicaoService.MotivoTransicao.ENCERRAMENTO));
    }

    @Test
    void reabertura_resolvido_para_aberto() {
        assertDoesNotThrow(() -> service.validarTransicao(
                TicketStatus.RESOLVIDO,
                TicketStatus.ABERTO,
                TicketStatusTransicaoService.MotivoTransicao.REABERTURA));
    }

    @Test
    void classificacao_ativo_para_indevido() {
        assertDoesNotThrow(() -> service.validarTransicao(
                TicketStatus.EM_ATENDIMENTO,
                TicketStatus.INDEVIDO,
                TicketStatusTransicaoService.MotivoTransicao.CLASSIFICACAO_INDEVIDO));
    }

    @Test
    void manual_bloqueia_resolvido_via_put() {
        assertThrows(IllegalArgumentException.class, () -> service.validarTransicao(
                TicketStatus.EM_ATENDIMENTO,
                TicketStatus.RESOLVIDO,
                TicketStatusTransicaoService.MotivoTransicao.ATUALIZACAO_MANUAL));
    }

    @Test
    void manual_bloqueia_indevido_via_put() {
        assertThrows(IllegalArgumentException.class, () -> service.validarTransicao(
                TicketStatus.ABERTO,
                TicketStatus.INDEVIDO,
                TicketStatusTransicaoService.MotivoTransicao.ATUALIZACAO_MANUAL));
    }

    @Test
    void indevido_nao_volta_para_ativo() {
        assertFalse(service.isTransicaoPermitida(
                TicketStatus.INDEVIDO,
                TicketStatus.EM_ATENDIMENTO,
                TicketStatusTransicaoService.MotivoTransicao.ATUALIZACAO_MANUAL));
        assertThrows(IllegalArgumentException.class, () -> service.validarTransicao(
                TicketStatus.INDEVIDO,
                TicketStatus.ABERTO,
                TicketStatusTransicaoService.MotivoTransicao.REABERTURA));
    }

    @Test
    void resolvido_nao_vai_direto_para_emAtendimento() {
        assertThrows(IllegalArgumentException.class, () -> service.validarTransicao(
                TicketStatus.RESOLVIDO,
                TicketStatus.EM_ATENDIMENTO,
                TicketStatusTransicaoService.MotivoTransicao.ATUALIZACAO_MANUAL));
    }

    @Test
    void encerramento_rejeita_de_resolvido() {
        assertThrows(IllegalArgumentException.class, () -> service.validarTransicao(
                TicketStatus.RESOLVIDO,
                TicketStatus.RESOLVIDO,
                TicketStatusTransicaoService.MotivoTransicao.ENCERRAMENTO));
        assertFalse(service.isTransicaoPermitida(
                TicketStatus.RESOLVIDO,
                TicketStatus.RESOLVIDO,
                TicketStatusTransicaoService.MotivoTransicao.ENCERRAMENTO));
    }

    @Test
    void encerramento_aberto_para_resolvido() {
        assertDoesNotThrow(() -> service.validarTransicao(
                TicketStatus.ABERTO,
                TicketStatus.RESOLVIDO,
                TicketStatusTransicaoService.MotivoTransicao.ENCERRAMENTO));
    }

    @Test
    void isStatusAtivoOperacional() {
        assertTrue(service.isStatusAtivoOperacional(TicketStatus.ABERTO));
        assertFalse(service.isStatusAtivoOperacional(TicketStatus.INDEVIDO));
    }
}

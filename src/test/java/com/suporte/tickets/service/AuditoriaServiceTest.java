package com.suporte.tickets.service;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.AuditoriaEvento;
import com.suporte.tickets.entity.PerfilAcesso;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.AuditoriaEventoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaEventoRepository auditoriaEventoRepository;

    @Mock
    private AnalistaRepository analistaRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @Test
    void registrar_persisteEvento() {
        Analista analista = new Analista();
        analista.setId(1L);
        analista.setNome("Admin");
        analista.setPerfilAcesso(PerfilAcesso.ADMIN);

        auditoriaService.registrar(
                AuditoriaService.ACAO_LOGIN_SUCESSO,
                AuditoriaService.ENTIDADE_ANALISTA,
                "1",
                "Login bem-sucedido",
                analista);

        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(auditoriaEventoRepository).save(captor.capture());
        AuditoriaEvento salvo = captor.getValue();
        assertEquals(AuditoriaService.ACAO_LOGIN_SUCESSO, salvo.getAcao());
        assertEquals(1L, salvo.getAnalistaId());
        assertEquals("ADMIN", salvo.getPerfilAcesso());
    }

    @Test
    void registrar_falhaNoRepositorio_naoPropagaExcecao() {
        doThrow(new RuntimeException("db")).when(auditoriaEventoRepository).save(any());
        when(analistaRepository.findById(1L)).thenReturn(Optional.of(new Analista()));

        assertDoesNotThrow(() -> auditoriaService.registrar(
                AuditoriaService.ACAO_LOGOUT,
                AuditoriaService.ENTIDADE_ANALISTA,
                "1",
                "Logout",
                1L));
    }
}

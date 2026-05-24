package com.suporte.tickets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suporte.tickets.config.GlobalExceptionHandler;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.exception.AcessoNegadoException;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WebhookControllerAuthTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TicketService ticketService;

    @Mock
    private PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WebhookController controller = new WebhookController(ticketService, perfilAcessoAutorizacaoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void postTickets_semSessao_bloqueadoAntesDeCriarTicket() throws Exception {
        when(perfilAcessoAutorizacaoService.exigirSessaoValida(isNull(), isNull()))
                .thenThrow(new AcessoNegadoException("Sessao invalida ou expirada. Faca login novamente."));

        TicketWebhookRequestDTO body = new TicketWebhookRequestDTO();
        body.setCliente("Cliente Teste");
        body.setMensagem("Mensagem teste");

        mockMvc.perform(post("/api/webhooks/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());

        verify(ticketService, never()).criarTicketPorWebhook(any());
    }

    @Test
    void postTickets_comSessaoValida_criaTicket() throws Exception {
        Analista analista = new Analista();
        analista.setId(1L);
        when(perfilAcessoAutorizacaoService.exigirSessaoValida(eq(1L), eq("token-valido")))
                .thenReturn(analista);

        TicketResponseDTO criado = new TicketResponseDTO();
        criado.setNumeroTicket("TK-000099");
        when(ticketService.criarTicketPorWebhook(any(TicketWebhookRequestDTO.class))).thenReturn(criado);

        TicketWebhookRequestDTO body = new TicketWebhookRequestDTO();
        body.setCliente("Cliente Teste");
        body.setMensagem("Mensagem teste");

        mockMvc.perform(post("/api/webhooks/tickets")
                        .header(PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, "1")
                        .header(PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, "token-valido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        verify(ticketService).criarTicketPorWebhook(any(TicketWebhookRequestDTO.class));
    }
}

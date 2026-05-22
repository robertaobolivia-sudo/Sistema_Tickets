package com.suporte.tickets.controller;

import com.suporte.tickets.dto.TicketSatisfacaoEvolucaoDiaDTO;
import com.suporte.tickets.dto.TicketSatisfacaoResumoDTO;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.TicketSatisfacaoCsvService;
import com.suporte.tickets.service.TicketSatisfacaoEvolucaoService;
import com.suporte.tickets.service.TicketSatisfacaoResumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tickets/satisfacao")
@RequiredArgsConstructor
public class TicketSatisfacaoResumoController {

    private final TicketSatisfacaoResumoService ticketSatisfacaoResumoService;
    private final TicketSatisfacaoCsvService ticketSatisfacaoCsvService;
    private final TicketSatisfacaoEvolucaoService ticketSatisfacaoEvolucaoService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    /**
     * GET /api/tickets/satisfacao/resumo — indicadores gerenciais (filtro por criadoEm da avaliação).
     */
    @GetMapping("/resumo")
    public ResponseEntity<TicketSatisfacaoResumoDTO> resumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Integer nota,
            @RequestParam(required = false) String statusTicket,
            @RequestParam(required = false) String termoCliente,
            @RequestParam(required = false) Integer clienteId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(
                ticketSatisfacaoResumoService.calcularResumo(
                        dataInicio, dataFim, nota, statusTicket, termoCliente, clienteId));
    }

    /**
     * GET /api/tickets/satisfacao/evolucao — média e totais agrupados por dia (criadoEm da avaliação).
     */
    @GetMapping("/evolucao")
    public ResponseEntity<List<TicketSatisfacaoEvolucaoDiaDTO>> evolucao(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Integer nota,
            @RequestParam(required = false) String statusTicket,
            @RequestParam(required = false) String termoCliente,
            @RequestParam(required = false) Integer clienteId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(ticketSatisfacaoEvolucaoService.calcularEvolucao(
                dataInicio, dataFim, nota, statusTicket, termoCliente, clienteId));
    }

    /**
     * GET /api/tickets/satisfacao/csv — exportação de avaliações (UTF-8 BOM, separador ;).
     */
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Integer nota,
            @RequestParam(required = false) String statusTicket,
            @RequestParam(required = false) String termoCliente,
            @RequestParam(required = false) Integer clienteId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        byte[] csv = ticketSatisfacaoCsvService.gerarCsv(
                dataInicio, dataFim, nota, statusTicket, termoCliente, clienteId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"satisfacao-tickets.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }
}

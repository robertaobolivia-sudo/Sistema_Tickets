package com.suporte.tickets.controller;

import com.suporte.tickets.dto.AnalistaFilaDTO;
import com.suporte.tickets.dto.ClientePendenciasDTO;
import com.suporte.tickets.dto.DashboardGerencialDTO;
import com.suporte.tickets.dto.DashboardAnalistasOnlineDTO;
import com.suporte.tickets.dto.DashboardOperacaoAgoraDTO;
import com.suporte.tickets.dto.DashboardOperacaoClienteB2BDTO;
import com.suporte.tickets.dto.DashboardResumoDTO;
import com.suporte.tickets.dto.DashboardSatisfacaoResumoDTO;
import com.suporte.tickets.dto.DashboardAvaliacaoTempoRealDTO;
import com.suporte.tickets.dto.DashboardEncerramentosDiaDTO;
import com.suporte.tickets.dto.DashboardSlaDTO;
import com.suporte.tickets.service.AnalistaService;
import com.suporte.tickets.service.DashboardAnalistasOnlineService;
import com.suporte.tickets.service.DashboardOperacaoAgoraService;
import com.suporte.tickets.service.DashboardOperacaoClienteB2BService;
import com.suporte.tickets.service.DashboardService;
import com.suporte.tickets.service.DashboardAvaliacaoTempoRealService;
import com.suporte.tickets.service.DashboardEncerramentosDiaService;
import com.suporte.tickets.service.DashboardSlaService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AnalistaService analistaService;
    private final DashboardService dashboardService;
    private final DashboardSlaService dashboardSlaService;
    private final DashboardEncerramentosDiaService dashboardEncerramentosDiaService;
    private final DashboardAvaliacaoTempoRealService dashboardAvaliacaoTempoRealService;
    private final DashboardOperacaoAgoraService dashboardOperacaoAgoraService;
    private final DashboardAnalistasOnlineService dashboardAnalistasOnlineService;
    private final DashboardOperacaoClienteB2BService dashboardOperacaoClienteB2BService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @GetMapping("/operacao-cliente-b2b")
    public ResponseEntity<DashboardOperacaoClienteB2BDTO> obterOperacaoClienteB2b(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardOperacaoClienteB2BService.obter());
    }

    @GetMapping("/analistas-online")
    public ResponseEntity<DashboardAnalistasOnlineDTO> obterAnalistasOnline(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardAnalistasOnlineService.obter());
    }

    @GetMapping("/operacao-agora")
    public ResponseEntity<DashboardOperacaoAgoraDTO> obterOperacaoAgora(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardOperacaoAgoraService.obter());
    }

    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoDTO> obterResumo(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardService.obterResumo());
    }

    @GetMapping("/encerramento-satisfacao")
    public ResponseEntity<DashboardSatisfacaoResumoDTO> obterEncerramentoSatisfacao(
            @RequestParam(required = false) Integer dias,
            @RequestParam(required = false) Integer clienteId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardService.obterEncerramentoSatisfacaoResumo(dias, clienteId));
    }

    @GetMapping("/gerencial")
    public ResponseEntity<DashboardGerencialDTO> obterGerencial(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardService.obterGerencial());
    }

    @GetMapping("/avaliacao-tempo-real")
    public ResponseEntity<DashboardAvaliacaoTempoRealDTO> obterAvaliacaoTempoReal(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardAvaliacaoTempoRealService.obter());
    }

    @GetMapping("/encerramentos-dia")
    public ResponseEntity<DashboardEncerramentosDiaDTO> obterEncerramentosDia(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardEncerramentosDiaService.obter());
    }

    @GetMapping("/sla")
    public ResponseEntity<DashboardSlaDTO> obterSla(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardSlaService.obterResumoSla());
    }

    @GetMapping("/filas-analistas")
    public ResponseEntity<List<AnalistaFilaDTO>> listarFilasAnalistas(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(analistaService.listarFilasAnalistasOnline());
    }

    @GetMapping("/clientes-pendencias")
    public ResponseEntity<List<ClientePendenciasDTO>> listarPendenciasPorCliente(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardService.listarPendenciasPorCliente());
    }
}

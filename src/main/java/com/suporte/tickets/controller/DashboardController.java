package com.suporte.tickets.controller;

import com.suporte.tickets.dto.AnalistaFilaDTO;
import com.suporte.tickets.dto.ConexaoPendenciasDTO;
import com.suporte.tickets.dto.DashboardGerencialDTO;
import com.suporte.tickets.dto.DashboardResumoDTO;
import com.suporte.tickets.dto.DashboardSatisfacaoResumoDTO;
import com.suporte.tickets.dto.DashboardSlaDTO;
import com.suporte.tickets.service.AnalistaService;
import com.suporte.tickets.service.DashboardService;
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
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

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

    @GetMapping("/conexoes-pendencias")
    public ResponseEntity<List<ConexaoPendenciasDTO>> listarPendenciasPorConexao(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(dashboardService.listarPendenciasPorConexao());
    }
}

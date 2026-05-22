package com.suporte.tickets.controller;

import com.suporte.tickets.dto.IndicadoresChamadosDTO;
import com.suporte.tickets.dto.IndicadoresEncerramentoAvaliacaoDTO;
import com.suporte.tickets.service.IndicadoresChamadosService;
import com.suporte.tickets.service.IndicadoresEncerramentoAvaliacaoService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/indicadores")
@RequiredArgsConstructor
public class IndicadoresController {

    private final IndicadoresChamadosService indicadoresChamadosService;
    private final IndicadoresEncerramentoAvaliacaoService indicadoresEncerramentoAvaliacaoService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @GetMapping("/chamados")
    public ResponseEntity<IndicadoresChamadosDTO> chamados(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String classificacaoCliente,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.ok(indicadoresChamadosService.obterChamados(dataInicio, dataFim, classificacaoCliente));
    }

    @GetMapping("/encerramento-avaliacao")
    public ResponseEntity<IndicadoresEncerramentoAvaliacaoDTO> encerramentoAvaliacao(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Long motivoId,
            @RequestParam(required = false) String statusPesquisa,
            @RequestParam(required = false) Integer notaAvaliacao,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.ok(indicadoresEncerramentoAvaliacaoService.obter(
                dataInicio, dataFim, clienteId, motivoId, statusPesquisa, notaAvaliacao));
    }
}

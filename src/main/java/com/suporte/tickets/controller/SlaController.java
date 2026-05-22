package com.suporte.tickets.controller;

import com.suporte.tickets.dto.SlaCalculoTesteResponseDTO;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.SlaTempoUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sla")
@RequiredArgsConstructor
public class SlaController {

    private final SlaTempoUtilService slaTempoUtilService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @GetMapping("/calcular-vencimento-teste")
    public ResponseEntity<SlaCalculoTesteResponseDTO> calcularVencimentoTeste(
            @RequestParam String inicio,
            @RequestParam long minutos,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(slaTempoUtilService.calcularVencimentoTeste(inicio, minutos));
    }
}

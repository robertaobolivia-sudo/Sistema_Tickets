package com.suporte.tickets.controller;

import com.suporte.tickets.dto.HorarioUtilDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.service.AuditoriaService;
import com.suporte.tickets.service.HorarioUtilService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/horarios-uteis")
@RequiredArgsConstructor
public class HorarioUtilController {

    private final HorarioUtilService horarioUtilService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;
    private final AuditoriaService auditoriaService;

    @GetMapping("/padrao")
    public ResponseEntity<HorarioUtilDTO> obterPadrao(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        return ResponseEntity.ok(horarioUtilService.obterPadrao());
    }

    @PutMapping("/padrao")
    public ResponseEntity<HorarioUtilDTO> atualizarPadrao(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestBody HorarioUtilDTO dto) {
        perfilAcessoAutorizacaoService.exigirAdmin(analistaId, analistaToken);
        Analista executor = perfilAcessoAutorizacaoService.validarSessao(analistaId, analistaToken);
        HorarioUtilDTO atualizado = horarioUtilService.atualizarPadrao(dto);
        auditoriaService.registrar(
                AuditoriaService.ACAO_CONFIG_HORARIO_UTIL,
                AuditoriaService.ENTIDADE_CONFIG,
                "padrao",
                "Horario util padrao atualizado",
                executor);
        return ResponseEntity.ok(atualizado);
    }
}

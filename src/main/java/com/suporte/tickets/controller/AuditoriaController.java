package com.suporte.tickets.controller;

import com.suporte.tickets.dto.AuditoriaEventoFiltroDTO;
import com.suporte.tickets.dto.AuditoriaEventoPageDTO;
import com.suporte.tickets.dto.AuditoriaRetencaoContagemDTO;
import com.suporte.tickets.dto.AuditoriaRetencaoExclusaoDTO;
import com.suporte.tickets.service.AuditoriaConsultaService;
import com.suporte.tickets.service.AuditoriaEventoCsvService;
import com.suporte.tickets.service.AuditoriaRetencaoService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaConsultaService auditoriaConsultaService;
    private final AuditoriaEventoCsvService auditoriaEventoCsvService;
    private final AuditoriaRetencaoService auditoriaRetencaoService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    /**
     * GET /api/auditoria/eventos — consulta read-only, somente ADMIN.
     */
    @GetMapping("/eventos")
    public ResponseEntity<AuditoriaEventoPageDTO> listarEventos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Long analistaId,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) String entidadeId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(required = false) Integer limite,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long headerAnalistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(headerAnalistaId, analistaToken);

        AuditoriaEventoFiltroDTO filtro = montarFiltro(dataInicio, dataFim, analistaId, acao, entidade, entidadeId);

        int limiteEfetivo = limite != null ? limite : AuditoriaConsultaService.LIMITE_PADRAO;
        return ResponseEntity.ok(auditoriaConsultaService.listar(filtro, pagina, limiteEfetivo));
    }

    /**
     * GET /api/auditoria/eventos/csv — exportação CSV, somente ADMIN (máx. 5000 registros).
     */
    @GetMapping("/eventos/csv")
    public ResponseEntity<byte[]> exportarEventosCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Long analistaId,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) String entidadeId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long headerAnalistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(headerAnalistaId, analistaToken);

        AuditoriaEventoFiltroDTO filtro = montarFiltro(dataInicio, dataFim, analistaId, acao, entidade, entidadeId);
        byte[] csv = auditoriaEventoCsvService.gerarCsv(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"auditoria-eventos.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    /**
     * GET /api/auditoria/eventos/contar-antigos?antesDe=YYYY-MM-DD — quantidade com dataHora anterior ao dia informado.
     */
    @GetMapping("/eventos/contar-antigos")
    public ResponseEntity<AuditoriaRetencaoContagemDTO> contarEventosAntigos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate antesDe,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long headerAnalistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(headerAnalistaId, analistaToken);
        return ResponseEntity.ok(auditoriaRetencaoService.contarAntigos(antesDe));
    }

    /**
     * DELETE /api/auditoria/eventos/antigos?antesDe=YYYY-MM-DD&confirmar=true — exclusão manual (sem scheduler).
     */
    @DeleteMapping("/eventos/antigos")
    public ResponseEntity<AuditoriaRetencaoExclusaoDTO> excluirEventosAntigos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate antesDe,
            @RequestParam(required = false, defaultValue = "false") boolean confirmar,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long headerAnalistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdmin(headerAnalistaId, analistaToken);
        return ResponseEntity.ok(auditoriaRetencaoService.excluirAntigos(antesDe, confirmar));
    }

    private static AuditoriaEventoFiltroDTO montarFiltro(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Long analistaId,
            String acao,
            String entidade,
            String entidadeId) {
        AuditoriaEventoFiltroDTO filtro = new AuditoriaEventoFiltroDTO();
        filtro.setDataInicio(dataInicio);
        filtro.setDataFim(dataFim);
        filtro.setAnalistaId(analistaId);
        filtro.setAcao(acao);
        filtro.setEntidade(entidade);
        filtro.setEntidadeId(entidadeId);
        return filtro;
    }
}

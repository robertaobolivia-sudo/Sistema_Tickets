package com.suporte.tickets.controller;

import com.suporte.tickets.dto.AtualizarStatusRequestDTO;
import com.suporte.tickets.dto.EncerrarTicketRequestDTO;
import com.suporte.tickets.dto.TicketAlertaDTO;
import com.suporte.tickets.dto.TicketAlertaReferenciaDTO;
import com.suporte.tickets.dto.TicketEscalonamentoRequestDTO;
import com.suporte.tickets.dto.EtiquetaResponseDTO;
import com.suporte.tickets.dto.TicketEtiquetasRequestDTO;
import com.suporte.tickets.dto.TicketObservacaoAtendimentoRequestDTO;
import com.suporte.tickets.dto.TicketFiltroDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.service.AuditoriaService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.TicketBuscaService;
import com.suporte.tickets.service.TicketPdfService;
import com.suporte.tickets.service.TicketRelatorioCsvService;
import com.suporte.tickets.service.TicketAtivoService;
import com.suporte.tickets.service.TicketEtiquetaService;
import com.suporte.tickets.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller para gerenciamento de tickets
 * 
 * Implementa endpoints para listar, buscar, atualizar status e encerrar tickets
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketEtiquetaService ticketEtiquetaService;
    private final TicketAtivoService ticketAtivoService;
    private final TicketBuscaService ticketBuscaService;
    private final TicketPdfService ticketPdfService;
    private final TicketRelatorioCsvService ticketRelatorioCsvService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;
    private final AuditoriaService auditoriaService;

    /**
     * GET /api/tickets
     * Lista todos os tickets ordenados por data de abertura (decrescente)
     * 
     * @return ResponseEntity com lista de tickets
     */
    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> listarTodos(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        List<TicketResponseDTO> tickets = ticketService.listarTodos();
        return ResponseEntity.ok(tickets);
    }

    /**
     * POST /api/tickets
     * Abertura de ticket pela UI autenticada (mesma regra de negócio do webhook público).
     */
    @PostMapping
    public ResponseEntity<TicketResponseDTO> criar(
            @Valid @RequestBody TicketWebhookRequestDTO request,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketResponseDTO ticketCriado = ticketService.criarTicketPorWebhook(request);
        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_CRIAR_UI,
                AuditoriaService.ENTIDADE_TICKET,
                ticketCriado.getNumeroTicket(),
                "Ticket criado pela UI: " + ticketCriado.getNumeroTicket(),
                executor);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketCriado);
    }

    @GetMapping("/busca")
    public ResponseEntity<List<TicketResponseDTO>> buscar(
            TicketFiltroDTO filtro,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(ticketBuscaService.buscar(filtro));
    }

    @GetMapping("/relatorios/csv")
    public ResponseEntity<byte[]> exportarRelatorioCsv(
            TicketFiltroDTO filtro,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        byte[] csv = ticketRelatorioCsvService.gerarCsv(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-tickets.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    @GetMapping("/alerta-referencia")
    public ResponseEntity<TicketAlertaReferenciaDTO> obterReferenciaAlerta(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(ticketService.obterReferenciaAlerta());
    }

    @GetMapping("/novos-alerta")
    public ResponseEntity<List<TicketAlertaDTO>> listarNovosParaAlerta(
            @RequestParam(required = false) Integer aposId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime aposData,
            @RequestParam(defaultValue = "50") int limite,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        if (aposId == null && aposData == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ticketService.listarNovosParaAlerta(aposId, aposData, limite));
    }

    /**
     * GET /api/tickets/ativo — ticket ativo mais recente para o contexto (WhatsApp/API futuro).
     */
    @GetMapping("/ativo")
    public ResponseEntity<TicketResponseDTO> buscarTicketAtivo(
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Integer contatoSolicitanteId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        if (clienteId == null && contatoSolicitanteId == null
                && (telefone == null || telefone.isBlank())) {
            return ResponseEntity.badRequest().build();
        }
        return ticketAtivoService
                .buscarTicketAtivo(clienteId, contatoSolicitanteId, telefone)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * GET /api/tickets/{numeroTicket}
     * Busca um ticket específico pelo número
     * 
     * @param numeroTicket Número do ticket (ex: TK-000001)
     * @return ResponseEntity com dados do ticket
     * @throws RuntimeException se ticket não encontrado (convertido para 404)
     */
    @GetMapping("/{numeroTicket}")
    public ResponseEntity<TicketResponseDTO> buscarPorNumero(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketResponseDTO ticket = ticketService.buscarPorNumero(numeroTicket);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{numeroTicket}/pdf")
    public ResponseEntity<byte[]> gerarPdf(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        byte[] pdf = ticketPdfService.gerarPdf(numeroTicket);
        String fileName = ticketPdfService.montarNomeArquivo(numeroTicket);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * PUT /api/tickets/{numeroTicket}/status
     * Atualiza o status de um ticket
     * 
     * Regras:
     * - Se novo status for EM_ATENDIMENTO e dataPrimeiroAtendimento estiver nula,
     *   preenche com data/hora atual
     * - Status válidos: ABERTO, EM_ATENDIMENTO, AGUARDANDO_CLIENTE, RESOLVIDO, CANCELADO
     * 
     * @param numeroTicket Número do ticket
     * @param requestDTO DTO com novo status
     * @return ResponseEntity com ticket atualizado
     * @throws RuntimeException se ticket não encontrado (convertido para 404)
     * @throws IllegalArgumentException se status inválido (convertido para 400)
     */
    @PutMapping("/{numeroTicket}/status")
    public ResponseEntity<TicketResponseDTO> atualizarStatus(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody AtualizarStatusRequestDTO requestDTO) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketResponseDTO ticketAtualizado = ticketService.atualizarStatus(
                numeroTicket,
                requestDTO.getStatus(),
                requestDTO.getAnalistaId()
        );
        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_STATUS,
                AuditoriaService.ENTIDADE_TICKET,
                numeroTicket,
                "Status alterado para " + requestDTO.getStatus(),
                executor);
        return ResponseEntity.ok(ticketAtualizado);
    }

    /**
     * PUT /api/tickets/{numeroTicket}/encerrar
     * Encerra um ticket
     * 
     * Regras:
     * - Define status como RESOLVIDO
     * - Preenche dataEncerramento com data/hora atual
     * - Se dataPrimeiroAtendimento estiver nula, preenche também
     * 
     * @param numeroTicket Número do ticket
     * @return ResponseEntity com ticket atualizado
     * @throws RuntimeException se ticket não encontrado (convertido para 404)
     */
    @PutMapping("/{numeroTicket}/encerrar")
    public ResponseEntity<TicketResponseDTO> encerrarTicket(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody EncerrarTicketRequestDTO requestDTO) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketResponseDTO ticketAtualizado =
                ticketService.encerrarTicket(numeroTicket, requestDTO, executor.getId());
        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_ENCERRAR,
                AuditoriaService.ENTIDADE_TICKET,
                numeroTicket,
                "Ticket encerrado",
                executor);
        return ResponseEntity.ok(ticketAtualizado);
    }

    @GetMapping("/{numeroTicket}/etiquetas")
    public ResponseEntity<List<EtiquetaResponseDTO>> listarEtiquetasDoTicket(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(ticketEtiquetaService.listarPorNumeroTicket(numeroTicket));
    }

    @PutMapping("/{numeroTicket}/etiquetas")
    public ResponseEntity<List<EtiquetaResponseDTO>> substituirEtiquetasDoTicket(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody(required = false) TicketEtiquetasRequestDTO requestDTO) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        List<Long> ids = requestDTO != null && requestDTO.getEtiquetaIds() != null
                ? requestDTO.getEtiquetaIds()
                : List.of();
        List<EtiquetaResponseDTO> atualizadas = ticketEtiquetaService.substituirVinculosAtivos(numeroTicket, ids);
        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_ETIQUETAS,
                AuditoriaService.ENTIDADE_TICKET,
                numeroTicket,
                "Etiquetas do ticket atualizadas",
                executor);
        return ResponseEntity.ok(atualizadas);
    }

    @PutMapping("/{numeroTicket}/observacao-atendimento")
    public ResponseEntity<TicketResponseDTO> atualizarObservacaoAtendimento(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody TicketObservacaoAtendimentoRequestDTO requestDTO) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        String texto = requestDTO != null ? requestDTO.getObservacao() : null;
        TicketResponseDTO atualizado = ticketService.atualizarObservacaoAtendimento(numeroTicket, texto);
        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_OBSERVACAO_ATENDIMENTO,
                AuditoriaService.ENTIDADE_TICKET,
                numeroTicket,
                "Observacao de atendimento atualizada",
                executor);
        return ResponseEntity.ok(atualizado);
    }

    @PutMapping("/{numeroTicket}/reabrir")
    public ResponseEntity<TicketResponseDTO> reabrirTicket(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketResponseDTO ticketAtualizado = ticketService.reabrirTicket(numeroTicket);
        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_REABRIR,
                AuditoriaService.ENTIDADE_TICKET,
                numeroTicket,
                "Ticket reaberto",
                executor);
        return ResponseEntity.ok(ticketAtualizado);
    }

    @PutMapping("/{numeroTicket}/escalonar")
    public ResponseEntity<TicketResponseDTO> escalonarTicket(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestBody(required = false) TicketEscalonamentoRequestDTO requestDTO) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketResponseDTO ticketAtualizado = ticketService.escalonarTicket(numeroTicket, requestDTO);
        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_ESCALONAR,
                AuditoriaService.ENTIDADE_TICKET,
                numeroTicket,
                "Ticket escalonado",
                executor);
        return ResponseEntity.ok(ticketAtualizado);
    }

    @PutMapping("/{numeroTicket}/remover-escalonamento")
    public ResponseEntity<TicketResponseDTO> removerEscalonamento(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketResponseDTO ticketAtualizado = ticketService.removerEscalonamento(numeroTicket);
        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_REMOVER_ESCALONAMENTO,
                AuditoriaService.ENTIDADE_TICKET,
                numeroTicket,
                "Escalonamento removido",
                executor);
        return ResponseEntity.ok(ticketAtualizado);
    }

    /**
     * GET /api/tickets/status/{status}
     * Lista tickets por status
     * 
     * @param status Status do ticket (ABERTO, EM_ATENDIMENTO, AGUARDANDO_CLIENTE, RESOLVIDO, CANCELADO)
     * @return ResponseEntity com lista de tickets do status especificado
     * @throws IllegalArgumentException se status inválido (convertido para 400)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketResponseDTO>> listarPorStatus(
            @PathVariable String status,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        List<TicketResponseDTO> tickets = ticketService.listarPorStatus(status);
        return ResponseEntity.ok(tickets);
    }

}

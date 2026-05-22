package com.suporte.tickets.controller;

import com.suporte.tickets.dto.AvaliacaoPublicaResponseDTO;
import com.suporte.tickets.dto.TicketSatisfacaoRespostaRequestDTO;
import com.suporte.tickets.service.TicketSatisfacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoPublicaController {

    private final TicketSatisfacaoService ticketSatisfacaoService;

    @GetMapping("/{token}")
    public ResponseEntity<AvaliacaoPublicaResponseDTO> consultar(@PathVariable String token) {
        return ResponseEntity.ok(ticketSatisfacaoService.consultarAvaliacaoPublica(token));
    }

    @PostMapping("/{token}/responder")
    public ResponseEntity<AvaliacaoPublicaResponseDTO> responder(
            @PathVariable String token,
            @Valid @RequestBody TicketSatisfacaoRespostaRequestDTO request) {
        return ResponseEntity.ok(ticketSatisfacaoService.responderAvaliacaoPublica(token, request));
    }
}

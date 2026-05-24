package com.suporte.tickets.service;

import com.suporte.tickets.dto.AvaliacaoPublicaResponseDTO;
import com.suporte.tickets.dto.TicketSatisfacaoRequestDTO;
import com.suporte.tickets.dto.TicketSatisfacaoRespostaRequestDTO;
import com.suporte.tickets.dto.TicketSatisfacaoResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketSatisfacaoService {

    public static final int NOTA_MINIMA = 1;
    public static final int NOTA_MAXIMA = 5;
    public static final String MSG_LINK_AVALIACAO_INVALIDO =
            "Link de avaliacao invalido ou indisponivel.";

    private final TicketRepository ticketRepository;
    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;
    private final PesquisaSatisfacaoEnvioService pesquisaSatisfacaoEnvioService;

    @Transactional
    public TicketSatisfacaoResponseDTO consultarPorNumeroTicket(String numeroTicket) {
        Ticket ticket = buscarTicket(numeroTicket);
        return ticketSatisfacaoRepository.findByTicket_Id(ticket.getId())
                .map(s -> {
                    String tokenPreview = garantirTokenRespostaSePendente(s);
                    return converter(s, tokenPreview);
                })
                .orElse(null);
    }

    @Transactional
    public TicketSatisfacaoResponseDTO registrar(String numeroTicket, TicketSatisfacaoRequestDTO request) {
        Ticket ticket = buscarTicket(numeroTicket);
        validarTicketPodeReceberSatisfacao(ticket);
        if (ticketSatisfacaoRepository.existsByTicket_Id(ticket.getId())) {
            throw new IllegalArgumentException(
                    "Este ticket ja possui avaliacao de satisfacao registrada.");
        }
        validarNota(request.getNota());

        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        TicketSatisfacao satisfacao = new TicketSatisfacao();
        satisfacao.setTicket(ticket);
        satisfacao.setStatus(TicketSatisfacaoStatus.REGISTRADA_MANUALMENTE);
        satisfacao.setNota(request.getNota());
        satisfacao.setRespondidaEm(agora);
        if (request.getComentario() != null && !request.getComentario().isBlank()) {
            satisfacao.setComentario(request.getComentario().trim());
        }
        return converter(ticketSatisfacaoRepository.save(satisfacao), null);
    }

    /**
     * Registra decisão do analista ao encerrar como RESOLVIDO (uma avaliação por ticket).
     */
    /**
     * @return token opaco gerado (somente quando pesquisa PENDENTE com contato), para link na resposta do encerramento
     */
    @Transactional
    public String registrarDecisaoPosEncerramento(Ticket ticket, boolean enviarPesquisa, Long analistaId) {
        if (ticket == null || ticket.getId() == null) {
            return null;
        }
        if (ticket.getStatus() != TicketStatus.RESOLVIDO) {
            return null;
        }
        if (ticketSatisfacaoRepository.existsByTicket_Id(ticket.getId())) {
            return null;
        }

        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        TicketSatisfacao satisfacao = new TicketSatisfacao();
        satisfacao.setTicket(ticket);

        boolean temContato = ticket.getContato() != null;
        String tokenOpaco = null;
        if (enviarPesquisa && temContato) {
            satisfacao.setStatus(TicketSatisfacaoStatus.PENDENTE);
            // enviadaEm = momento da decisão/opt-in no encerramento (Sprint 197)
            satisfacao.setEnviadaEm(agora);
            satisfacao.setExpiraEm(TicketSatisfacaoExpiracaoUtil.calcularProximaSexta18h(agora, CalendarioSlaHelper.FUSO_SLA));
            satisfacao.setSolicitadaPorAnalistaId(analistaId);
            tokenOpaco = atribuirNovoTokenResposta(satisfacao, agora);
        } else {
            satisfacao.setStatus(TicketSatisfacaoStatus.NAO_ENVIADA);
        }
        TicketSatisfacao salva = ticketSatisfacaoRepository.save(satisfacao);
        if (tokenOpaco != null) {
            pesquisaSatisfacaoEnvioService.enviarPesquisa(salva, tokenOpaco, analistaId);
        }
        return tokenOpaco;
    }

    @Transactional(readOnly = true)
    public void preencherResumoNoTicketResponse(com.suporte.tickets.dto.TicketResponseDTO dto, Integer ticketId) {
        if (dto == null || ticketId == null) {
            return;
        }
        ticketSatisfacaoRepository.findByTicket_Id(ticketId).ifPresent(s -> {
            dto.setSatisfacaoStatus(resolverStatusExibicao(s));
            dto.setSatisfacaoNota(s.getNota());
            dto.setSatisfacaoComentario(s.getComentario());
            if (s.getEnvioStatus() != null) {
                dto.setSatisfacaoEnvioStatus(s.getEnvioStatus().name());
            }
            dto.setSatisfacaoEnviadaEm(s.getEnviadaEm());
            dto.setSatisfacaoRespondidaEm(s.getRespondidaEm());
            dto.setSatisfacaoExpiraEm(s.getExpiraEm());
        });
    }

    /**
     * Registra resposta do Contato em avaliação PENDENTE (endpoint interno autenticado).
     */
    @Transactional
    public TicketSatisfacaoResponseDTO responderAvaliacao(
            String numeroTicket, TicketSatisfacaoRespostaRequestDTO request) {
        Ticket ticket = buscarTicket(numeroTicket);
        TicketSatisfacao satisfacao = ticketSatisfacaoRepository.findByTicket_Id(ticket.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nao existe avaliacao de satisfacao para este ticket."));

        validarNota(request.getNota());
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        aplicarRespostaPendente(satisfacao, request, agora);
        return converter(ticketSatisfacaoRepository.save(satisfacao), null);
    }

    @Transactional(readOnly = true)
    public AvaliacaoPublicaResponseDTO consultarAvaliacaoPublica(String token) {
        TicketSatisfacao satisfacao = buscarPorToken(token);
        return montarRespostaPublica(satisfacao, LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA));
    }

    @Transactional
    public AvaliacaoPublicaResponseDTO responderAvaliacaoPublica(
            String token, TicketSatisfacaoRespostaRequestDTO request) {
        TicketSatisfacao satisfacao = buscarPorToken(token);
        validarNota(request.getNota());
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        aplicarRespostaPendente(satisfacao, request, agora);
        satisfacao.setTokenUsadoEm(agora);
        ticketSatisfacaoRepository.save(satisfacao);
        return montarRespostaPublica(satisfacao, agora);
    }

    /**
     * Marca avaliações PENDENTE vencidas como EXPIRADA (scheduler).
     */
    @Transactional
    public int marcarPendentesExpiradas() {
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        List<TicketSatisfacao> pendentes =
                ticketSatisfacaoRepository.findByStatusAndExpiraEmBefore(
                        TicketSatisfacaoStatus.PENDENTE, agora);
        int count = 0;
        for (TicketSatisfacao s : pendentes) {
            if (s.getStatus() != TicketSatisfacaoStatus.PENDENTE) {
                continue;
            }
            s.setStatus(TicketSatisfacaoStatus.EXPIRADA);
            count++;
        }
        if (count > 0) {
            ticketSatisfacaoRepository.saveAll(pendentes);
        }
        return count;
    }

    static boolean estaExpirada(TicketSatisfacao satisfacao, LocalDateTime agora) {
        return satisfacao.getExpiraEm() != null && !agora.isBefore(satisfacao.getExpiraEm());
    }

    static String mensagemStatusNaoPermiteResposta(TicketSatisfacaoStatus status) {
        if (status == null) {
            return "Esta avaliacao nao aceita resposta.";
        }
        return switch (status) {
            case NAO_ENVIADA -> "Pesquisa de satisfacao nao foi enviada para este ticket.";
            case RESPONDIDA -> "Esta pesquisa de satisfacao ja foi respondida.";
            case EXPIRADA -> "O prazo para responder a pesquisa de satisfacao expirou.";
            case REGISTRADA_MANUALMENTE -> "Esta avaliacao foi registrada manualmente e nao aceita resposta.";
            case PENDENTE -> "Esta avaliacao nao aceita resposta.";
        };
    }

    private TicketSatisfacao buscarPorToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(MSG_LINK_AVALIACAO_INVALIDO);
        }
        String hash = TicketSatisfacaoTokenUtil.hashToken(token);
        return ticketSatisfacaoRepository.findByTokenRespostaHash(hash)
                .orElseThrow(() -> new IllegalArgumentException(MSG_LINK_AVALIACAO_INVALIDO));
    }

    /**
     * Gera token se PENDENTE ainda não possui (retorna texto claro uma vez).
     */
    String garantirTokenRespostaSePendente(TicketSatisfacao satisfacao) {
        if (satisfacao.getStatus() != TicketSatisfacaoStatus.PENDENTE) {
            return null;
        }
        if (satisfacao.getTokenRespostaHash() != null) {
            return null;
        }
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        String plain = atribuirNovoTokenResposta(satisfacao, agora);
        ticketSatisfacaoRepository.save(satisfacao);
        return plain;
    }

    static String atribuirNovoTokenResposta(TicketSatisfacao satisfacao, LocalDateTime agora) {
        String plain = TicketSatisfacaoTokenUtil.gerarTokenOpaco();
        satisfacao.setTokenRespostaHash(TicketSatisfacaoTokenUtil.hashToken(plain));
        satisfacao.setTokenCriadoEm(agora);
        return plain;
    }

    private void aplicarRespostaPendente(
            TicketSatisfacao satisfacao,
            TicketSatisfacaoRespostaRequestDTO request,
            LocalDateTime agora) {
        if (satisfacao.getStatus() != TicketSatisfacaoStatus.PENDENTE) {
            throw new IllegalArgumentException(mensagemStatusNaoPermiteResposta(satisfacao.getStatus()));
        }
        if (estaExpirada(satisfacao, agora)) {
            satisfacao.setStatus(TicketSatisfacaoStatus.EXPIRADA);
            ticketSatisfacaoRepository.save(satisfacao);
            throw new IllegalArgumentException(
                    "O prazo para responder a pesquisa de satisfacao expirou.");
        }
        satisfacao.setNota(request.getNota());
        if (request.getComentario() != null && !request.getComentario().isBlank()) {
            satisfacao.setComentario(request.getComentario().trim());
        } else {
            satisfacao.setComentario(null);
        }
        satisfacao.setRespondidaEm(agora);
        satisfacao.setStatus(TicketSatisfacaoStatus.RESPONDIDA);
    }

    private AvaliacaoPublicaResponseDTO montarRespostaPublica(
            TicketSatisfacao satisfacao, LocalDateTime agora) {
        TicketSatisfacaoStatus status = satisfacao.getStatus();
        boolean expirada = status == TicketSatisfacaoStatus.EXPIRADA
                || (status == TicketSatisfacaoStatus.PENDENTE && estaExpirada(satisfacao, agora));
        boolean jaRespondida = status == TicketSatisfacaoStatus.RESPONDIDA;

        AvaliacaoPublicaResponseDTO dto = new AvaliacaoPublicaResponseDTO();
        dto.setStatus(status != null ? status.name() : null);
        dto.setExpirada(expirada);
        dto.setJaRespondida(jaRespondida);
        dto.setClienteNome(resolverNomeClientePublico(satisfacao.getTicket()));
        dto.setProtocoloMascarado(mascararProtocolo(satisfacao.getTicket()));
        dto.setMensagemOrientativa(resolverMensagemPublica(status, expirada, jaRespondida));
        return dto;
    }

    static String resolverNomeClientePublico(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        Cliente cliente = ticket.getCliente();
        if (cliente == null || cliente.getNome() == null || cliente.getNome().isBlank()) {
            return "Atendimento";
        }
        return cliente.getNome().trim();
    }

    static String mascararProtocolo(Ticket ticket) {
        if (ticket == null || ticket.getNumeroTicket() == null) {
            return "Chamado";
        }
        String numero = ticket.getNumeroTicket().trim();
        if (numero.length() <= 4) {
            return "Chamado ••••";
        }
        return "Chamado ••••" + numero.substring(numero.length() - 4);
    }

    static String resolverMensagemPublica(
            TicketSatisfacaoStatus status, boolean expirada, boolean jaRespondida) {
        if (jaRespondida) {
            return "Obrigado! Sua avaliacao ja foi registrada.";
        }
        if (expirada || status == TicketSatisfacaoStatus.EXPIRADA) {
            return "O prazo para responder esta pesquisa encerrou.";
        }
        if (status == TicketSatisfacaoStatus.PENDENTE) {
            return "Como foi seu atendimento? Escolha uma nota de 1 a 5.";
        }
        return MSG_LINK_AVALIACAO_INVALIDO;
    }

    private Ticket buscarTicket(String numeroTicket) {
        return ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new IllegalArgumentException("Ticket nao encontrado: " + numeroTicket));
    }

    /**
     * Impede pesquisa pendente após classificação indevida (Sprint 274).
     */
    @Transactional
    public void tratarAvaliacaoAoClassificarIndevido(Ticket ticket) {
        if (ticket == null || ticket.getId() == null) {
            return;
        }
        ticketSatisfacaoRepository.findByTicket_Id(ticket.getId()).ifPresent(satisfacao -> {
            if (satisfacao.getStatus() == TicketSatisfacaoStatus.PENDENTE) {
                satisfacao.setStatus(TicketSatisfacaoStatus.NAO_ENVIADA);
                satisfacao.setExpiraEm(null);
                ticketSatisfacaoRepository.save(satisfacao);
            }
        });
    }

    static void validarTicketPodeReceberSatisfacao(Ticket ticket) {
        TicketStatus status = ticket.getStatus();
        if (status == TicketStatus.INDEVIDO) {
            throw new IllegalArgumentException("Ticket indevido nao pode receber avaliacao de satisfacao.");
        }
        if (status != TicketStatus.RESOLVIDO && status != TicketStatus.CANCELADO) {
            throw new IllegalArgumentException(
                    "Satisfacao so pode ser registrada em ticket encerrado (RESOLVIDO ou CANCELADO).");
        }
    }

    static void validarNota(Integer nota) {
        if (nota == null || nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new IllegalArgumentException("Nota deve ser um valor inteiro entre 1 e 5.");
        }
    }

    private TicketSatisfacaoResponseDTO converter(TicketSatisfacao satisfacao, String tokenRespostaPreview) {
        TicketSatisfacaoResponseDTO dto = new TicketSatisfacaoResponseDTO();
        dto.setId(satisfacao.getId());
        dto.setNumeroTicket(satisfacao.getTicket().getNumeroTicket());
        dto.setStatus(resolverStatusExibicao(satisfacao));
        dto.setNota(satisfacao.getNota());
        dto.setComentario(satisfacao.getComentario());
        dto.setCriadoEm(satisfacao.getCriadoEm());
        dto.setEnviadaEm(satisfacao.getEnviadaEm());
        dto.setRespondidaEm(satisfacao.getRespondidaEm());
        dto.setExpiraEm(satisfacao.getExpiraEm());
        dto.setTokenRespostaPreview(tokenRespostaPreview);
        if (satisfacao.getEnvioStatus() != null) {
            dto.setEnvioStatus(satisfacao.getEnvioStatus().name());
        }
        if (tokenRespostaPreview != null && !tokenRespostaPreview.isBlank()) {
            dto.setLinkAvaliacaoPublico(pesquisaSatisfacaoEnvioService.montarLinkAvaliacao(tokenRespostaPreview));
        }
        return dto;
    }

    static String resolverStatusExibicao(TicketSatisfacao satisfacao) {
        if (satisfacao.getStatus() != null) {
            return satisfacao.getStatus().name();
        }
        if (satisfacao.getNota() != null) {
            return TicketSatisfacaoStatus.REGISTRADA_MANUALMENTE.name();
        }
        return null;
    }
}

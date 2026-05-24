package com.suporte.tickets.service;

import com.suporte.tickets.dto.ClientePortalDashboardDTO;
import com.suporte.tickets.dto.ClientePortalUsuarioRequestDTO;
import com.suporte.tickets.dto.ClientePortalUsuarioResponseDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.ClientePortalUsuario;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.ClientePortalUsuarioRepository;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientePortalService {

    private final ClientePortalUsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final TicketRepository ticketRepository;
    private final TicketSatisfacaoRepository satisfacaoRepository;
    private final TicketService ticketService;
    private final AnalistaSenhaService senhaService;

    @Transactional(readOnly = true)
    public ClientePortalDashboardDTO getDashboard(Integer clienteId) {
        List<Ticket> todos = ticketRepository.findByDataAberturaPeriodo(null, null)
                .stream().filter(t -> t.getCliente() != null && clienteId.equals(t.getCliente().getId())).toList();

        long abertos = todos.stream().filter(t -> t.getStatus() == TicketStatus.ABERTO).count();
        long emAtend = todos.stream().filter(t -> t.getStatus() == TicketStatus.EM_ATENDIMENTO
                || t.getStatus() == TicketStatus.AGUARDANDO_CLIENTE).count();
        long resolvidos = todos.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVIDO).count();
        long cancelados = todos.stream().filter(t -> t.getStatus() == TicketStatus.CANCELADO).count();

        Set<Integer> ids = todos.stream().map(Ticket::getId).collect(Collectors.toSet());
        List<TicketSatisfacao> avaliacoes = satisfacaoRepository.findByTicket_IdIn(ids)
                .stream().filter(s -> s.getNota() != null).toList();
        double media = avaliacoes.isEmpty() ? 0.0
                : avaliacoes.stream().mapToInt(TicketSatisfacao::getNota).average().orElse(0.0);

        return new ClientePortalDashboardDTO(
                todos.size(), abertos, emAtend, resolvidos, cancelados,
                avaliacoes.isEmpty() ? null : Math.round(media * 10.0) / 10.0,
                avaliacoes.size());
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTickets(Integer clienteId) {
        return ticketRepository.findByDataAberturaPeriodo(null, null)
                .stream()
                .filter(t -> t.getCliente() != null && clienteId.equals(t.getCliente().getId()))
                .sorted((a, b) -> b.getDataAbertura().compareTo(a.getDataAbertura()))
                .map(ticketService::converterParaResponseSeguro)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientePortalUsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findAllByOrderByNomeAsc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ClientePortalUsuarioResponseDTO> listarPorCliente(Integer clienteId) {
        return usuarioRepository.findByCliente_IdOrderByNomeAsc(clienteId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ClientePortalUsuarioResponseDTO criar(ClientePortalUsuarioRequestDTO req, Long criadoPorAnalistaId) {
        if (req.getSenha() == null || req.getSenha().isBlank()) {
            throw new IllegalArgumentException("Senha obrigatoria para novo usuario do portal.");
        }
        if (usuarioRepository.findByEmailIgnoreCase(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail ja cadastrado no portal: " + req.getEmail());
        }
        Cliente cliente = clienteRepository.findById(req.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado: " + req.getClienteId()));

        ClientePortalUsuario u = new ClientePortalUsuario();
        u.setNome(req.getNome().trim());
        u.setEmail(req.getEmail().trim().toLowerCase());
        u.setSenha(senhaService.hashSenha(req.getSenha()));
        u.setCliente(cliente);
        u.setAtivo(req.getAtivo() != null ? req.getAtivo() : true);
        u.setCriadoPorAnalistaId(criadoPorAnalistaId);
        return toResponse(usuarioRepository.save(u));
    }

    @Transactional
    public ClientePortalUsuarioResponseDTO atualizar(Long id, ClientePortalUsuarioRequestDTO req) {
        ClientePortalUsuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario do portal nao encontrado: " + id));
        if (req.getNome() != null && !req.getNome().isBlank()) {
            u.setNome(req.getNome().trim());
        }
        if (req.getSenha() != null && !req.getSenha().isBlank()) {
            u.setSenha(senhaService.hashSenha(req.getSenha()));
        }
        if (req.getAtivo() != null) {
            u.setAtivo(req.getAtivo());
        }
        if (req.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(req.getClienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado."));
            u.setCliente(cliente);
        }
        return toResponse(usuarioRepository.save(u));
    }

    private ClientePortalUsuarioResponseDTO toResponse(ClientePortalUsuario u) {
        Cliente c = u.getCliente();
        return new ClientePortalUsuarioResponseDTO(
                u.getId(), u.getNome(), u.getEmail(),
                c != null ? c.getId() : null,
                c != null ? c.getNome() : null,
                u.getAtivo(), u.getCriadoEm());
    }
}

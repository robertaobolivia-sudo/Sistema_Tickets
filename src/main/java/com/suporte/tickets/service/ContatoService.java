package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoGestaoResponseDTO;
import com.suporte.tickets.dto.ContatoRequestDTO;
import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.dto.ContatoTicketHistoricoItemDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.domain.EtiquetaOperacionalCatalog;
import com.suporte.tickets.entity.ContatoEtiqueta;
import com.suporte.tickets.entity.Etiqueta;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.ContatoEtiquetaRepository;
import com.suporte.tickets.entity.ContatoTelefone;
import com.suporte.tickets.repository.ContatoRepository;
import com.suporte.tickets.repository.ContatoTelefoneRepository;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContatoService {

    private final ContatoRepository contatoRepository;
    private final ContatoTelefoneRepository contatoTelefoneRepository;
    private final ClienteRepository clienteRepository;
    private final ContatoEtiquetaRepository contatoEtiquetaRepository;
    private final TicketRepository ticketRepository;
    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;

    @Transactional
    public ContatoResponseDTO criar(ContatoRequestDTO dto) {
        if (dto.getClienteId() == null) {
            throw new IllegalArgumentException("Cliente e obrigatorio.");
        }
        Cliente cliente = buscarCliente(dto.getClienteId());
        String whatsappNorm = exigirWhatsappNormalizado(dto.getWhatsapp());
        if (existeTelefoneNoCliente(cliente.getId(), whatsappNorm)) {
            throw new IllegalArgumentException(
                    "Ja existe contato ou telefone vinculado com este numero para o cliente informado.");
        }
        Contato contato = new Contato();
        contato.setCliente(cliente);
        aplicarWhatsappNaCriacao(contato, dto.getWhatsapp(), whatsappNorm);
        preencherCamposEditaveis(contato, dto, true);
        if (contato.getNome() == null || contato.getNome().isBlank()) {
            contato.setNome(nomeFallbackWhatsapp(contato.getWhatsapp()));
        }
        return ContatoResponseDTO.fromEntity(contatoRepository.save(contato));
    }

    @Transactional
    public ContatoResponseDTO atualizar(Integer id, ContatoRequestDTO dto) {
        Contato contato = buscarEntidade(id);
        if (dto.getWhatsapp() != null && !dto.getWhatsapp().isBlank()) {
            String novoNorm = TicketAtivoService.normalizarTelefone(dto.getWhatsapp());
            if (novoNorm != null && !novoNorm.equals(contato.getWhatsappNormalizado())) {
                throw new IllegalArgumentException("WhatsApp do contato nao pode ser alterado.");
            }
        }
        preencherCamposEditaveis(contato, dto, false);
        return ContatoResponseDTO.fromEntity(contatoRepository.save(contato));
    }

    @Transactional(readOnly = true)
    public ContatoResponseDTO buscarPorId(Integer id) {
        return ContatoResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ContatoResponseDTO> listarPorCliente(Integer clienteId) {
        buscarCliente(clienteId);
        return contatoRepository.findByCliente_IdOrderByNomeAsc(clienteId)
                .stream()
                .map(ContatoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean clientePossuiContatoWhatsappAtivo(Integer clienteId) {
        if (clienteId == null) {
            return false;
        }
        return contatoRepository.findByCliente_IdOrderByNomeAsc(clienteId).stream()
                .anyMatch(c -> c.getAtivo() == null || Boolean.TRUE.equals(c.getAtivo()));
    }

    @Transactional(readOnly = true)
    public List<ContatoTicketHistoricoItemDTO> listarHistoricoTickets(Integer contatoId) {
        buscarEntidade(contatoId);
        return ticketRepository.findHistoricoByContatoIdOrderByDataAberturaDesc(contatoId).stream()
                .map(this::mapearHistoricoTicket)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContatoGestaoResponseDTO> listarParaGestao(
            Integer clienteId,
            String busca,
            Long etiquetaId,
            String cidade,
            String uf,
            Boolean comTicketsAbertos,
            Boolean comAvaliacaoRuim,
            Boolean semEtiqueta) {
        if (clienteId != null) {
            buscarCliente(clienteId);
        }
        List<Contato> contatos = clienteId != null
                ? contatoRepository.findByCliente_IdOrderByNomeAsc(clienteId)
                : contatoRepository.findAllByOrderByNomeAsc();
        String termo = busca == null ? "" : busca.trim().toLowerCase();
        String cidadeFiltro = trimOrNull(cidade);
        String ufFiltro = normalizarUfFiltro(uf);
        boolean filtroTicketsAbertos = Boolean.TRUE.equals(comTicketsAbertos);
        boolean filtroAvaliacaoRuim = Boolean.TRUE.equals(comAvaliacaoRuim);
        boolean filtroSemEtiqueta = Boolean.TRUE.equals(semEtiqueta);
        List<ContatoGestaoResponseDTO> resultado = new ArrayList<>();
        for (Contato contato : contatos) {
            ContatoGestaoResponseDTO item = montarGestaoDto(contato);
            if (!termo.isEmpty() && !correspondeBuscaPrincipal(item, termo)) {
                continue;
            }
            if (cidadeFiltro != null && !correspondeCidade(item.getCidade(), cidadeFiltro)) {
                continue;
            }
            if (ufFiltro != null && !ufFiltro.equalsIgnoreCase(normalizarUfFiltro(item.getUf()))) {
                continue;
            }
            if (etiquetaId != null
                    && !contatoEtiquetaRepository.existsByContato_IdAndEtiqueta_Id(contato.getId(), etiquetaId)) {
                continue;
            }
            if (filtroSemEtiqueta && contatoEtiquetaRepository.countByContato_Id(contato.getId()) > 0) {
                continue;
            }
            if (filtroTicketsAbertos && (item.getChamadosAtivos() == null || item.getChamadosAtivos() <= 0)) {
                continue;
            }
            if (filtroAvaliacaoRuim
                    && !ticketSatisfacaoRepository.existsAvaliacaoRuimPorContatoId(contato.getId())) {
                continue;
            }
            resultado.add(item);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public ContatoResponseDTO buscarPorClienteEWhatsapp(Integer clienteId, String whatsapp) {
        String norm = TicketAtivoService.normalizarTelefone(whatsapp);
        if (norm == null) {
            throw new IllegalArgumentException("WhatsApp invalido.");
        }
        return buscarEntidadePorClienteETelefone(clienteId, norm)
                .map(ContatoResponseDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Contato nao encontrado para o cliente e WhatsApp informados."));
    }

    /**
     * Cria contato se nao existir para Cliente + WhatsApp (uso futuro integracao).
     */
    @Transactional
    public ContatoResponseDTO criarSeNaoExistir(Integer clienteId, String whatsapp, String nomeOpcional) {
        String norm = exigirWhatsappNormalizado(whatsapp);
        return buscarEntidadePorClienteETelefone(clienteId, norm)
                .map(ContatoResponseDTO::fromEntity)
                .orElseGet(() -> {
                    ContatoRequestDTO req = new ContatoRequestDTO();
                    req.setClienteId(clienteId);
                    req.setWhatsapp(whatsapp);
                    req.setNome(nomeOpcional != null && !nomeOpcional.isBlank()
                            ? nomeOpcional.trim()
                            : nomeFallbackWhatsapp(whatsapp));
                    req.setCriadoAutomaticamente(true);
                    req.setAtivo(true);
                    return criar(req);
                });
    }

    @Transactional
    public ContatoResponseDTO ativar(Integer id) {
        Contato contato = buscarEntidade(id);
        contato.setAtivo(true);
        return ContatoResponseDTO.fromEntity(contatoRepository.save(contato));
    }

    @Transactional
    public ContatoResponseDTO inativar(Integer id) {
        Contato contato = buscarEntidade(id);
        contato.setAtivo(false);
        return ContatoResponseDTO.fromEntity(contatoRepository.save(contato));
    }

    @Transactional(readOnly = true)
    public Contato buscarEntidade(Integer id) {
        return contatoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contato nao encontrado: " + id));
    }

    /**
     * Resolve Contato pelo WhatsApp principal ou por telefone adicional (Sprint 288).
     */
    @Transactional(readOnly = true)
    public Optional<Contato> buscarEntidadePorClienteETelefone(Integer clienteId, String telefoneNormalizado) {
        if (clienteId == null || telefoneNormalizado == null || telefoneNormalizado.isBlank()) {
            return Optional.empty();
        }
        Optional<Contato> principal = contatoRepository.findByCliente_IdAndWhatsappNormalizado(
                clienteId, telefoneNormalizado);
        if (principal.isPresent()) {
            return principal;
        }
        return contatoTelefoneRepository
                .findByCliente_IdAndTelefoneNormalizado(clienteId, telefoneNormalizado)
                .map(ContatoTelefone::getContato);
    }

    @Transactional(readOnly = true)
    public boolean existeTelefoneNoCliente(Integer clienteId, String telefoneNormalizado) {
        if (clienteId == null || telefoneNormalizado == null) {
            return false;
        }
        return contatoRepository.existsByCliente_IdAndWhatsappNormalizado(clienteId, telefoneNormalizado)
                || contatoTelefoneRepository.existsByCliente_IdAndTelefoneNormalizado(
                        clienteId, telefoneNormalizado);
    }

    private Cliente buscarCliente(Integer clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado: " + clienteId));
    }

    private static String exigirWhatsappNormalizado(String whatsapp) {
        String norm = TicketAtivoService.normalizarTelefone(whatsapp);
        if (norm == null) {
            throw new IllegalArgumentException("WhatsApp e obrigatorio e deve conter digitos validos.");
        }
        return norm;
    }

    private static void aplicarWhatsappNaCriacao(Contato contato, String whatsappInformado, String norm) {
        contato.setWhatsapp(whatsappInformado == null ? norm : whatsappInformado.trim());
        contato.setWhatsappNormalizado(norm);
    }

    private static void preencherCamposEditaveis(Contato contato, ContatoRequestDTO dto, boolean criacao) {
        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            contato.setNome(dto.getNome().trim());
        }
        contato.setEmail(trimOrNull(dto.getEmail()));
        contato.setEmpresaLocal(trimOrNull(dto.getEmpresaLocal()));
        contato.setCidade(trimOrNull(dto.getCidade()));
        contato.setUf(trimOrNull(dto.getUf()));
        contato.setObservacoes(trimOrNull(dto.getObservacoes()));
        if (dto.getAtivo() != null) {
            contato.setAtivo(dto.getAtivo());
        } else if (criacao) {
            contato.setAtivo(true);
        }
        if (criacao && dto.getCriadoAutomaticamente() != null) {
            contato.setCriadoAutomaticamente(dto.getCriadoAutomaticamente());
        }
        if (criacao && Boolean.TRUE.equals(dto.getCriadoAutomaticamente())) {
            LocalDateTime agora = LocalDateTime.now();
            contato.setPrimeiraInteracaoEm(agora);
            contato.setUltimaInteracaoEm(agora);
        }
    }

    private static String trimOrNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static String nomeFallbackWhatsapp(String whatsapp) {
        if (whatsapp == null || whatsapp.isBlank()) {
            return "Contato WhatsApp";
        }
        return "WhatsApp " + whatsapp.trim();
    }

    private ContatoTicketHistoricoItemDTO mapearHistoricoTicket(Ticket ticket) {
        ContatoTicketHistoricoItemDTO dto = new ContatoTicketHistoricoItemDTO();
        dto.setProtocolo(ticket.getNumeroTicket());
        dto.setDataAbertura(ticket.getDataAbertura());
        if (ticket.getGrupoCategoria() != null) {
            dto.setCategoria(ticket.getGrupoCategoria().getNome());
        }
        if (ticket.getSubgrupoCategoria() != null) {
            dto.setSubcategoria(ticket.getSubgrupoCategoria().getNome());
        }
        if (ticket.getMotivo() != null) {
            dto.setMotivo(ticket.getMotivo().getNome());
        }
        dto.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : null);
        dto.setDataEncerramento(ticket.getDataEncerramento());
        ticketSatisfacaoRepository.findByTicket_Id(ticket.getId())
                .ifPresent(s -> preencherAvaliacaoHistorico(dto, s));
        if (ticket.getAtendimentoTelefone() != null && !ticket.getAtendimentoTelefone().isBlank()) {
            dto.setAtendimentoTelefone(ticket.getAtendimentoTelefone().trim());
        }
        if (ticket.getAtendimentoTelefoneTipo() != null && !ticket.getAtendimentoTelefoneTipo().isBlank()) {
            dto.setAtendimentoTelefoneTipo(ticket.getAtendimentoTelefoneTipo().trim());
        }
        return dto;
    }

    private void preencherAvaliacaoHistorico(ContatoTicketHistoricoItemDTO dto, TicketSatisfacao satisfacao) {
        if (satisfacao.getStatus() != null) {
            dto.setSatisfacaoStatus(satisfacao.getStatus().name());
        } else if (satisfacao.getEnvioStatus() != null) {
            dto.setSatisfacaoStatus(satisfacao.getEnvioStatus().name());
        }
        dto.setSatisfacaoNota(satisfacao.getNota());
    }

    private ContatoGestaoResponseDTO montarGestaoDto(Contato contato) {
        ContatoGestaoResponseDTO dto = new ContatoGestaoResponseDTO();
        dto.setId(contato.getId());
        Cliente cliente = contato.getCliente();
        if (cliente != null) {
            dto.setClienteId(cliente.getId());
            dto.setClienteRazaoSocial(rotuloCliente(cliente));
        }
        dto.setNome(contato.getNome());
        dto.setWhatsapp(contato.getWhatsapp());
        dto.setEmail(contato.getEmail());
        dto.setEmpresaLocal(contato.getEmpresaLocal());
        dto.setCidade(contato.getCidade());
        dto.setUf(contato.getUf());
        dto.setAtivo(contato.getAtivo());
        List<String> nomesEtiquetas = listarNomesEtiquetasAtivas(contato);
        dto.setEtiquetasResumo(resumirNomesEtiquetas(nomesEtiquetas));
        dto.setTemEtiquetaOperacional(EtiquetaOperacionalCatalog.temAlgumaOperacional(nomesEtiquetas));
        int total = (int) ticketRepository.countByContato_Id(contato.getId());
        int ativos = (int) ticketRepository.countByContato_IdAndStatusIn(
                contato.getId(), List.copyOf(TicketAtivoService.STATUS_ATIVOS));
        dto.setTotalChamados(total);
        dto.setChamadosAtivos(ativos);
        return dto;
    }

    private List<String> listarNomesEtiquetasAtivas(Contato contato) {
        List<ContatoEtiqueta> vinculos = contatoEtiquetaRepository.findByContatoOrderByEtiqueta_NomeAsc(contato);
        if (vinculos.isEmpty()) {
            return List.of();
        }
        return vinculos.stream()
                .map(ContatoEtiqueta::getEtiqueta)
                .filter(e -> e != null && (e.getAtivo() == null || Boolean.TRUE.equals(e.getAtivo())))
                .map(Etiqueta::getNome)
                .filter(n -> n != null && !n.isBlank())
                .limit(8)
                .collect(Collectors.toList());
    }

    private static String resumirNomesEtiquetas(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) {
            return null;
        }
        String joined = nomes.stream().limit(8).collect(Collectors.joining(", "));
        return joined.isBlank() ? null : joined;
    }

    private static String rotuloCliente(Cliente cliente) {
        if (cliente.getRazaoSocial() != null && !cliente.getRazaoSocial().isBlank()) {
            return cliente.getRazaoSocial().trim();
        }
        if (cliente.getEmpresa() != null && !cliente.getEmpresa().isBlank()) {
            return cliente.getEmpresa().trim();
        }
        return cliente.getNome();
    }

    /** Busca principal: nome ou WhatsApp (Sprint 265). */
    private static boolean correspondeBuscaPrincipal(ContatoGestaoResponseDTO dto, String termo) {
        String nome = safe(dto.getNome()).toLowerCase();
        String wa = safe(dto.getWhatsapp()).toLowerCase();
        if (nome.contains(termo) || wa.contains(termo)) {
            return true;
        }
        String termDigits = termo.replaceAll("\\D", "");
        if (!termDigits.isEmpty()) {
            String waDigits = wa.replaceAll("\\D", "");
            return waDigits.contains(termDigits);
        }
        return false;
    }

    private static boolean correspondeCidade(String cidadeContato, String cidadeFiltro) {
        if (cidadeFiltro == null || cidadeFiltro.isBlank()) {
            return true;
        }
        return safe(cidadeContato).toLowerCase().contains(cidadeFiltro.trim().toLowerCase());
    }

    private static String normalizarUfFiltro(String uf) {
        if (uf == null || uf.isBlank()) {
            return null;
        }
        return uf.trim().toUpperCase();
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }
}

package com.suporte.tickets.service;

import com.suporte.tickets.dto.AnalistaFilaDTO;
import com.suporte.tickets.dto.AnalistaLoginRequestDTO;
import com.suporte.tickets.dto.AnalistaResponseDTO;
import com.suporte.tickets.dto.AtualizarAnalistaRequestDTO;
import com.suporte.tickets.dto.CriarAnalistaRequestDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.PerfilAcesso;
import com.suporte.tickets.entity.StatusOperador;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.exception.CredenciaisInvalidasException;
import com.suporte.tickets.config.UploadStorageProperties;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalistaService {

    public static final String ANALISTA_PADRAO_EMAIL = "analista.teste@suporte.local";
    static final int SESSAO_VALIDADE_HORAS = 12;

    private final AnalistaRepository analistaRepository;
    private final TicketRepository ticketRepository;
    private final UploadStorageProperties uploadStorageProperties;
    private final AnalistaSenhaService analistaSenhaService;
    private final AnalistaSenhaPoliticaService analistaSenhaPoliticaService;

    @Transactional(readOnly = true)
    public List<AnalistaResponseDTO> listarTodos() {
        return analistaRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(this::converterAnalista)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalistaResponseDTO> listarOnline() {
        return analistaRepository.findByOnlineTrueAndAtivoTrueOrderByNomeAsc()
                .stream()
                .map(this::converterAnalista)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDTO> listarTicketsPorAnalista(Long analistaId) {
        analistaRepository.findById(analistaId)
                .orElseThrow(() -> new RuntimeException("Analista nao encontrado: " + analistaId));

        return ticketRepository.findByAnalistaResponsavelIdAndStatusOrderByDataAberturaAsc(
                        analistaId,
                        TicketStatus.EM_ATENDIMENTO
                )
                .stream()
                .map(this::converterTicketResumo)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalistaFilaDTO> listarFilasAnalistasOnline() {
        return analistaRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .filter(analista -> !ANALISTA_PADRAO_EMAIL.equalsIgnoreCase(analista.getEmail()))
                .map(analista -> {
                    List<TicketResponseDTO> tickets = listarTicketsPorAnalista(analista.getId());
                    AnalistaFilaDTO fila = new AnalistaFilaDTO();
                    fila.setId(analista.getId());
                    fila.setNome(analista.getNome());
                    fila.setNomeCompleto(analista.getNomeCompleto());
                    fila.setEmail(analista.getEmail());
                    fila.setNivel(analista.getNivel());
                    fila.setPerfilAcesso(resolverPerfilAcesso(analista).name());
                    fila.setFotoUrl(analista.getFotoUrl());
                    fila.setStatusOperador(obterStatusOperador(analista).name());
                    fila.setOnline(analista.getOnline());
                    fila.setQuantidadeTickets(tickets.size());
                    fila.setTickets(tickets);
                    return fila;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Analista buscarAnalistaPadrao() {
        return analistaRepository.findByEmailIgnoreCase(com.suporte.tickets.config.AnalistasOficiaisConstants.JOAO_EMAIL)
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .or(() -> analistaRepository.findByEmailIgnoreCase(ANALISTA_PADRAO_EMAIL)
                        .filter(a -> Boolean.TRUE.equals(a.getAtivo())))
                .or(() -> analistaRepository.findByAtivoTrueOrderByNomeAsc().stream().findFirst())
                .orElseThrow(() -> new RuntimeException("Analista padrao nao encontrado"));
    }

    @Transactional(readOnly = true)
    public Analista buscarPorId(Long id) {
        return analistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Analista nao encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public AnalistaResponseDTO buscarResponsePorId(Long id) {
        return converterAnalista(buscarPorId(id));
    }

    @Transactional
    public AnalistaResponseDTO autenticar(AnalistaLoginRequestDTO request) {
        Analista analista = analistaRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(CredenciaisInvalidasException::new);

        String senhaArmazenada = analista.getSenha();
        if (!Boolean.TRUE.equals(analista.getAtivo()) || senhaArmazenada == null
                || !analistaSenhaService.senhaConfere(request.getSenha(), senhaArmazenada)) {
            throw new CredenciaisInvalidasException();
        }
        if (analistaSenhaService.deveMigrarParaHash(senhaArmazenada)) {
            analista.setSenha(analistaSenhaService.hashSenha(request.getSenha()));
        }

        String authToken = gerarTokenSessao();
        analista.setAuthToken(authToken);
        analista.setAuthTokenExpiraEm(LocalDateTime.now().plusHours(SESSAO_VALIDADE_HORAS));

        if (analista.getStatusOperador() != StatusOperador.AUSENTE) {
            analista.setStatusOperador(StatusOperador.ONLINE);
            analista.setOnline(true);
        }
        analista = analistaRepository.save(analista);

        AnalistaResponseDTO response = converterAnalista(analista);
        response.setAuthToken(authToken);
        return response;
    }

    static String gerarTokenSessao() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    @Transactional
    public void encerrarSessao(Long analistaId) {
        Analista analista = buscarPorId(analistaId);
        analista.setAuthToken(null);
        analista.setAuthTokenExpiraEm(null);
        analistaRepository.save(analista);
    }

    @Transactional
    public AnalistaResponseDTO atualizarStatusOperador(Long id, String statusOperador) {
        StatusOperador novoStatus = converterStatusOperador(statusOperador);
        Analista analista = buscarPorId(id);
        analista.setStatusOperador(novoStatus);
        analista.setOnline(
                novoStatus == StatusOperador.ONLINE
                        || novoStatus == StatusOperador.OCUPADO
                        || novoStatus == StatusOperador.AUSENTE);
        return converterAnalista(analistaRepository.save(analista));
    }

    @Transactional
    public AnalistaResponseDTO atualizarPerfilAcesso(Long id, String perfilAcesso) {
        PerfilAcesso novoPerfil = converterPerfilAcesso(perfilAcesso);
        Analista analista = buscarPorId(id);
        analista.setPerfilAcesso(novoPerfil);
        return converterAnalista(analistaRepository.save(analista));
    }

    @Transactional
    public AnalistaResponseDTO criar(CriarAnalistaRequestDTO dto) {
        String email = dto.getEmail().trim();
        validarEmailUnico(email, null);

        analistaSenhaPoliticaService.validarSenhaInformada(dto.getSenha());

        Analista analista = new Analista();
        analista.setNome(dto.getNome().trim());
        analista.setEmail(email);
        analista.setSenha(analistaSenhaService.hashSenha(dto.getSenha()));
        analista.setNivel(nivelOuPadrao(dto.getNivel()));
        analista.setPerfilAcesso(converterPerfilAcesso(dto.getPerfilAcesso()));
        analista.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        analista.setOnline(false);
        analista.setStatusOperador(StatusOperador.OFFLINE);
        return converterAnalista(analistaRepository.save(analista));
    }

    @Transactional
    public AnalistaResponseDTO atualizarCadastro(Long id, AtualizarAnalistaRequestDTO dto) {
        Analista analista = buscarPorId(id);

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            analista.setNome(dto.getNome().trim());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String email = dto.getEmail().trim();
            validarEmailUnico(email, id);
            analista.setEmail(email);
        }
        if (dto.getNivel() != null && !dto.getNivel().isBlank()) {
            analista.setNivel(nivelOuPadrao(dto.getNivel()));
        }
        if (dto.getPerfilAcesso() != null && !dto.getPerfilAcesso().isBlank()) {
            analista.setPerfilAcesso(converterPerfilAcesso(dto.getPerfilAcesso()));
        }
        if (dto.getAtivo() != null) {
            aplicarStatusAtivo(analista, dto.getAtivo());
        }
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            analistaSenhaPoliticaService.validarSenhaInformada(dto.getSenha());
            analista.setSenha(analistaSenhaService.hashSenha(dto.getSenha()));
        }

        return converterAnalista(analistaRepository.save(analista));
    }

    @Transactional
    public AnalistaResponseDTO salvarFotoPerfil(Long id, MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new IllegalArgumentException("Foto de perfil é obrigatória");
        }

        String contentType = foto.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("image/png")
                && !contentType.equalsIgnoreCase("image/jpeg")
                && !contentType.equalsIgnoreCase("image/jpg"))) {
            throw new IllegalArgumentException("A foto deve ser PNG, JPG ou JPEG");
        }

        Analista analista = buscarPorId(id);
        removerArquivoFotoSeExistir(analista.getFotoUrl());

        String originalName = foto.getOriginalFilename() != null ? foto.getOriginalFilename() : "foto.jpg";
        String extensao = obterExtensao(originalName);
        String nomeArquivo = "analista-" + id + "-" + UUID.randomUUID() + extensao;

        try {
            Path destinoDir = uploadStorageProperties.getAnalistasDir();
            Files.createDirectories(destinoDir);
            Path destino = destinoDir.resolve(nomeArquivo);
            Files.copy(foto.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            analista.setFotoUrl(uploadStorageProperties.toPublicUrl(nomeArquivo));
            return converterAnalista(analistaRepository.save(analista));
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar foto de perfil", e);
        }
    }

    @Transactional
    public AnalistaResponseDTO removerFotoPerfil(Long id) {
        Analista analista = buscarPorId(id);
        String fotoUrlAtual = analista.getFotoUrl();
        analista.setFotoUrl(null);

        removerArquivoFotoSeExistir(fotoUrlAtual);

        return converterAnalista(analistaRepository.save(analista));
    }

    private void removerArquivoFotoSeExistir(String fotoUrl) {
        Path arquivo = uploadStorageProperties.resolveFromPublicUrl(fotoUrl);
        if (arquivo == null) {
            return;
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            // A foto pode ter sido removida manualmente; o perfil ainda deve ser limpo.
        }
    }

    private String obterExtensao(String nomeArquivo) {
        int posicao = nomeArquivo.lastIndexOf('.');
        if (posicao < 0) {
            return ".jpg";
        }
        String extensao = nomeArquivo.substring(posicao).toLowerCase(Locale.ROOT);
        if (!extensao.equals(".png") && !extensao.equals(".jpg") && !extensao.equals(".jpeg")) {
            return ".jpg";
        }
        return extensao;
    }

    private void validarEmailUnico(String email, Long ignorarId) {
        analistaRepository.findByEmailIgnoreCase(email)
                .filter(a -> ignorarId == null || !a.getId().equals(ignorarId))
                .ifPresent(a -> {
                    throw new IllegalArgumentException("E-mail ja cadastrado para outro analista.");
                });
    }

    private String nivelOuPadrao(String nivel) {
        if (nivel == null || nivel.isBlank()) {
            return "Nível 1";
        }
        return nivel.trim();
    }

    private void aplicarStatusAtivo(Analista analista, boolean ativo) {
        analista.setAtivo(ativo);
        if (!ativo) {
            analista.setOnline(false);
            analista.setStatusOperador(StatusOperador.OFFLINE);
            analista.setAuthToken(null);
            analista.setAuthTokenExpiraEm(null);
        }
    }

    private StatusOperador converterStatusOperador(String statusOperador) {
        try {
            return StatusOperador.valueOf(statusOperador);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Status do operador inválido: " + statusOperador);
        }
    }

    static PerfilAcesso resolverPerfilAcesso(Analista analista) {
        if (analista == null || analista.getPerfilAcesso() == null) {
            return PerfilAcesso.ANALISTA;
        }
        return analista.getPerfilAcesso();
    }

    private PerfilAcesso converterPerfilAcesso(String perfilAcesso) {
        if (perfilAcesso == null || perfilAcesso.isBlank()) {
            throw new IllegalArgumentException("Perfil de acesso inválido");
        }
        try {
            return PerfilAcesso.valueOf(perfilAcesso.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Perfil de acesso inválido: " + perfilAcesso);
        }
    }

    private AnalistaResponseDTO converterAnalista(Analista analista) {
        AnalistaResponseDTO response = new AnalistaResponseDTO();
        response.setId(analista.getId());
        response.setNome(analista.getNome());
        response.setNomeCompleto(analista.getNomeCompleto());
        response.setCpf(analista.getCpf());
        response.setCep(analista.getCep());
        response.setRua(analista.getRua());
        response.setNumero(analista.getNumero());
        response.setBairro(analista.getBairro());
        response.setCidade(analista.getCidade());
        response.setEstado(analista.getEstado());
        response.setUf(analista.getUf());
        response.setPais(analista.getPais());
        response.setCelular(analista.getCelular());
        response.setDataNascimento(analista.getDataNascimento());
        response.setEmail(analista.getEmail());
        response.setNivel(analista.getNivel());
        response.setPerfilAcesso(resolverPerfilAcesso(analista).name());
        response.setFotoUrl(analista.getFotoUrl());
        response.setStatusOperador(obterStatusOperador(analista).name());
        response.setOnline(analista.getOnline());
        response.setAtivo(analista.getAtivo());
        response.setDataCadastro(analista.getDataCadastro());
        return response;
    }

    private StatusOperador obterStatusOperador(Analista analista) {
        if (analista.getStatusOperador() != null) {
            return analista.getStatusOperador();
        }
        return Boolean.TRUE.equals(analista.getOnline()) ? StatusOperador.ONLINE : StatusOperador.OFFLINE;
    }

    private TicketResponseDTO converterTicketResumo(Ticket ticket) {
        TicketResponseDTO response = new TicketResponseDTO();
        response.setId((long) ticket.getId());
        response.setNumeroTicket(ticket.getNumeroTicket());
        response.setCliente(ticket.getCliente().getNome());
        response.setTelefone(ticket.getCliente().getTelefone());
        response.setCanal(ticket.getCanal());
        response.setMensagemInicial(ticket.getMensagemInicial());
        response.setStatus(ticket.getStatus().name());
        response.setDataAbertura(ticket.getDataAbertura());
        response.setDataPrimeiroAtendimento(ticket.getDataPrimeiroAtendimento());
        response.setDataEncerramento(ticket.getDataEncerramento());
        if (ticket.getAnalistaResponsavel() != null) {
            response.setAnalistaResponsavelId(ticket.getAnalistaResponsavel().getId());
            response.setAnalistaResponsavelNome(ticket.getAnalistaResponsavel().getNomeCompleto() != null
                    ? ticket.getAnalistaResponsavel().getNomeCompleto()
                    : ticket.getAnalistaResponsavel().getNome());
            response.setAnalistaResponsavelEmail(ticket.getAnalistaResponsavel().getEmail());
        }
        return response;
    }
}

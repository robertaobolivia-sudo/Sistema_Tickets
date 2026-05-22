package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoRequestDTO;
import com.suporte.tickets.dto.ContatoResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.ContatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContatoService {

    private final ContatoRepository contatoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public ContatoResponseDTO criar(ContatoRequestDTO dto) {
        if (dto.getClienteId() == null) {
            throw new IllegalArgumentException("Cliente e obrigatorio.");
        }
        Cliente cliente = buscarCliente(dto.getClienteId());
        String whatsappNorm = exigirWhatsappNormalizado(dto.getWhatsapp());
        if (contatoRepository.existsByCliente_IdAndWhatsappNormalizado(cliente.getId(), whatsappNorm)) {
            throw new IllegalArgumentException(
                    "Ja existe contato com este WhatsApp para o cliente informado.");
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
    public ContatoResponseDTO buscarPorClienteEWhatsapp(Integer clienteId, String whatsapp) {
        String norm = TicketAtivoService.normalizarTelefone(whatsapp);
        if (norm == null) {
            throw new IllegalArgumentException("WhatsApp invalido.");
        }
        return contatoRepository.findByCliente_IdAndWhatsappNormalizado(clienteId, norm)
                .map(ContatoResponseDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Contato nao encontrado para o cliente e WhatsApp informados."));
    }

    /**
     * Cria contato se nao existir para Cliente + WhatsApp (uso futuro integracao).
     */
    @Transactional
    public ContatoResponseDTO criarSeNaoExistir(Integer clienteId, String whatsapp, String nomeOpcional) {
        String norm = exigirWhatsappNormalizado(whatsapp);
        return contatoRepository.findByCliente_IdAndWhatsappNormalizado(clienteId, norm)
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
}

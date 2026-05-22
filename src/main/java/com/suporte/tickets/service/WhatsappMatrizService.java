package com.suporte.tickets.service;

import com.suporte.tickets.dto.WhatsappMatrizRequestDTO;
import com.suporte.tickets.dto.WhatsappMatrizResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.WhatsappMatriz;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.WhatsappMatrizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WhatsappMatrizService {

    private final WhatsappMatrizRepository whatsappMatrizRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public WhatsappMatrizResponseDTO criar(WhatsappMatrizRequestDTO dto) {
        if (dto.getNumero() == null || dto.getNumero().isBlank()) {
            throw new IllegalArgumentException("Numero do WhatsApp matriz e obrigatorio.");
        }
        Cliente cliente = buscarCliente(dto.getClienteId());
        String norm = exigirNumeroNormalizado(dto.getNumero());
        if (whatsappMatrizRepository.existsByNumeroNormalizado(norm)) {
            throw new IllegalArgumentException("Ja existe WhatsApp matriz com este numero.");
        }
        WhatsappMatriz m = new WhatsappMatriz();
        m.setCliente(cliente);
        aplicarNumero(m, dto.getNumero(), norm);
        preencherCampos(m, dto, true);
        return WhatsappMatrizResponseDTO.fromEntity(whatsappMatrizRepository.save(m));
    }

    @Transactional
    public WhatsappMatrizResponseDTO atualizar(Integer id, WhatsappMatrizRequestDTO dto) {
        WhatsappMatriz m = buscarEntidade(id);
        if (dto.getNumero() != null && !dto.getNumero().isBlank()) {
            String norm = exigirNumeroNormalizado(dto.getNumero());
            if (whatsappMatrizRepository.existsByNumeroNormalizadoAndIdNot(norm, id)) {
                throw new IllegalArgumentException("Ja existe WhatsApp matriz com este numero.");
            }
            aplicarNumero(m, dto.getNumero(), norm);
        }
        preencherCampos(m, dto, false);
        return WhatsappMatrizResponseDTO.fromEntity(whatsappMatrizRepository.save(m));
    }

    @Transactional(readOnly = true)
    public WhatsappMatrizResponseDTO buscarPorId(Integer id) {
        return WhatsappMatrizResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<WhatsappMatrizResponseDTO> listarPorCliente(Integer clienteId) {
        buscarCliente(clienteId);
        return whatsappMatrizRepository.findByCliente_IdOrderByNomeAscNumeroAsc(clienteId)
                .stream()
                .map(WhatsappMatrizResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WhatsappMatrizResponseDTO buscarPorNumeroNormalizado(String numero) {
        String norm = exigirNumeroNormalizado(numero);
        return whatsappMatrizRepository.findByNumeroNormalizado(norm)
                .map(WhatsappMatrizResponseDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("WhatsApp matriz nao encontrado para o numero informado."));
    }

    @Transactional(readOnly = true)
    public Cliente resolverClientePorWhatsappMatrizId(Integer whatsappMatrizId) {
        WhatsappMatriz m = buscarEntidade(whatsappMatrizId);
        if (!Boolean.TRUE.equals(m.getAtivo())) {
            throw new IllegalArgumentException("WhatsApp matriz inativo.");
        }
        return m.getCliente();
    }

    @Transactional(readOnly = true)
    public Cliente resolverClientePorNumeroMatriz(String numeroMatriz) {
        return resolverMatrizAtivaPorNumero(numeroMatriz).getCliente();
    }

    @Transactional(readOnly = true)
    public WhatsappMatriz resolverMatrizAtivaPorNumero(String numeroMatriz) {
        String norm = exigirNumeroNormalizado(numeroMatriz);
        WhatsappMatriz m = whatsappMatrizRepository.findByNumeroNormalizado(norm)
                .orElseThrow(() -> new RuntimeException("WhatsApp matriz nao encontrado para o numero informado."));
        if (!Boolean.TRUE.equals(m.getAtivo())) {
            throw new IllegalArgumentException("WhatsApp matriz inativo.");
        }
        return m;
    }

    @Transactional(readOnly = true)
    public WhatsappMatriz buscarEntidade(Integer id) {
        return whatsappMatrizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WhatsApp matriz nao encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public WhatsappMatriz buscarEntidadeAtivaPorId(Integer id) {
        WhatsappMatriz m = buscarEntidade(id);
        if (!Boolean.TRUE.equals(m.getAtivo())) {
            throw new IllegalArgumentException("WhatsApp matriz inativo: " + id);
        }
        return m;
    }

    @Transactional
    public WhatsappMatrizResponseDTO ativar(Integer id) {
        WhatsappMatriz m = buscarEntidade(id);
        m.setAtivo(true);
        return WhatsappMatrizResponseDTO.fromEntity(whatsappMatrizRepository.save(m));
    }

    @Transactional
    public WhatsappMatrizResponseDTO inativar(Integer id) {
        WhatsappMatriz m = buscarEntidade(id);
        m.setAtivo(false);
        return WhatsappMatrizResponseDTO.fromEntity(whatsappMatrizRepository.save(m));
    }

    private Cliente buscarCliente(Integer clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado: " + clienteId));
    }

    private static String exigirNumeroNormalizado(String numero) {
        String norm = TicketAtivoService.normalizarTelefone(numero);
        if (norm == null) {
            throw new IllegalArgumentException("Numero do WhatsApp matriz invalido.");
        }
        return norm;
    }

    private static void aplicarNumero(WhatsappMatriz m, String numeroBruto, String norm) {
        m.setNumero(numeroBruto == null ? norm : numeroBruto.trim());
        m.setNumeroNormalizado(norm);
    }

    private static void preencherCampos(WhatsappMatriz m, WhatsappMatrizRequestDTO dto, boolean criacao) {
        if (dto.getNome() != null) {
            m.setNome(dto.getNome().isBlank() ? null : dto.getNome().trim());
        }
        if (dto.getAtivo() != null) {
            m.setAtivo(dto.getAtivo());
        } else if (criacao) {
            m.setAtivo(true);
        }
        m.setProvedor(trimOrNull(dto.getProvedor()));
        m.setIdentificadorExterno(trimOrNull(dto.getIdentificadorExterno()));
    }

    private static String trimOrNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}

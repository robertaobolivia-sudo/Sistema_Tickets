package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoClienteRequestDTO;
import com.suporte.tickets.dto.ContatoClienteResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.ContatoCliente;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.ContatoClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContatoClienteService {

    private final ContatoClienteRepository contatoClienteRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public ContatoClienteResponseDTO criar(Integer clienteId, ContatoClienteRequestDTO dto) {
        validarTelefoneOuCelular(dto);
        Cliente cliente = buscarCliente(clienteId);
        ContatoCliente contato = new ContatoCliente();
        contato.setCliente(cliente);
        preencher(contato, dto, clienteId);
        if (Boolean.TRUE.equals(dto.getPrincipal())) {
            contatoClienteRepository.removerPrincipalDosDemais(clienteId, null);
            contato.setPrincipal(true);
        }
        return ContatoClienteResponseDTO.fromEntity(contatoClienteRepository.save(contato));
    }

    @Transactional(readOnly = true)
    public List<ContatoClienteResponseDTO> listarPorCliente(Integer clienteId) {
        buscarCliente(clienteId);
        return contatoClienteRepository.findByCliente_IdOrderByNomeAsc(clienteId).stream()
                .map(ContatoClienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContatoClienteResponseDTO> listarAtivosPorCliente(Integer clienteId) {
        buscarCliente(clienteId);
        return contatoClienteRepository.findByCliente_IdAndAtivoTrueOrderByNomeAsc(clienteId).stream()
                .map(ContatoClienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContatoClienteResponseDTO buscarPorId(Integer id) {
        return ContatoClienteResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public ContatoClienteResponseDTO atualizar(Integer id, ContatoClienteRequestDTO dto) {
        validarTelefoneOuCelular(dto);
        ContatoCliente contato = buscarEntidade(id);
        Integer clienteId = contato.getCliente().getId();
        if (dto.getClienteId() != null && !dto.getClienteId().equals(clienteId)) {
            throw new RuntimeException("Nao e permitido alterar o cliente do contato");
        }
        preencher(contato, dto, clienteId);
        if (Boolean.TRUE.equals(dto.getPrincipal())) {
            contatoClienteRepository.removerPrincipalDosDemais(clienteId, id);
            contato.setPrincipal(true);
        }
        return ContatoClienteResponseDTO.fromEntity(contatoClienteRepository.save(contato));
    }

    @Transactional
    public ContatoClienteResponseDTO ativar(Integer id) {
        ContatoCliente contato = buscarEntidade(id);
        contato.setAtivo(true);
        return ContatoClienteResponseDTO.fromEntity(contatoClienteRepository.save(contato));
    }

    @Transactional
    public ContatoClienteResponseDTO inativar(Integer id) {
        ContatoCliente contato = buscarEntidade(id);
        contato.setAtivo(false);
        if (Boolean.TRUE.equals(contato.getPrincipal())) {
            contato.setPrincipal(false);
        }
        return ContatoClienteResponseDTO.fromEntity(contatoClienteRepository.save(contato));
    }

    @Transactional
    public ContatoClienteResponseDTO definirPrincipal(Integer id) {
        ContatoCliente contato = buscarEntidade(id);
        if (!Boolean.TRUE.equals(contato.getAtivo())) {
            throw new RuntimeException("Contato inativo nao pode ser principal");
        }
        Integer clienteId = contato.getCliente().getId();
        contatoClienteRepository.removerPrincipalDosDemais(clienteId, id);
        contato.setPrincipal(true);
        return ContatoClienteResponseDTO.fromEntity(contatoClienteRepository.save(contato));
    }

    @Transactional(readOnly = true)
    public List<ContatoClienteResponseDTO> pesquisar(String termo) {
        if (termo == null || termo.isBlank()) {
            return contatoClienteRepository.findAll().stream()
                    .map(ContatoClienteResponseDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        return contatoClienteRepository.pesquisar(termo.trim()).stream()
                .map(ContatoClienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContatoCliente buscarEntidadeParaTicket(Integer contatoId, Integer clienteId) {
        ContatoCliente contato = buscarEntidade(contatoId);
        if (!contato.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("Contato nao pertence ao cliente informado");
        }
        if (!Boolean.TRUE.equals(contato.getAtivo())) {
            throw new RuntimeException("Contato solicitante deve estar ativo");
        }
        return contato;
    }

    private void preencher(ContatoCliente contato, ContatoClienteRequestDTO dto, Integer clienteIdEsperado) {
        if (dto.getClienteId() != null && !dto.getClienteId().equals(clienteIdEsperado)) {
            throw new RuntimeException("Cliente do contato invalido");
        }
        contato.setNome(dto.getNome().trim());
        contato.setCargo(trimOrNull(dto.getCargo()));
        contato.setTelefone(trimOrNull(dto.getTelefone()));
        contato.setCelular(trimOrNull(dto.getCelular()));
        contato.setEmail(trimOrNull(dto.getEmail()));
        contato.setObservacoes(trimOrNull(dto.getObservacoes()));
        if (dto.getPrincipal() != null) {
            contato.setPrincipal(dto.getPrincipal());
        }
    }

    private void validarTelefoneOuCelular(ContatoClienteRequestDTO dto) {
        boolean temTelefone = dto.getTelefone() != null && !dto.getTelefone().isBlank();
        boolean temCelular = dto.getCelular() != null && !dto.getCelular().isBlank();
        if (!temTelefone && !temCelular) {
            throw new RuntimeException("Informe telefone ou celular do contato");
        }
    }

    private Cliente buscarCliente(Integer clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado: " + clienteId));
    }

    private ContatoCliente buscarEntidade(Integer id) {
        return contatoClienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contato nao encontrado: " + id));
    }

    private String trimOrNull(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}

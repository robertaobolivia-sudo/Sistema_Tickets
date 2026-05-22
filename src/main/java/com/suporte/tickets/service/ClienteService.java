package com.suporte.tickets.service;

import com.suporte.tickets.config.UploadStorageProperties;
import com.suporte.tickets.dto.ClienteRequestDTO;
import com.suporte.tickets.dto.ClienteResponseDTO;
import com.suporte.tickets.entity.ClassificacaoCliente;
import com.suporte.tickets.entity.Carteira;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.repository.CarteiraRepository;
import com.suporte.tickets.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final CarteiraRepository carteiraRepository;
    private final UploadStorageProperties uploadStorageProperties;

    @Transactional
    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        Cliente cliente = preencher(new Cliente(), dto);
        return ClienteResponseDTO.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAllByOrderByDataCadastroDesc()
                .stream()
                .map(ClienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarAtivos() {
        return clienteRepository.findByAtivoTrueOrderByDataCadastroDesc()
                .stream()
                .map(ClienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Integer id) {
        return ClienteResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> pesquisar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }
        return clienteRepository.pesquisar(termo.trim())
                .stream()
                .map(ClienteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteResponseDTO atualizar(Integer id, ClienteRequestDTO dto) {
        Cliente cliente = buscarEntidade(id);
        preencher(cliente, dto);
        return ClienteResponseDTO.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public void excluir(Integer id) {
        clienteRepository.delete(buscarEntidade(id));
    }

    @Transactional
    public ClienteResponseDTO ativar(Integer id) {
        Cliente cliente = buscarEntidade(id);
        cliente.setStatus("ATIVO");
        cliente.setAtivo(true);
        return ClienteResponseDTO.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponseDTO inativar(Integer id) {
        Cliente cliente = buscarEntidade(id);
        cliente.setStatus("INATIVO");
        cliente.setAtivo(false);
        return ClienteResponseDTO.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponseDTO salvarArteHeaderChats(Integer id, MultipartFile arte) {
        ArteHeaderChatsUploadSupport.validarArteMultipart(arte);

        Cliente cliente = buscarEntidade(id);
        removerArteClienteArquivoSeExistir(cliente.getArteHeaderChatsUrl());

        String extensao = ArteHeaderChatsUploadSupport.obterExtensaoImagem(arte);
        String nomeArquivo = "cliente-" + id + "-" + UUID.randomUUID() + extensao;

        try {
            Path destinoDir = uploadStorageProperties.getClientesHeaderChatsDir();
            Files.createDirectories(destinoDir);
            Path destino = destinoDir.resolve(nomeArquivo);
            Files.copy(arte.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            cliente.setArteHeaderChatsUrl(uploadStorageProperties.toClienteHeaderChatsPublicUrl(nomeArquivo));
            return ClienteResponseDTO.fromEntity(clienteRepository.save(cliente));
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arte do header do Chats", e);
        }
    }

    private void removerArteClienteArquivoSeExistir(String arteUrl) {
        Path arquivo = uploadStorageProperties.resolveClienteHeaderChatsPublicUrl(arteUrl);
        if (arquivo == null) {
            return;
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException ignored) {
            // Arquivo pode ter sido removido manualmente.
        }
    }

    private Cliente buscarEntidade(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado: " + id));
    }

    private Cliente preencher(Cliente cliente, ClienteRequestDTO dto) {
        cliente.setNome(dto.getNome().trim());
        cliente.setTelefone(normalizarTelefone(dto.getTelefone()));
        cliente.setTelefoneContato(normalizarTelefone(dto.getTelefoneContato()));
        cliente.setEmail(dto.getEmail().trim().toLowerCase());
        cliente.setEmpresa(trimOrNull(dto.getEmpresa()));
        cliente.setCnpj(trimOrNull(dto.getCnpj()));
        cliente.setCidade(trimOrNull(dto.getCidade()));
        cliente.setUf(trimOrNull(dto.getUf()));
        cliente.setEndereco(trimOrNull(dto.getEndereco()));
        String status = dto.getStatus() == null || dto.getStatus().isBlank()
                ? "ATIVO"
                : dto.getStatus().trim().toUpperCase();
        cliente.setStatus(status);
        cliente.setAtivo(!"INATIVO".equalsIgnoreCase(status));
        cliente.setClassificacaoCliente(ClassificacaoCliente.parse(dto.getClassificacaoCliente()));
        cliente.setObservacoes(trimOrNull(dto.getObservacoes()));

        aplicarCarteiraLegadoSeInformada(cliente, dto);

        return cliente;
    }

    /**
     * Carteira deixa de ser conceito principal (Sprint 188). Só aplica FK se vier {@code carteiraId}
     * explícito (integrações legadas). Não cria Carteira por nome nem altera vínculo na tela Clientes.
     */
    private void aplicarCarteiraLegadoSeInformada(Cliente cliente, ClienteRequestDTO dto) {
        if (dto.getCarteiraId() == null) {
            return;
        }
        Carteira carteira = carteiraRepository.findById(dto.getCarteiraId())
                .orElseThrow(() -> new RuntimeException("Carteira nao encontrada: " + dto.getCarteiraId()));
        cliente.setCarteira(carteira);
    }

    private String normalizarTelefone(String telefone) {
        return telefone == null ? null : telefone.replaceAll("\\D", "");
    }

    private String trimOrNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
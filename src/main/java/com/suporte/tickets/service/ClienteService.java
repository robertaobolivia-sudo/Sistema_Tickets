package com.suporte.tickets.service;

import com.suporte.tickets.config.UploadStorageProperties;
import com.suporte.tickets.dto.ClienteRequestDTO;
import com.suporte.tickets.dto.ClienteResponseDTO;
import com.suporte.tickets.entity.ClassificacaoCliente;
import com.suporte.tickets.entity.Cliente;
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
        String responsavel = trimOrNull(dto.getResponsavel());
        if (responsavel == null) {
            responsavel = dto.getNome().trim();
        }
        cliente.setNome(responsavel);
        cliente.setResponsavel(responsavel);

        String razaoSocial = trimOrNull(dto.getRazaoSocial());
        if (razaoSocial == null) {
            razaoSocial = trimOrNull(dto.getEmpresa());
        }
        cliente.setRazaoSocial(razaoSocial);
        cliente.setEmpresa(razaoSocial);

        String whatsapp = normalizarTelefone(primeiroNaoVazio(dto.getWhatsapp(), dto.getTelefone()));
        cliente.setWhatsapp(whatsapp);
        cliente.setTelefone(whatsapp);
        cliente.setTelefoneContato(normalizarTelefone(dto.getTelefoneContato()));
        cliente.setEmail(dto.getEmail().trim().toLowerCase());
        cliente.setCnpj(trimOrNull(dto.getCnpj()));
        cliente.setInscricaoEstadual(trimOrNull(dto.getInscricaoEstadual()));
        cliente.setCidade(trimOrNull(dto.getCidade()));
        cliente.setUf(trimOrNull(dto.getUf()));
        cliente.setEndereco(trimOrNull(dto.getEndereco()));
        cliente.setCep(trimOrNull(dto.getCep()));
        cliente.setSite(trimOrNull(dto.getSite()));
        cliente.setHorarioFuncionamento(trimOrNull(dto.getHorarioFuncionamento()));
        String status = dto.getStatus() == null || dto.getStatus().isBlank()
                ? "ATIVO"
                : dto.getStatus().trim().toUpperCase();
        cliente.setStatus(status);
        cliente.setAtivo(!"INATIVO".equalsIgnoreCase(status));
        cliente.setClassificacaoCliente(ClassificacaoCliente.parse(dto.getClassificacaoCliente()));
        cliente.setObservacoes(trimOrNull(dto.getObservacoes()));

        return cliente;
    }

    private String normalizarTelefone(String telefone) {
        return telefone == null ? null : telefone.replaceAll("\\D", "");
    }

    private String trimOrNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static String primeiroNaoVazio(String... valores) {
        if (valores == null) {
            return null;
        }
        for (String v : valores) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }
}
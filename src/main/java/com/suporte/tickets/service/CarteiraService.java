package com.suporte.tickets.service;

import com.suporte.tickets.config.UploadStorageProperties;
import com.suporte.tickets.dto.CarteiraRequestDTO;
import com.suporte.tickets.dto.CarteiraResponseDTO;
import com.suporte.tickets.entity.Carteira;
import com.suporte.tickets.repository.CarteiraRepository;
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
public class CarteiraService {

    private final CarteiraRepository carteiraRepository;
    private final UploadStorageProperties uploadStorageProperties;

    @Transactional(readOnly = true)
    public List<CarteiraResponseDTO> listar() {
        return carteiraRepository.findAllByOrderByNomeAsc().stream()
                .map(CarteiraResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CarteiraResponseDTO buscar(Integer id) {
        return CarteiraResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public CarteiraResponseDTO criar(CarteiraRequestDTO dto) {
        validarNomeDuplicado(dto.getNome(), null);
        Carteira carteira = new Carteira();
        carteira.setNome(dto.getNome().trim());
        return CarteiraResponseDTO.fromEntity(carteiraRepository.save(carteira));
    }

    @Transactional
    public CarteiraResponseDTO atualizar(Integer id, CarteiraRequestDTO dto) {
        Carteira carteira = buscarEntidade(id);
        validarNomeDuplicado(dto.getNome(), id);
        carteira.setNome(dto.getNome().trim());
        return CarteiraResponseDTO.fromEntity(carteiraRepository.save(carteira));
    }

    @Transactional
    public CarteiraResponseDTO salvarArteHeaderChats(Integer id, MultipartFile arte) {
        ArteHeaderChatsUploadSupport.validarArteMultipart(arte);

        Carteira carteira = buscarEntidade(id);
        removerArteArquivoSeExistir(carteira.getArteHeaderChatsUrl());

        String extensao = ArteHeaderChatsUploadSupport.obterExtensaoImagem(arte);
        String nomeArquivo = "carteira-" + id + "-" + UUID.randomUUID() + extensao;

        try {
            Path destinoDir = uploadStorageProperties.getConexoesHeaderChatsDir();
            Files.createDirectories(destinoDir);
            Path destino = destinoDir.resolve(nomeArquivo);
            Files.copy(arte.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            carteira.setArteHeaderChatsUrl(uploadStorageProperties.toConexaoHeaderChatsPublicUrl(nomeArquivo));
            return CarteiraResponseDTO.fromEntity(carteiraRepository.save(carteira));
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arte do header do Chats", e);
        }
    }

    private void removerArteArquivoSeExistir(String arteUrl) {
        Path arquivo = uploadStorageProperties.resolveConexaoHeaderChatsPublicUrl(arteUrl);
        if (arquivo == null) {
            return;
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException ignored) {
            // Arquivo pode ter sido removido manualmente.
        }
    }

    private Carteira buscarEntidade(Integer id) {
        return carteiraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conexão/Revenda não encontrada: " + id));
    }

    private void validarNomeDuplicado(String nome, Integer idAtual) {
        carteiraRepository.findByNomeIgnoreCase(nome.trim())
                .filter(c -> idAtual == null || !c.getId().equals(idAtual))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Já existe uma conexão/revenda com este nome.");
                });
    }
}

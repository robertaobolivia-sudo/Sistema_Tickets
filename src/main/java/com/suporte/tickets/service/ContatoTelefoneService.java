package com.suporte.tickets.service;

import com.suporte.tickets.dto.ContatoTelefoneRequestDTO;
import com.suporte.tickets.dto.ContatoTelefoneResponseDTO;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.ContatoTelefone;
import com.suporte.tickets.repository.ContatoRepository;
import com.suporte.tickets.repository.ContatoTelefoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContatoTelefoneService {

    private final ContatoTelefoneRepository contatoTelefoneRepository;
    private final ContatoRepository contatoRepository;

    @Transactional(readOnly = true)
    public List<ContatoTelefoneResponseDTO> listarPorContato(Integer contatoId) {
        buscarContato(contatoId);
        return contatoTelefoneRepository.findByContato_IdOrderByTelefoneNormalizadoAsc(contatoId).stream()
                .map(ContatoTelefoneResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContatoTelefoneResponseDTO adicionar(Integer contatoId, ContatoTelefoneRequestDTO dto) {
        Contato contato = buscarContato(contatoId);
        String norm = exigirTelefoneNormalizado(dto.getTelefone());
        validarTelefoneAdicional(contato, norm);

        ContatoTelefone vinculo = new ContatoTelefone();
        vinculo.setContato(contato);
        vinculo.setCliente(contato.getCliente());
        vinculo.setTelefone(dto.getTelefone().trim());
        vinculo.setTelefoneNormalizado(norm);
        vinculo.setPrincipal(false);
        vinculo.setOrigem(resolverOrigem(dto.getOrigem()));
        return ContatoTelefoneResponseDTO.fromEntity(contatoTelefoneRepository.save(vinculo));
    }

    private void validarTelefoneAdicional(Contato contato, String norm) {
        if (norm.equals(contato.getWhatsappNormalizado())) {
            throw new IllegalArgumentException(
                    "Este numero ja e o WhatsApp principal do contato. O principal nao pode ser cadastrado como adicional.");
        }
        Integer clienteId = contato.getCliente().getId();
        if (contatoRepository.existsByCliente_IdAndWhatsappNormalizado(clienteId, norm)
                || contatoTelefoneRepository.existsByCliente_IdAndTelefoneNormalizado(clienteId, norm)) {
            throw new IllegalArgumentException(
                    "Ja existe contato ou telefone adicional com este numero para o cliente informado.");
        }
    }

    private Contato buscarContato(Integer contatoId) {
        return contatoRepository.findById(contatoId)
                .orElseThrow(() -> new RuntimeException("Contato nao encontrado: " + contatoId));
    }

    private static String exigirTelefoneNormalizado(String telefone) {
        String norm = TicketAtivoService.normalizarTelefone(telefone);
        if (norm == null) {
            throw new IllegalArgumentException("Telefone invalido. Informe digitos validos.");
        }
        return norm;
    }

    private static String resolverOrigem(String origem) {
        if (origem == null || origem.isBlank()) {
            return ContatoTelefone.ORIGEM_ADICIONAL;
        }
        String trimmed = origem.trim().toUpperCase();
        if (ContatoTelefone.ORIGEM_CADASTRO_MANUAL.equals(trimmed)) {
            return ContatoTelefone.ORIGEM_CADASTRO_MANUAL;
        }
        return ContatoTelefone.ORIGEM_ADICIONAL;
    }
}

package com.suporte.tickets.service;

import com.suporte.tickets.dto.EtiquetaResponseDTO;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.ContatoEtiqueta;
import com.suporte.tickets.entity.Etiqueta;
import com.suporte.tickets.entity.TicketClassificacaoOperacional;
import com.suporte.tickets.repository.ContatoEtiquetaRepository;
import com.suporte.tickets.repository.ContatoRepository;
import com.suporte.tickets.repository.EtiquetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContatoEtiquetaService {

    private final ContatoRepository contatoRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final ContatoEtiquetaRepository contatoEtiquetaRepository;

    @Transactional(readOnly = true)
    public List<EtiquetaResponseDTO> listarPorContatoId(Integer contatoId) {
        Contato contato = buscarContato(contatoId);
        return contatoEtiquetaRepository.findByContatoOrderByEtiqueta_NomeAsc(contato)
                .stream()
                .map(ContatoEtiqueta::getEtiqueta)
                .map(EtiquetaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EtiquetaResponseDTO> substituirVinculosAtivos(Integer contatoId, List<Long> etiquetaIds) {
        Contato contato = buscarContato(contatoId);
        List<Long> idsAtivosSolicitados = EtiquetaVinculoIdsNormalizer.normalizarIdsEtiqueta(etiquetaIds);

        List<ContatoEtiqueta> atuais = contatoEtiquetaRepository.findByContatoOrderByEtiqueta_NomeAsc(contato);
        Set<Long> legadoInativos = atuais.stream()
                .filter(v -> v.getEtiqueta() != null && Boolean.FALSE.equals(v.getEtiqueta().getAtivo()))
                .map(v -> v.getEtiqueta().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        contatoEtiquetaRepository.deleteByContato(contato);
        contatoEtiquetaRepository.flush();

        Set<Long> idsFinais = new LinkedHashSet<>(legadoInativos);
        for (Long id : idsAtivosSolicitados) {
            Etiqueta etiqueta = etiquetaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Etiqueta nao encontrada: " + id));
            if (!Boolean.TRUE.equals(etiqueta.getAtivo())) {
                throw new IllegalArgumentException("Etiqueta inativa nao pode ser vinculada: " + id);
            }
            idsFinais.add(id);
        }

        for (Long id : idsFinais) {
            Etiqueta etiqueta = etiquetaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Etiqueta nao encontrada: " + id));
            ContatoEtiqueta vinculo = new ContatoEtiqueta();
            vinculo.setContato(contato);
            vinculo.setEtiqueta(etiqueta);
            contatoEtiquetaRepository.save(vinculo);
        }

        return listarPorContatoId(contatoId);
    }

    /**
     * Sprint 303: resolve classificação operacional pela primeira etiqueta do contato
     * que corresponda a um valor de TicketClassificacaoOperacional (ex: "Indevido", "Contato Pessoal").
     */
    @Transactional(readOnly = true)
    public TicketClassificacaoOperacional resolverClassificacaoOperacional(Contato contato) {
        if (contato == null) {
            return null;
        }
        List<ContatoEtiqueta> vinculos = contatoEtiquetaRepository.findByContatoOrderByEtiqueta_NomeAsc(contato);
        for (ContatoEtiqueta vinculo : vinculos) {
            String nome = vinculo.getEtiqueta().getNome();
            if (nome == null) {
                continue;
            }
            String normalizado = nome.trim().toUpperCase().replace(' ', '_').replace('-', '_');
            try {
                return TicketClassificacaoOperacional.valueOf(normalizado);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    private Contato buscarContato(Integer contatoId) {
        if (contatoId == null || contatoId <= 0) {
            throw new IllegalArgumentException("Contato invalido.");
        }
        return contatoRepository.findById(contatoId)
                .orElseThrow(() -> new IllegalArgumentException("Contato nao encontrado: " + contatoId));
    }
}

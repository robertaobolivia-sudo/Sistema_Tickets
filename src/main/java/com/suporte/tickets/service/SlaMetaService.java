package com.suporte.tickets.service;

import com.suporte.tickets.dto.SlaMetaDTO;
import com.suporte.tickets.dto.SlaMetaSeedResultadoDTO;
import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.SlaMeta;
import com.suporte.tickets.repository.SlaMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlaMetaService {

    private final SlaMetaRepository slaMetaRepository;

    @Transactional
    public List<SlaMetaDTO> listarTodos() {
        garantirMetasPadraoSeNecessario();
        return slaMetaRepository.findAllByOrderByPrioridadeAsc().stream()
                .sorted(Comparator.comparing(m -> ordemPrioridade(m.getPrioridade())))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public List<SlaMetaDTO> listarAtivas() {
        garantirMetasPadraoSeNecessario();
        return slaMetaRepository.findByAtivoTrueOrderByPrioridadeAsc().stream()
                .sorted(Comparator.comparing(m -> ordemPrioridade(m.getPrioridade())))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public SlaMetaDTO obterPorPrioridade(String prioridadeStr) {
        PrioridadeTicket prioridade = parsePrioridade(prioridadeStr);
        garantirMetasPadraoSeNecessario();
        SlaMeta meta = slaMetaRepository.findFirstByPrioridadeAndAtivoTrue(prioridade)
                .or(() -> slaMetaRepository.findByPrioridade(prioridade))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Meta de SLA não encontrada para a prioridade informada."));
        return toDto(meta);
    }

    @Transactional
    public SlaMetaDTO atualizarPorPrioridade(String prioridadeStr, SlaMetaDTO dto) {
        PrioridadeTicket prioridade = parsePrioridade(prioridadeStr);
        validarPrazos(dto);
        garantirMetasPadraoSeNecessario();

        SlaMeta meta = slaMetaRepository.findByPrioridade(prioridade)
                .orElseGet(() -> criarMetaPadrao(prioridade));

        if (Boolean.TRUE.equals(dto.getAtivo())) {
            validarDuplicidadeAtiva(prioridade, meta.getId());
        }

        meta.setPrazoPrimeiroAtendimentoMinutos(dto.getPrazoPrimeiroAtendimentoMinutos());
        meta.setPrazoResolucaoMinutos(dto.getPrazoResolucaoMinutos());
        meta.setAtivo(dto.getAtivo() == null ? Boolean.TRUE : dto.getAtivo());

        return toDto(slaMetaRepository.save(meta));
    }

    @Transactional
    public SlaMetaSeedResultadoDTO seedDefault() {
        int criados = 0;
        int ignorados = 0;
        for (PrioridadeTicket prioridade : PrioridadeTicket.values()) {
            if (slaMetaRepository.findByPrioridade(prioridade).isPresent()) {
                ignorados++;
                continue;
            }
            slaMetaRepository.save(criarMetaPadrao(prioridade));
            criados++;
        }
        String mensagem = String.format(
                "Metas default: %d criadas, %d ignoradas (já existentes).", criados, ignorados);
        return new SlaMetaSeedResultadoDTO(mensagem, criados, ignorados);
    }

    /**
     * Consulta futura do motor de SLA. Ticket sem prioridade deve usar MEDIA (decisão documentada).
     */
    @Transactional
    public SlaMeta buscarMetaAtivaPorPrioridade(PrioridadeTicket prioridade) {
        PrioridadeTicket efetiva = prioridade != null ? prioridade : PrioridadeTicket.MEDIA;
        garantirMetasPadraoSeNecessario();
        return slaMetaRepository.findFirstByPrioridadeAndAtivoTrue(efetiva)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Meta de SLA ativa não encontrada para " + efetiva.name()));
    }

    @Transactional
    public void garantirMetasPadraoSeNecessario() {
        if (slaMetaRepository.count() == 0) {
            Arrays.stream(PrioridadeTicket.values()).forEach(this::criarMetaPadraoSalvar);
        }
    }

    private void criarMetaPadraoSalvar(PrioridadeTicket prioridade) {
        slaMetaRepository.save(criarMetaPadrao(prioridade));
    }

    private SlaMeta criarMetaPadrao(PrioridadeTicket prioridade) {
        SlaMeta meta = new SlaMeta();
        meta.setPrioridade(prioridade);
        meta.setAtivo(true);
        switch (prioridade) {
            case BAIXA -> {
                meta.setPrazoPrimeiroAtendimentoMinutos(240);
                meta.setPrazoResolucaoMinutos(1440);
            }
            case MEDIA -> {
                meta.setPrazoPrimeiroAtendimentoMinutos(120);
                meta.setPrazoResolucaoMinutos(960);
            }
            case ALTA -> {
                meta.setPrazoPrimeiroAtendimentoMinutos(60);
                meta.setPrazoResolucaoMinutos(480);
            }
            case CRITICA -> {
                meta.setPrazoPrimeiroAtendimentoMinutos(15);
                meta.setPrazoResolucaoMinutos(240);
            }
            default -> throw new IllegalStateException("Prioridade não suportada: " + prioridade);
        }
        return meta;
    }

    private void validarPrazos(SlaMetaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados da meta de SLA são obrigatórios.");
        }
        validarMinutos(dto.getPrazoPrimeiroAtendimentoMinutos(), "primeiro atendimento");
        validarMinutos(dto.getPrazoResolucaoMinutos(), "resolução");
    }

    private void validarMinutos(Integer valor, String rotulo) {
        if (valor == null) {
            throw new IllegalArgumentException("Informe o prazo de " + rotulo + " em minutos úteis.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O prazo de " + rotulo + " deve ser maior que zero.");
        }
    }

    private void validarDuplicidadeAtiva(PrioridadeTicket prioridade, Long idIgnorar) {
        if (idIgnorar == null) {
            return;
        }
        if (slaMetaRepository.existsByPrioridadeAndAtivoTrueAndIdNot(prioridade, idIgnorar)) {
            throw new IllegalArgumentException("Já existe uma meta ativa para esta prioridade.");
        }
    }

    private PrioridadeTicket parsePrioridade(String prioridadeStr) {
        if (prioridadeStr == null || prioridadeStr.isBlank()) {
            throw new IllegalArgumentException("Informe a prioridade.");
        }
        if (!PrioridadeTicket.isValido(prioridadeStr)) {
            throw new IllegalArgumentException(
                    "Prioridade inválida. Use BAIXA, MEDIA, ALTA ou CRITICA.");
        }
        return PrioridadeTicket.valueOf(prioridadeStr.trim().toUpperCase());
    }

    private int ordemPrioridade(PrioridadeTicket prioridade) {
        return switch (prioridade) {
            case CRITICA -> 0;
            case ALTA -> 1;
            case MEDIA -> 2;
            case BAIXA -> 3;
        };
    }

    private SlaMetaDTO toDto(SlaMeta meta) {
        SlaMetaDTO dto = new SlaMetaDTO();
        dto.setId(meta.getId());
        dto.setPrioridade(meta.getPrioridade().name());
        dto.setPrazoPrimeiroAtendimentoMinutos(meta.getPrazoPrimeiroAtendimentoMinutos());
        dto.setPrazoResolucaoMinutos(meta.getPrazoResolucaoMinutos());
        dto.setAtivo(meta.getAtivo());
        return dto;
    }
}

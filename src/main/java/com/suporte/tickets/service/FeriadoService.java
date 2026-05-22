package com.suporte.tickets.service;

import com.suporte.tickets.dto.FeriadoDTO;
import com.suporte.tickets.dto.FeriadoSeedResultadoDTO;
import com.suporte.tickets.dto.FeriadoVerificacaoDTO;
import com.suporte.tickets.entity.Feriado;
import com.suporte.tickets.entity.FeriadoEscopo;
import com.suporte.tickets.entity.FeriadoTipo;
import com.suporte.tickets.repository.FeriadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeriadoService {

    private static final String DESCRICAO_PADRAO = "Feriado";

    private final FeriadoRepository feriadoRepository;
    private final CalendarioSlaHelper calendarioSlaHelper;

    @Transactional(readOnly = true)
    public List<FeriadoDTO> listarTodos() {
        return feriadoRepository.findAllByOrderByDataAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<FeriadoDTO> listarAtivos() {
        return feriadoRepository.findByAtivoTrueOrderByDataAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public FeriadoVerificacaoDTO verificar(String dataStr) {
        LocalDate data = parseData(dataStr);
        return feriadoRepository.findFirstByDataAndAtivoTrue(data)
                .map(f -> new FeriadoVerificacaoDTO(
                        formatData(f.getData()),
                        true,
                        f.getDescricao(),
                        f.getEscopo().name()))
                .orElseGet(() -> new FeriadoVerificacaoDTO(formatData(data), false, "-", null));
    }

    @Transactional
    public FeriadoDTO criar(FeriadoDTO dto) {
        LocalDate data = parseData(dto.getData());
        validarDuplicidadeAtiva(data, null);
        Feriado entidade = new Feriado();
        aplicarDto(entidade, dto, data);
        return toDto(feriadoRepository.save(entidade));
    }

    @Transactional
    public FeriadoDTO atualizar(Long id, FeriadoDTO dto) {
        Feriado entidade = buscarEntidade(id);
        LocalDate data = parseData(dto.getData());
        validarDuplicidadeAtiva(data, id);
        aplicarDto(entidade, dto, data);
        return toDto(feriadoRepository.save(entidade));
    }

    @Transactional
    public FeriadoDTO ativar(Long id) {
        Feriado entidade = buscarEntidade(id);
        validarDuplicidadeAtiva(entidade.getData(), id);
        entidade.setAtivo(true);
        return toDto(feriadoRepository.save(entidade));
    }

    @Transactional
    public FeriadoDTO inativar(Long id) {
        Feriado entidade = buscarEntidade(id);
        entidade.setAtivo(false);
        return toDto(feriadoRepository.save(entidade));
    }

    @Transactional
    public FeriadoSeedResultadoDTO seedSaoPaulo2026() {
        List<SeedItem> itens = buildSeedSaoPaulo2026();
        int criados = 0;
        int atualizados = 0;
        int ignorados = 0;

        for (SeedItem item : itens) {
            var existente = feriadoRepository.findFirstByData(item.data());
            if (existente.isPresent()) {
                Feriado f = existente.get();
                if (Boolean.TRUE.equals(f.getAtivo())) {
                    ignorados++;
                    continue;
                }
                f.setDescricao(item.descricao());
                f.setTipo(item.tipo());
                f.setEscopo(item.escopo());
                f.setAtivo(true);
                feriadoRepository.save(f);
                atualizados++;
            } else {
                Feriado novo = new Feriado();
                novo.setData(item.data());
                novo.setDescricao(item.descricao());
                novo.setTipo(item.tipo());
                novo.setEscopo(item.escopo());
                novo.setAtivo(true);
                feriadoRepository.save(novo);
                criados++;
            }
        }

        String mensagem = String.format(
                "Carga 2026 São Paulo concluída: %d criados, %d reativados/atualizados, %d ignorados (já ativos).",
                criados, atualizados, ignorados);
        return new FeriadoSeedResultadoDTO(mensagem, criados, atualizados, ignorados);
    }

    public boolean isFeriado(LocalDate data) {
        return calendarioSlaHelper.isFeriado(data);
    }

    public boolean isDiaNaoUtil(LocalDate data) {
        return calendarioSlaHelper.isDiaNaoUtil(data);
    }

    private Feriado buscarEntidade(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Identificador do feriado é obrigatório.");
        }
        return feriadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feriado não encontrado."));
    }

    private void validarDuplicidadeAtiva(LocalDate data, Long idIgnorar) {
        boolean duplicado = idIgnorar == null
                ? feriadoRepository.existsByDataAndAtivoTrue(data)
                : feriadoRepository.existsByDataAndAtivoTrueAndIdNot(data, idIgnorar);
        if (duplicado) {
            throw new IllegalArgumentException("Já existe um feriado ativo cadastrado para esta data.");
        }
    }

    private void aplicarDto(Feriado entidade, FeriadoDTO dto, LocalDate data) {
        entidade.setData(data);
        entidade.setDescricao(resolverDescricao(dto.getDescricao()));
        entidade.setTipo(resolverTipo(dto.getTipo()));
        entidade.setEscopo(resolverEscopo(dto.getEscopo()));
        entidade.setAtivo(dto.getAtivo() == null ? Boolean.TRUE : dto.getAtivo());
        if (Boolean.TRUE.equals(entidade.getAtivo())) {
            validarDuplicidadeAtiva(data, entidade.getId());
        }
    }

    private String resolverDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return DESCRICAO_PADRAO;
        }
        return descricao.trim();
    }

    private FeriadoTipo resolverTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return FeriadoTipo.FIXO;
        }
        try {
            return FeriadoTipo.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de feriado inválido. Use FIXO ou MOVEL.");
        }
    }

    private FeriadoEscopo resolverEscopo(String escopo) {
        if (escopo == null || escopo.isBlank()) {
            return FeriadoEscopo.NACIONAL;
        }
        try {
            return FeriadoEscopo.valueOf(escopo.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Escopo inválido. Use NACIONAL, ESTADUAL_SP ou MUNICIPAL_SAO_PAULO.");
        }
    }

    private LocalDate parseData(String dataStr) {
        if (dataStr == null || dataStr.isBlank()) {
            throw new IllegalArgumentException("Informe a data do feriado.");
        }
        try {
            return LocalDate.parse(dataStr.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Data inválida. Use o formato YYYY-MM-DD.");
        }
    }

    private String formatData(LocalDate data) {
        return data == null ? null : data.toString();
    }

    private FeriadoDTO toDto(Feriado entidade) {
        FeriadoDTO dto = new FeriadoDTO();
        dto.setId(entidade.getId());
        dto.setData(formatData(entidade.getData()));
        dto.setDescricao(entidade.getDescricao());
        dto.setTipo(entidade.getTipo().name());
        dto.setEscopo(entidade.getEscopo().name());
        dto.setAtivo(entidade.getAtivo());
        return dto;
    }

    private List<SeedItem> buildSeedSaoPaulo2026() {
        List<SeedItem> lista = new ArrayList<>();
        lista.add(new SeedItem(LocalDate.of(2026, 1, 1), "Confraternização Universal",
                FeriadoTipo.FIXO, FeriadoEscopo.NACIONAL));
        lista.add(new SeedItem(LocalDate.of(2026, 1, 25), "Aniversário da Cidade de São Paulo",
                FeriadoTipo.FIXO, FeriadoEscopo.MUNICIPAL_SAO_PAULO));
        lista.add(new SeedItem(LocalDate.of(2026, 4, 3), "Paixão de Cristo",
                FeriadoTipo.MOVEL, FeriadoEscopo.NACIONAL));
        lista.add(new SeedItem(LocalDate.of(2026, 4, 21), "Tiradentes",
                FeriadoTipo.FIXO, FeriadoEscopo.NACIONAL));
        lista.add(new SeedItem(LocalDate.of(2026, 5, 1), "Dia Mundial do Trabalho",
                FeriadoTipo.FIXO, FeriadoEscopo.NACIONAL));
        lista.add(new SeedItem(LocalDate.of(2026, 6, 4), "Corpus Christi",
                FeriadoTipo.MOVEL, FeriadoEscopo.MUNICIPAL_SAO_PAULO));
        lista.add(new SeedItem(LocalDate.of(2026, 7, 9), "Data Magna do Estado de São Paulo",
                FeriadoTipo.FIXO, FeriadoEscopo.ESTADUAL_SP));
        lista.add(new SeedItem(LocalDate.of(2026, 9, 7), "Independência do Brasil",
                FeriadoTipo.FIXO, FeriadoEscopo.NACIONAL));
        lista.add(new SeedItem(LocalDate.of(2026, 10, 12), "Nossa Senhora Aparecida",
                FeriadoTipo.FIXO, FeriadoEscopo.NACIONAL));
        lista.add(new SeedItem(LocalDate.of(2026, 11, 2), "Finados",
                FeriadoTipo.FIXO, FeriadoEscopo.NACIONAL));
        lista.add(new SeedItem(LocalDate.of(2026, 11, 15), "Proclamação da República",
                FeriadoTipo.FIXO, FeriadoEscopo.NACIONAL));
        lista.add(new SeedItem(LocalDate.of(2026, 11, 20), "Dia Nacional de Zumbi e da Consciência Negra",
                FeriadoTipo.FIXO, FeriadoEscopo.NACIONAL));
        lista.add(new SeedItem(LocalDate.of(2026, 12, 25), "Natal",
                FeriadoTipo.FIXO, FeriadoEscopo.NACIONAL));
        return lista;
    }

    private record SeedItem(LocalDate data, String descricao, FeriadoTipo tipo, FeriadoEscopo escopo) {
    }
}

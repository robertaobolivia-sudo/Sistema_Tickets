package com.suporte.tickets.service;

import com.suporte.tickets.dto.AuditoriaEventoFiltroDTO;
import com.suporte.tickets.dto.AuditoriaEventoPageDTO;
import com.suporte.tickets.dto.AuditoriaEventoResponseDTO;
import com.suporte.tickets.entity.AuditoriaEvento;
import com.suporte.tickets.repository.AuditoriaEventoRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaConsultaService {

    public static final int LIMITE_PADRAO = 50;
    public static final int LIMITE_MAXIMO = 200;
    public static final int LIMITE_EXPORTACAO_CSV = 5000;

    private final AuditoriaEventoRepository auditoriaEventoRepository;

    public AuditoriaEventoPageDTO listar(AuditoriaEventoFiltroDTO filtro, int pagina, int limite) {
        int limiteSeguro = normalizarLimite(limite);
        int paginaSegura = Math.max(0, pagina);

        Specification<AuditoriaEvento> spec = montarSpecification(filtro);
        PageRequest pageable = PageRequest.of(
                paginaSegura,
                limiteSeguro,
                Sort.by(Sort.Direction.DESC, "dataHora"));

        Page<AuditoriaEvento> page = auditoriaEventoRepository.findAll(spec, pageable);

        AuditoriaEventoPageDTO resultado = new AuditoriaEventoPageDTO();
        resultado.setPagina(paginaSegura);
        resultado.setLimite(limiteSeguro);
        resultado.setTotal(page.getTotalElements());
        resultado.setItens(page.getContent().stream().map(this::converter).toList());
        return resultado;
    }

    public List<AuditoriaEventoResponseDTO> listarParaExportacao(AuditoriaEventoFiltroDTO filtro) {
        Specification<AuditoriaEvento> spec = montarSpecification(filtro);
        PageRequest pageable = PageRequest.of(
                0,
                LIMITE_EXPORTACAO_CSV,
                Sort.by(Sort.Direction.DESC, "dataHora"));
        return auditoriaEventoRepository.findAll(spec, pageable).getContent().stream()
                .map(this::converter)
                .toList();
    }

    static int normalizarLimite(int limite) {
        if (limite <= 0) {
            return LIMITE_PADRAO;
        }
        return Math.min(limite, LIMITE_MAXIMO);
    }

    private Specification<AuditoriaEvento> montarSpecification(AuditoriaEventoFiltroDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (filtro != null) {
                if (filtro.getDataInicio() != null) {
                    predicados.add(cb.greaterThanOrEqualTo(root.get("dataHora"), filtro.getDataInicio()));
                }
                if (filtro.getDataFim() != null) {
                    predicados.add(cb.lessThanOrEqualTo(root.get("dataHora"), filtro.getDataFim()));
                }
                if (filtro.getAnalistaId() != null) {
                    predicados.add(cb.equal(root.get("analistaId"), filtro.getAnalistaId()));
                }
                if (StringUtils.hasText(filtro.getAcao())) {
                    predicados.add(cb.equal(root.get("acao"), filtro.getAcao().trim()));
                }
                if (StringUtils.hasText(filtro.getEntidade())) {
                    predicados.add(cb.equal(root.get("entidade"), filtro.getEntidade().trim()));
                }
                if (StringUtils.hasText(filtro.getEntidadeId())) {
                    predicados.add(cb.equal(root.get("entidadeId"), filtro.getEntidadeId().trim()));
                }
            }
            return predicados.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    private AuditoriaEventoResponseDTO converter(AuditoriaEvento evento) {
        AuditoriaEventoResponseDTO dto = new AuditoriaEventoResponseDTO();
        dto.setId(evento.getId());
        dto.setDataHora(evento.getDataHora());
        dto.setAnalistaId(evento.getAnalistaId());
        dto.setAnalistaNome(evento.getAnalistaNome());
        dto.setPerfilAcesso(evento.getPerfilAcesso());
        dto.setAcao(evento.getAcao());
        dto.setEntidade(evento.getEntidade());
        dto.setEntidadeId(evento.getEntidadeId());
        dto.setDescricao(evento.getDescricao());
        dto.setIpOrigem(evento.getIpOrigem());
        dto.setUserAgent(evento.getUserAgent());
        return dto;
    }
}

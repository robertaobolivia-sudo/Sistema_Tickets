package com.suporte.tickets.service;

import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.ContatoEtiqueta;
import com.suporte.tickets.entity.Etiqueta;
import com.suporte.tickets.repository.ContatoEtiquetaRepository;
import com.suporte.tickets.repository.ContatoRepository;
import com.suporte.tickets.repository.EtiquetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Sprint F32: backfill idempotente para {@code contato_etiquetas} a partir da tabela legada por ticket (removida na F34).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContatoEtiquetaLegadoBackfillService {

    private static final String TABELA_LEGADO_ETIQUETAS_TICKET = "ticket_etiquetas";

    private final JdbcTemplate jdbcTemplate;
    private final ContatoRepository contatoRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final ContatoEtiquetaRepository contatoEtiquetaRepository;

    @Transactional
    public Resultado executar() {
        if (!tabelaLegadoEtiquetasTicketExiste()) {
            return new Resultado(0, 0, 0, 0);
        }
        List<Map<String, Object>> linhas = jdbcTemplate.queryForList("""
                SELECT t.contato_id AS contatoId, te.etiqueta_id AS etiquetaId
                FROM ticket_etiquetas te
                INNER JOIN tickets t ON t.id = te.ticket_id
                WHERE t.contato_id IS NOT NULL
                """);
        int inseridos = 0;
        int jaExistiam = 0;
        int ignorados = 0;
        for (Map<String, Object> row : linhas) {
            Object contatoRaw = row.get("contatoId");
            Object etiquetaRaw = row.get("etiquetaId");
            if (contatoRaw == null || etiquetaRaw == null) {
                ignorados++;
                continue;
            }
            Integer contatoId = ((Number) contatoRaw).intValue();
            Long etiquetaId = ((Number) etiquetaRaw).longValue();
            if (contatoEtiquetaRepository.existsByContato_IdAndEtiqueta_Id(contatoId, etiquetaId)) {
                jaExistiam++;
                continue;
            }
            Contato contato = contatoRepository.findById(contatoId).orElse(null);
            Etiqueta etiqueta = etiquetaRepository.findById(etiquetaId).orElse(null);
            if (contato == null || etiqueta == null) {
                ignorados++;
                continue;
            }
            ContatoEtiqueta vinculo = new ContatoEtiqueta();
            vinculo.setContato(contato);
            vinculo.setEtiqueta(etiqueta);
            contatoEtiquetaRepository.save(vinculo);
            inseridos++;
        }
        return new Resultado(linhas.size(), inseridos, jaExistiam, ignorados);
    }

    private boolean tabelaLegadoEtiquetasTicketExiste() {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.TABLES
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                        """,
                Integer.class,
                TABELA_LEGADO_ETIQUETAS_TICKET);
        return count != null && count > 0;
    }

    public record Resultado(int candidatos, int inseridos, int jaExistiam, int ignorados) {
    }
}

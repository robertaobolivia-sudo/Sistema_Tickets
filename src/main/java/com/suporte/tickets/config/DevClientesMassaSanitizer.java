package com.suporte.tickets.config;

import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.ContatoRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Sanitiza massa DEV de contratantes: 4 LTDA oficiais, sem duplicados conexão/E2E.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevClientesMassaSanitizer {

    private final JdbcTemplate jdbcTemplate;
    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public MassaSanitizeResult executar(String origemLog) {
        log.warn("{}: sanitizacao de clientes DEV iniciada", origemLog);
        List<Cliente> todos = clienteRepository.findAllByOrderByDataCadastroDesc();
        Map<String, Cliente> oficialPorRazao = new HashMap<>();
        List<Cliente> legados = new ArrayList<>();

        for (Cliente c : todos) {
            String razao = normalizarRazao(c.getRazaoSocial());
            if (razao != null && MassaOficialClientesDevConstants.RAZOES_SOCIAIS_OFICIAIS.contains(razao)) {
                oficialPorRazao.merge(razao, c, (a, b) -> a.getId() <= b.getId() ? a : b);
            }
        }

        Set<Integer> idsOficiaisCanon =
                new HashSet<>(oficialPorRazao.values().stream().map(Cliente::getId).toList());

        for (Cliente c : todos) {
            String razao = normalizarRazao(c.getRazaoSocial());
            if (razao != null
                    && MassaOficialClientesDevConstants.RAZOES_SOCIAIS_OFICIAIS.contains(razao)
                    && !idsOficiaisCanon.contains(c.getId())) {
                legados.add(c);
            }
        }

        for (Cliente c : todos) {
            if (idsOficiaisCanon.contains(c.getId()) || legados.contains(c)) {
                continue;
            }
            String alvo = MassaOficialClientesDevConstants.resolverRazaoOficialPorLegado(
                    new MassaOficialClientesDevConstants.ClienteLegadoRef(
                            c.getNome(), c.getRazaoSocial(), c.getEmpresa()));
            if (alvo != null && oficialPorRazao.containsKey(alvo)) {
                legados.add(c);
            }
        }

        for (Cliente legado : legados) {
            String razaoAlvo = MassaOficialClientesDevConstants.resolverRazaoOficialPorLegado(
                    new MassaOficialClientesDevConstants.ClienteLegadoRef(
                            legado.getNome(), legado.getRazaoSocial(), legado.getEmpresa()));
            Cliente oficial = oficialPorRazao.get(razaoAlvo);
            if (oficial == null) {
                continue;
            }
            consolidarLegadoNoOficial(legado.getId(), oficial.getId(), origemLog);
        }

        realinharTicketsPorMensagemS253(oficialPorRazao);
        removerTicketsE2eResiduais(origemLog);
        removerClientesE2eRuido(idsOficiaisCanon, origemLog);
        removerClientesNaoOficiaisRemanescentes(idsOficiaisCanon, oficialPorRazao, origemLog);

        long clientesFinais = clienteRepository.count();
        long ticketsFinais = ticketRepository.count();
        long contatosFinais = contatoRepository.count();
        log.warn(
                "{}: sanitizacao concluida — clientes={}, tickets={}, contatos={} (esperado 4/12/12)",
                origemLog,
                clientesFinais,
                ticketsFinais,
                contatosFinais);
        return new MassaSanitizeResult(clientesFinais, ticketsFinais, contatosFinais);
    }

    public record MassaSanitizeResult(long clientes, long tickets, long contatos) {
    }

    private void removerClientesNaoOficiaisRemanescentes(
            Set<Integer> idsOficiaisCanon, Map<String, Cliente> oficialPorRazao, String origemLog) {
        for (Cliente c : clienteRepository.findAll()) {
            if (idsOficiaisCanon.contains(c.getId())) {
                continue;
            }
            String alvo = MassaOficialClientesDevConstants.resolverRazaoOficialPorLegado(
                    new MassaOficialClientesDevConstants.ClienteLegadoRef(
                            c.getNome(), c.getRazaoSocial(), c.getEmpresa()));
            Cliente oficial = alvo != null ? oficialPorRazao.get(alvo) : null;
            if (oficial != null) {
                consolidarLegadoNoOficial(c.getId(), oficial.getId(), origemLog);
            } else {
                purgarClienteDev(c.getId());
                log.info("{}: removido cliente fora da massa oficial id={}", origemLog, c.getId());
            }
        }
    }

    private void consolidarLegadoNoOficial(Integer legadoId, Integer oficialId, String origemLog) {
        if (Objects.equals(legadoId, oficialId)) {
            return;
        }
        jdbcTemplate.update(
                """
                UPDATE tickets t
                INNER JOIN contatos c ON c.id = t.contato_id
                INNER JOIN contatos c2 ON c2.cliente_id = ? AND c2.whatsapp_normalizado = c.whatsapp_normalizado
                SET t.cliente_id = ?, t.contato_id = c2.id
                WHERE t.cliente_id = ?
                """,
                oficialId,
                oficialId,
                legadoId);

        jdbcTemplate.update(
                "UPDATE tickets SET cliente_id = ? WHERE cliente_id = ?",
                oficialId,
                legadoId);

        jdbcTemplate.update(
                "DELETE FROM contato_etiquetas WHERE contato_id IN (SELECT id FROM contatos WHERE cliente_id = ?)",
                legadoId);
        jdbcTemplate.update("DELETE FROM contatos WHERE cliente_id = ?", legadoId);
        jdbcTemplate.update("DELETE FROM whatsapp_matriz WHERE cliente_id = ?", legadoId);
        clienteRepository.deleteById(legadoId);
        log.info("{}: removido cliente legado id={} → oficial id={}", origemLog, legadoId, oficialId);
    }

    private void realinharTicketsPorMensagemS253(Map<String, Cliente> oficialPorRazao) {
        for (var entry : MassaOficialClientesDevConstants.RAZOES_SOCIAIS_OFICIAIS.stream()
                .map(razao -> Map.entry(razao, oficialPorRazao.get(razao)))
                .filter(e -> e.getValue() != null)
                .toList()) {
            String razao = entry.getKey();
            Integer oficialId = entry.getValue().getId();
            jdbcTemplate.update(
                    """
                    UPDATE tickets t
                    INNER JOIN contatos c ON c.id = t.contato_id
                    INNER JOIN contatos c2 ON c2.cliente_id = ? AND c2.whatsapp_normalizado = c.whatsapp_normalizado
                    SET t.cliente_id = ?, t.contato_id = c2.id
                    WHERE t.mensagem_inicial LIKE ?
                    """,
                    oficialId,
                    oficialId,
                    "S253-" + razao + "%");
        }
    }

    private void removerTicketsE2eResiduais(String origemLog) {
        List<Integer> ticketIds = jdbcTemplate.queryForList(
                """
                SELECT id FROM tickets
                WHERE mensagem_inicial LIKE '%E2E%'
                   OR mensagem_inicial LIKE 'Abertura E2E%'
                """,
                Integer.class);
        for (Integer ticketId : ticketIds) {
            excluirTicketComDependencias(ticketId);
            log.info("{}: removido ticket E2E id={}", origemLog, ticketId);
        }
    }

    private void removerClientesE2eRuido(Set<Integer> idsOficiaisCanon, String origemLog) {
        for (Cliente c : clienteRepository.findAll()) {
            if (idsOficiaisCanon.contains(c.getId())) {
                continue;
            }
            if (!isClienteE2eRuido(c)) {
                continue;
            }
            purgarClienteDev(c.getId());
            log.info("{}: removido cliente E2E/ruido id={}", origemLog, c.getId());
        }
    }

    private static boolean isClienteE2eRuido(Cliente c) {
        String nome = c.getNome() == null ? "" : c.getNome().toLowerCase();
        return nome.contains("e2e") || nome.contains("playwright");
    }

    private void purgarClienteDev(Integer clienteId) {
        List<Integer> ticketIds = jdbcTemplate.queryForList(
                "SELECT id FROM tickets WHERE cliente_id = ?", Integer.class, clienteId);
        for (Integer ticketId : ticketIds) {
            excluirTicketComDependencias(ticketId);
        }
        jdbcTemplate.update(
                "DELETE FROM contato_etiquetas WHERE contato_id IN (SELECT id FROM contatos WHERE cliente_id = ?)",
                clienteId);
        jdbcTemplate.update("DELETE FROM contatos WHERE cliente_id = ?", clienteId);
        jdbcTemplate.update("DELETE FROM whatsapp_matriz WHERE cliente_id = ?", clienteId);
        clienteRepository.deleteById(clienteId);
    }

    private void excluirTicketComDependencias(Integer ticketId) {
        jdbcTemplate.update("DELETE FROM ticket_interacoes WHERE ticket_id = ?", ticketId);
        jdbcTemplate.update("DELETE FROM ticket_anexos WHERE ticket_id = ?", ticketId);
        jdbcTemplate.update("DELETE FROM ticket_satisfacao WHERE ticket_id = ?", ticketId);
        jdbcTemplate.update(
                "DELETE FROM interacao_pendente_decisao WHERE ticket_anterior_id = ? OR ticket_gerado_id = ?",
                ticketId,
                ticketId);
        jdbcTemplate.update("DELETE FROM tickets WHERE id = ?", ticketId);
    }

    private static String normalizarRazao(String razao) {
        return razao == null || razao.isBlank() ? null : razao.trim();
    }
}

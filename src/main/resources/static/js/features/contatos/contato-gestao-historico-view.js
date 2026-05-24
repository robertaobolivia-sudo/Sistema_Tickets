/**
 * Histórico de tickets por contato (Clientes → Contatos, Sprint 262).
 */

import { escapeContatoGestaoHtml } from '@features/clientes/cliente-contatos-gestao-view.js';

const COLUNAS_TABELA = 10;

export function getHistoricoColspan() {
    return COLUNAS_TABELA;
}

export function formatHistoricoData(iso) {
    if (!iso) {
        return '—';
    }
    try {
        const d = new Date(iso);
        if (Number.isNaN(d.getTime())) {
            return '—';
        }
        return d.toLocaleString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch {
        return '—';
    }
}

export function formatHistoricoStatus(status) {
    if (!status) {
        return '—';
    }
    return String(status).replace(/_/g, ' ');
}

export function formatHistoricoAtendimentoOrigem(item) {
    const tel = item?.atendimentoTelefone;
    const tipo = item?.atendimentoTelefoneTipo;
    if (!tel && !tipo) {
        return '—';
    }
    const tipoLabel =
        tipo === 'PRINCIPAL' ? 'Principal' : tipo === 'ADICIONAL' ? 'Adicional' : '';
    if (tel && tipoLabel) {
        return `${tel} (${tipoLabel})`;
    }
    return tel || tipoLabel || '—';
}

export function formatHistoricoAvaliacao(item) {
    const status = item?.satisfacaoStatus;
    const nota = item?.satisfacaoNota;
    if (!status && (nota == null || nota === '')) {
        return '—';
    }
    const partes = [];
    if (status) {
        partes.push(formatHistoricoStatus(status));
    }
    if (nota != null && nota !== '') {
        partes.push(`nota ${nota}`);
    }
    return partes.join(' · ');
}

export function buildHistoricoPanelHtml(
    contatoNome,
    tickets,
    { loading = false, erro = null, telefonesHtml = '' } = {}
) {
    const titulo = escapeContatoGestaoHtml(contatoNome || 'Contato');
    const telefonesBlock = telefonesHtml ? String(telefonesHtml) : '';
    if (loading) {
        return `<div class="contato-historico-panel"><p class="contato-historico-title">Histórico — ${titulo}</p>${telefonesBlock}<p class="empty-state">Carregando chamados…</p></div>`;
    }
    if (erro) {
        return `<div class="contato-historico-panel"><p class="contato-historico-title">Histórico — ${titulo}</p>${telefonesBlock}<p class="alert-error">${escapeContatoGestaoHtml(erro)}</p></div>`;
    }
    const lista = Array.isArray(tickets) ? tickets : [];
    if (!lista.length) {
        return `<div class="contato-historico-panel"><p class="contato-historico-title">Histórico — ${titulo}</p>${telefonesBlock}<p class="empty-state">Nenhum chamado vinculado a este contato.</p></div>`;
    }
    const rows = lista
        .map(
            t => `
        <tr>
            <td>${escapeContatoGestaoHtml(t.protocolo || '—')}</td>
            <td>${escapeContatoGestaoHtml(formatHistoricoData(t.dataAbertura))}</td>
            <td>${escapeContatoGestaoHtml(t.categoria || '—')}</td>
            <td>${escapeContatoGestaoHtml(t.subcategoria || '—')}</td>
            <td>${escapeContatoGestaoHtml(t.motivo || '—')}</td>
            <td>${escapeContatoGestaoHtml(formatHistoricoStatus(t.status))}</td>
            <td>${escapeContatoGestaoHtml(formatHistoricoData(t.dataEncerramento))}</td>
            <td class="contato-historico-telefone-origem">${escapeContatoGestaoHtml(formatHistoricoAtendimentoOrigem(t))}</td>
            <td>${escapeContatoGestaoHtml(formatHistoricoAvaliacao(t))}</td>
            <td class="contato-historico-acoes">
                <button type="button" class="btn-secondary btn-sm" data-contato-ver-conversa
                    data-protocolo="${escapeContatoGestaoHtml(t.protocolo || '')}"
                    data-status="${escapeContatoGestaoHtml(t.status || '')}">Ver conversa</button>
            </td>
        </tr>`
        )
        .join('');
    return `
        <div class="contato-historico-panel">
            <p class="contato-historico-title">Histórico — ${titulo}</p>
            ${telefonesBlock}
            <div class="contato-historico-table-wrap">
                <table class="clientes-data-table clientes-data-table--historico">
                    <thead>
                        <tr>
                            <th scope="col">Protocolo</th>
                            <th scope="col">Abertura</th>
                            <th scope="col">Categoria</th>
                            <th scope="col">Subcategoria</th>
                            <th scope="col">Motivo</th>
                            <th scope="col">Status</th>
                            <th scope="col">Encerramento</th>
                            <th scope="col">Origem do atendimento</th>
                            <th scope="col">Pesquisa</th>
                            <th scope="col">Conversa</th>
                        </tr>
                    </thead>
                    <tbody>${rows}</tbody>
                </table>
            </div>
        </div>`;
}

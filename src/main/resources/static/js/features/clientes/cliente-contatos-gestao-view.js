/**
 * Helpers para Clientes → Contatos e resumo no cadastro (Sprint 254).
 */

export const CLIENTES_CONTATOS_MSG_VAZIO = 'Nenhum contato vinculado a este contratante.';
export const CLIENTES_CONTATOS_MSG_BUSCA = 'Nenhum contato encontrado com os filtros informados.';

/** Sugestões de etiquetas operacionais (cadastro global em Configurações). Sprint 267/271. */
export const CONTATO_ETIQUETAS_OPERACIONAIS_SUGESTAO = [
    'Indevido',
    'Contato Pessoal',
    'Propaganda'
];

export const CONTATO_ETIQUETA_OPERACIONAL_AVISO =
    'Este contato possui etiqueta operacional (Indevido, Contato Pessoal ou Propaganda). ' +
    'Use para qualificar o atendimento; tickets não são invalidados automaticamente nesta fase.';

const OPERACIONAIS_NORMALIZADAS = new Set(
    CONTATO_ETIQUETAS_OPERACIONAIS_SUGESTAO.map(n => normalizarNomeEtiquetaOperacional(n))
);

export function normalizarNomeEtiquetaOperacional(nome) {
    return String(nome ?? '')
        .trim()
        .toLowerCase();
}

export function isNomeEtiquetaOperacional(nome) {
    const n = normalizarNomeEtiquetaOperacional(nome);
    return n.length > 0 && OPERACIONAIS_NORMALIZADAS.has(n);
}

export function parseEtiquetasResumo(resumo) {
    if (!resumo) {
        return [];
    }
    return String(resumo)
        .split(',')
        .map(s => s.trim())
        .filter(Boolean);
}

export function contatoTemEtiquetaOperacional(contato) {
    if (contato?.temEtiquetaOperacional === true) {
        return true;
    }
    return parseEtiquetasResumo(contato?.etiquetasResumo).some(isNomeEtiquetaOperacional);
}

export function formatEtiquetasGestaoCellHtml(contato) {
    const nomes = parseEtiquetasResumo(contato?.etiquetasResumo);
    if (!nomes.length) {
        return escapeContatoGestaoHtml('—');
    }
    const chips = nomes
        .map(nome => {
            const op = isNomeEtiquetaOperacional(nome);
            const cls = op
                ? 'contato-gestao-etiqueta-badge contato-gestao-etiqueta-badge--operacional'
                : 'contato-gestao-etiqueta-badge';
            const title = op ? 'Etiqueta operacional' : '';
            const titleAttr = title ? ` title="${escapeContatoGestaoHtml(title)}"` : '';
            return `<span class="${cls}"${titleAttr}>${escapeContatoGestaoHtml(nome)}</span>`;
        })
        .join('');
    return `<span class="contato-gestao-etiquetas-cell">${chips}</span>`;
}

export function getEtiquetasOperacionaisVinculadas(etiquetas) {
    const lista = Array.isArray(etiquetas) ? etiquetas : [];
    return lista
        .map(e => (e?.nome != null ? String(e.nome) : ''))
        .filter(isNomeEtiquetaOperacional);
}

export function catalogoTemEtiquetasOperacionais(etiquetasAtivas) {
    const nomes = new Set(
        (Array.isArray(etiquetasAtivas) ? etiquetasAtivas : [])
            .map(e => normalizarNomeEtiquetaOperacional(e?.nome))
            .filter(Boolean)
    );
    return CONTATO_ETIQUETAS_OPERACIONAIS_SUGESTAO.every(s =>
        nomes.has(normalizarNomeEtiquetaOperacional(s))
    );
}

export function formatContatoGestaoStatusLabel(ativo) {
    return ativo === false ? 'Inativo' : 'Ativo';
}

export function formatContatoGestaoStatusHtml(ativo) {
    const label = formatContatoGestaoStatusLabel(ativo);
    const cls = ativo === false ? 'contato-gestao-status contato-gestao-status--inativo' : 'contato-gestao-status contato-gestao-status--ativo';
    return `<span class="${cls}">${escapeContatoGestaoHtml(label)}</span>`;
}

export function escapeContatoGestaoHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

export function formatContatoGestaoLocal(contato) {
    const cidade = contato?.cidade ? String(contato.cidade).trim() : '';
    const uf = contato?.uf ? String(contato.uf).trim() : '';
    if (cidade && uf) {
        return `${cidade}/${uf}`;
    }
    return cidade || uf || '—';
}

export function formatContatoGestaoChamados(contato) {
    const ativos = Number(contato?.chamadosAtivos ?? 0);
    const total = Number(contato?.totalChamados ?? 0);
    if (total <= 0) {
        return '—';
    }
    if (ativos > 0) {
        return `${ativos} ativo(s) · ${total} total`;
    }
    return `${total} encerrado(s)`;
}

export function filterContatosGestaoLocal(contatos, busca) {
    const termo = String(busca || '').trim().toLowerCase();
    if (!termo || !Array.isArray(contatos)) {
        return Array.isArray(contatos) ? contatos : [];
    }
    return contatos.filter(c => {
        const blob = [
            c?.nome,
            c?.whatsapp,
            c?.email,
            c?.empresaLocal,
            c?.cidade,
            c?.uf,
            c?.clienteRazaoSocial,
            c?.etiquetasResumo
        ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase();
        return blob.includes(termo);
    });
}

export function getContatosGestaoEmptyMessage(filtrosAtivos, temResultados) {
    if (temResultados) {
        return null;
    }
    return filtrosAtivos ? CLIENTES_CONTATOS_MSG_BUSCA : CLIENTES_CONTATOS_MSG_VAZIO;
}

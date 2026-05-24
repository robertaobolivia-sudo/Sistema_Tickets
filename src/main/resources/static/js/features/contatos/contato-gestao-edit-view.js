/**
 * Formulário Clientes → Contatos (edição básica, Sprint 261).
 */

import {
    CONTATO_ETIQUETA_OPERACIONAL_AVISO,
    getEtiquetasOperacionaisVinculadas,
    isNomeEtiquetaOperacional
} from '@features/clientes/cliente-contatos-gestao-view.js';

export function buildContatoUpdatePayload(form) {
    const nome = String(form?.nome ?? '').trim();
    if (!nome) {
        throw new Error('Informe o nome do contato.');
    }
    const uf = String(form?.uf ?? '').trim();
    if (uf.length > 2) {
        throw new Error('UF deve ter no máximo 2 caracteres.');
    }
    return {
        nome,
        email: trimOrNull(form?.email),
        empresaLocal: trimOrNull(form?.empresaLocal),
        cidade: trimOrNull(form?.cidade),
        uf: uf || null,
        observacoes: trimOrNull(form?.observacoes),
        whatsapp: form?.whatsapp != null ? String(form.whatsapp).trim() : null
    };
}

function trimOrNull(value) {
    const v = value == null ? '' : String(value).trim();
    return v ? v : null;
}

export function renderEtiquetasCheckboxList(container, etiquetas, selectedIds) {
    if (!container) {
        return;
    }
    const selected = new Set((selectedIds || []).map(id => Number(id)));
    container.innerHTML = '';
    const lista = Array.isArray(etiquetas) ? etiquetas : [];
    if (!lista.length) {
        container.textContent = 'Nenhuma etiqueta ativa cadastrada.';
        return;
    }
    lista.forEach(et => {
        const id = Number(et.id);
        const label = document.createElement('label');
        label.className = 'contato-gestao-etiqueta-chip';
        const input = document.createElement('input');
        input.type = 'checkbox';
        input.value = String(id);
        input.dataset.etiquetaId = String(id);
        if (selected.has(id)) {
            input.checked = true;
        }
        const span = document.createElement('span');
        span.textContent = et.nome || `Etiqueta ${id}`;
        if (isNomeEtiquetaOperacional(et.nome)) {
            label.classList.add('contato-gestao-etiqueta-chip--operacional');
        }
        label.appendChild(input);
        label.appendChild(span);
        container.appendChild(label);
    });
}

export function atualizarAvisoEtiquetaOperacional(avisoEl, etiquetasAtivas, etiquetaIdsSelecionados) {
    if (!avisoEl) {
        return;
    }
    const selecionadas = (Array.isArray(etiquetasAtivas) ? etiquetasAtivas : []).filter(e =>
        (etiquetaIdsSelecionados || []).includes(Number(e.id))
    );
    const operacionais = getEtiquetasOperacionaisVinculadas(selecionadas);
    if (!operacionais.length) {
        avisoEl.textContent = '';
        avisoEl.classList.add('hidden');
        return;
    }
    avisoEl.textContent = `${CONTATO_ETIQUETA_OPERACIONAL_AVISO} (${operacionais.join(', ')})`;
    avisoEl.classList.remove('hidden');
}

export function collectEtiquetaIdsFromContainer(container) {
    if (!container) {
        return [];
    }
    return [...container.querySelectorAll('input[type="checkbox"][data-etiqueta-id]:checked')].map(el =>
        Number(el.value)
    );
}

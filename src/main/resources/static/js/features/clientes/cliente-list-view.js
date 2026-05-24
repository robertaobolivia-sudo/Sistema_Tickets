/**
 * Lista e busca de clientes (Sprint 178) — helpers puros.
 */

export const CLIENTES_LISTA_MSG_VAZIO = 'Nenhum contratante cadastrado.';
export const CLIENTES_LISTA_MSG_BUSCA = 'Nenhum contratante encontrado.';

export function escapeClienteListHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

export function formatClienteListContactLine(cliente) {
    const telefone =
        cliente?.telefone && cliente.telefone !== '-' ? String(cliente.telefone).trim() : '';
    const telContato =
        cliente?.telefoneContato && cliente.telefoneContato !== '-'
            ? String(cliente.telefoneContato).trim()
            : '';
    const email =
        cliente?.email && cliente.email !== '-' ? String(cliente.email).trim() : '';
    const parts = [];
    if (telefone) {
        parts.push(telefone);
    } else if (telContato) {
        parts.push(telContato);
    }
    if (email) {
        parts.push(email);
    }
    return parts.length ? parts.join(' · ') : '';
}

export function formatClienteListLocationLine(cliente) {
    const cidade = cliente?.cidade && cliente.cidade !== '-' ? String(cliente.cidade).trim() : '';
    const uf = cliente?.uf && cliente.uf !== '-' ? String(cliente.uf).trim() : '';
    if (cidade && uf) {
        return `${cidade}/${uf}`;
    }
    return cidade || uf || '';
}

export function getClientesListaEmptyMessage(termoBusca, temResultados) {
    if (temResultados) {
        return null;
    }
    const termo = termoBusca == null ? '' : String(termoBusca).trim();
    return termo ? CLIENTES_LISTA_MSG_BUSCA : CLIENTES_LISTA_MSG_VAZIO;
}

/** Filtro local opcional (nome, empresa, telefones, e-mail, cidade, UF, CNPJ). */
export function filterClientesByTermo(clientes, termo) {
    const t = termo == null ? '' : String(termo).trim().toLowerCase();
    if (!t || !Array.isArray(clientes)) {
        return Array.isArray(clientes) ? clientes : [];
    }
    return clientes.filter(c => {
        const blob = [
            c?.nome,
            c?.responsavel,
            c?.razaoSocial,
            c?.empresa,
            c?.cnpj,
            c?.telefone,
            c?.whatsapp,
            c?.telefoneContato,
            c?.email,
            c?.cidade,
            c?.uf
        ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase();
        return blob.includes(t);
    });
}

/**
 * Sprint 282 — Operação por Cliente B2B (Dashboard).
 */

export function formatChamadoStatusLabel(status) {
    if (!status) return '—';
    if (status === 'PENDENCIA_DECISAO') return 'Pendência pós-encerramento';
    if (status === 'AGUARDANDO_CLIENTE') return 'Aguardando cliente';
    if (status === 'EM_ATENDIMENTO') return 'Em atendimento';
    if (status === 'ABERTO') return 'Aberto';
    return String(status);
}

export function buildAcompanharPrepMessage(clienteNome) {
    const nome = clienteNome && String(clienteNome).trim() ? String(clienteNome).trim() : 'Cliente';
    return `Acompanhamento de ${nome} será habilitado na próxima sprint (somente leitura).`;
}

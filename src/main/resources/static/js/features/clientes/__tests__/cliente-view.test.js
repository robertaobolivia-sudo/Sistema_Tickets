import { describe, expect, it } from 'vitest';
import {
    normalizeClassificacaoCliente,
    formatEtiquetaClienteLabel,
    formatClassificacaoClienteLabel,
    renderClassificacaoClienteBadge,
    ETIQUETA_CLIENTE_PADRAO
} from '@features/clientes/cliente-view.js';

describe('clienteView etiquetas', () => {
    it('normaliza legado N1/N2 para sem etiqueta', () => {
        expect(normalizeClassificacaoCliente('N1')).toBe(ETIQUETA_CLIENTE_PADRAO);
        expect(normalizeClassificacaoCliente('n2')).toBe(ETIQUETA_CLIENTE_PADRAO);
        expect(normalizeClassificacaoCliente(null)).toBe(ETIQUETA_CLIENTE_PADRAO);
    });

    it('rótulo neutro sem N1/N2', () => {
        expect(formatEtiquetaClienteLabel()).toBe('Sem etiqueta');
        expect(formatClassificacaoClienteLabel('N1')).toBe('Sem etiqueta');
    });

    it('badge não exibe N1/N2', () => {
        const html = renderClassificacaoClienteBadge('N1');
        expect(html).not.toContain('N1');
        expect(html).toContain('Sem etiqueta');
        expect(html).toContain('cliente-classificacao-sem');
    });
});

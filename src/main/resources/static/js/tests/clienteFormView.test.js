import { describe, expect, it } from 'vitest';
import { readFileSync } from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import {
    CLIENTE_FORM_MSG_EMPTY,
    CLIENTE_FORM_MSG_ARTE_VAZIO,
    assertClientesPageSemNomenclaturaLegadaVisivel,
    clienteArtePreviewTemImagem,
    getClienteFormHeaderTitle,
    getClienteFormHeaderSubtitle,
    sanitizeClienteArteHeaderPublicUrl
} from '../core/clienteFormView.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const indexHtmlPath = path.resolve(__dirname, '../../index.html');

describe('clienteFormView', () => {
    it('mensagens fixas do formulário', () => {
        expect(CLIENTE_FORM_MSG_EMPTY).toContain('contratante');
        expect(CLIENTE_FORM_MSG_ARTE_VAZIO).toContain('Nenhuma arte');
    });

    it('sanitiza URL pública da arte do cliente', () => {
        expect(sanitizeClienteArteHeaderPublicUrl('/uploads/clientes/header-chats/c1.png')).toContain(
            'clientes/header-chats'
        );
        expect(sanitizeClienteArteHeaderPublicUrl('/uploads/conexoes/header-chats/x.png')).toBeNull();
        expect(sanitizeClienteArteHeaderPublicUrl('../etc/passwd')).toBeNull();
        expect(clienteArtePreviewTemImagem('/uploads/clientes/header-chats/a.webp')).toBe(true);
        expect(clienteArtePreviewTemImagem(null)).toBe(false);
    });

    it('título conforme modo (contratante F5)', () => {
        expect(getClienteFormHeaderTitle('idle')).toBe('Cadastro de cliente contratante');
        expect(getClienteFormHeaderTitle('novo')).toBe('Novo cliente contratante');
        expect(getClienteFormHeaderTitle('edit')).toBe('Editando cliente contratante');
    });

    it('subtítulo só em edição com nome', () => {
        expect(getClienteFormHeaderSubtitle('novo', 'Acme')).toBeNull();
        expect(getClienteFormHeaderSubtitle('edit', '')).toBeNull();
        expect(getClienteFormHeaderSubtitle('edit', '  Loja X  ')).toBe('Loja X');
    });

    it('index.html: página Clientes sem rótulo Carteira/Revenda/Conexão/Subcliente', () => {
        const html = readFileSync(indexHtmlPath, 'utf8');
        const start = html.indexOf('id="page-clientes"');
        const end = html.indexOf('id="page-abrir-ticket"');
        expect(start).toBeGreaterThan(-1);
        const section = html.slice(start, end > start ? end : undefined);
        expect(() => assertClientesPageSemNomenclaturaLegadaVisivel(section)).not.toThrow();
        expect(section).toContain('clienteSecComunicacaoLegend');
        expect(section).not.toContain('id="carteiraCliente"');
    });
});

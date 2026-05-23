import { describe, expect, it } from 'vitest';
import {
    applyContatoSelectionToPayload,
    ABIR_TICKET_CONTATO_MODE_LEGADO_ONLY,
    ABIR_TICKET_CONTATO_MODE_WHATSAPP_PRIMARY,
    CONTATO_SOURCE_SOLICITANTE_LEGADO,
    CONTATO_SOURCE_WHATSAPP,
    filtrarContatosWhatsappAtivos,
    formatContatoWhatsappLabel,
    resolveAbrirTicketContatoUiMode,
    shouldInlineLegadoContatoClienteOptions
} from '../core/abrirTicketPayloadView.js';
import { buildAbrirTicketPayloadFromForm } from '../core/abrirTicketPayloadView.js';

describe('abrirTicketPayloadView', () => {
    it('monta payload com clienteContratanteId e contatoWhatsappId', () => {
        const payload = buildAbrirTicketPayloadFromForm(
            { id: 10, nome: 'Cliente A', telefone: '5511000000000', carteira: 'Carteira X' },
            { canal: 'WHATSAPP', mensagem: 'Oi', prioridade: 'MEDIA', conexao: '' },
            { source: CONTATO_SOURCE_WHATSAPP, id: 200, whatsapp: '5511999000002', nome: 'Contato 2' }
        );
        expect(payload.clienteContratanteId).toBe(10);
        expect(payload.contatoWhatsappId).toBe(200);
        expect(payload.contatoSolicitanteId).toBeUndefined();
        expect(payload.telefone).toBe('5511999000002');
        expect(payload.nomeContato).toBe('Contato 2');
    });

    it('fallback legado envia contatoSolicitanteId sem contatoWhatsappId', () => {
        const payload = buildAbrirTicketPayloadFromForm(
            { id: 10, nome: 'Cliente A', telefone: '5511000000000' },
            { canal: 'EMAIL', mensagem: 'Teste', prioridade: 'BAIXA' },
            { source: CONTATO_SOURCE_SOLICITANTE_LEGADO, id: 5 }
        );
        expect(payload.contatoSolicitanteId).toBe(5);
        expect(payload.contatoWhatsappId).toBeUndefined();
        expect(payload.telefone).toBe('5511000000000');
    });

    it('sem selecao mantem telefone do cliente', () => {
        const payload = buildAbrirTicketPayloadFromForm(
            { id: 1, nome: 'C', telefone: '55' },
            { canal: 'X', mensagem: 'M', prioridade: 'MEDIA' },
            null
        );
        expect(payload.contatoWhatsappId).toBeUndefined();
        expect(payload.contatoSolicitanteId).toBeUndefined();
        expect(payload.telefone).toBe('55');
    });

    it('applyContatoSelection prioriza whatsapp sobre solicitante', () => {
        const base = { clienteContratanteId: 10, contatoSolicitanteId: 99, telefone: '55' };
        const out = applyContatoSelectionToPayload(base, {
            source: CONTATO_SOURCE_WHATSAPP,
            id: 200,
            whatsapp: '5511999000002',
            nome: 'B'
        });
        expect(out.contatoWhatsappId).toBe(200);
        expect(out.contatoSolicitanteId).toBeUndefined();
        expect(out.telefone).toBe('5511999000002');
    });

    it('filtra contatos inativos', () => {
        const lista = filtrarContatosWhatsappAtivos([
            { id: 1, ativo: true },
            { id: 2, ativo: false },
            { id: 3 }
        ]);
        expect(lista.map(c => c.id)).toEqual([1, 3]);
    });

    it('formatContatoWhatsappLabel', () => {
        expect(formatContatoWhatsappLabel({ nome: ' Ana ', whatsapp: '5511' })).toBe('Ana - 5511');
    });

    it('F4: com Contatos reais usa modo whatsapp_primary e nao inline legado', () => {
        expect(resolveAbrirTicketContatoUiMode(2)).toBe(ABIR_TICKET_CONTATO_MODE_WHATSAPP_PRIMARY);
        expect(shouldInlineLegadoContatoClienteOptions(ABIR_TICKET_CONTATO_MODE_WHATSAPP_PRIMARY)).toBe(false);
    });

    it('F4: sem Contatos reais mantem legado inline', () => {
        expect(resolveAbrirTicketContatoUiMode(0)).toBe(ABIR_TICKET_CONTATO_MODE_LEGADO_ONLY);
        expect(shouldInlineLegadoContatoClienteOptions(ABIR_TICKET_CONTATO_MODE_LEGADO_ONLY)).toBe(true);
    });

    it('F4: selecao whatsapp limpa contatoSolicitanteId no payload', () => {
        const payload = buildAbrirTicketPayloadFromForm(
            { id: 10, nome: 'A', telefone: '5511000000000' },
            { canal: 'W', mensagem: 'M', prioridade: 'MEDIA' },
            { source: CONTATO_SOURCE_WHATSAPP, id: 200, whatsapp: '5511999000002', nome: 'C2' }
        );
        expect(payload.contatoWhatsappId).toBe(200);
        expect(payload.contatoSolicitanteId).toBeUndefined();
    });

    it('F4: fallback legado via painel continua compativel', () => {
        const payload = buildAbrirTicketPayloadFromForm(
            { id: 10, nome: 'A', telefone: '5511000000000' },
            { canal: 'W', mensagem: 'M', prioridade: 'MEDIA' },
            { source: CONTATO_SOURCE_SOLICITANTE_LEGADO, id: 7 }
        );
        expect(payload.contatoSolicitanteId).toBe(7);
        expect(payload.contatoWhatsappId).toBeUndefined();
    });
});

import { describe, expect, it } from 'vitest';
import {
    buildAbrirTicketPayloadFromForm,
    filtrarContatosWhatsappAtivos,
    formatContatoWhatsappLabel,
    getAbrirTicketContatoLabel,
    getAbrirTicketSemContatosOrientacao,
    MSG_SEM_CONTATO_ORIENTACAO,
    shouldShowCadastrarContatoLink,
    validateAbrirTicketSubmit
} from '@features/tickets/abrir-ticket-payload-view.js';

describe('abrirTicketPayloadView', () => {
    const cliente = { id: 10, nome: 'Cliente A', telefone: '5511000000000', carteira: 'Carteira X' };
    const form = { canal: 'WHATSAPP', mensagem: 'Oi', prioridade: 'MEDIA' };
    const contato = { id: 200, whatsapp: '5511999000002', nome: 'Contato 2' };

    it('payload manual so envia campos operacionais atuais', () => {
        const payload = buildAbrirTicketPayloadFromForm(cliente, form, contato);
        expect(Object.keys(payload).sort()).toEqual(
            [
                'canal',
                'cliente',
                'clienteContratanteId',
                'contatoWhatsappId',
                'mensagem',
                'nomeContato',
                'prioridade',
                'telefone'
            ].sort()
        );
    });

    it('monta payload com clienteContratanteId e contatoWhatsappId', () => {
        const payload = buildAbrirTicketPayloadFromForm(cliente, form, contato);
        expect(payload.clienteContratanteId).toBe(10);
        expect(payload.contatoWhatsappId).toBe(200);
        expect(payload.contatoSolicitanteId).toBeUndefined();
        expect(payload.telefone).toBe('5511999000002');
        expect(payload.nomeContato).toBe('Contato 2');
    });

    it('F10: payload nao usa telefone do Cliente como pessoa atendida', () => {
        const payload = buildAbrirTicketPayloadFromForm(cliente, form, contato);
        expect(payload.telefone).not.toBe(cliente.telefone);
        expect(payload.telefone).toBe(contato.whatsapp);
    });

    it('F10: payload manual sem contato nao envia telefone nem solicitante', () => {
        const payload = buildAbrirTicketPayloadFromForm(cliente, form, null);
        expect(payload.contatoWhatsappId).toBeUndefined();
        expect(payload.contatoSolicitanteId).toBeUndefined();
        expect(payload.telefone).toBeUndefined();
        expect(payload.nomeContato).toBeUndefined();
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

    it('label e orientacao sem Contatos reais', () => {
        expect(getAbrirTicketContatoLabel(0)).toContain('WhatsApp');
        expect(getAbrirTicketSemContatosOrientacao()).toBe(MSG_SEM_CONTATO_ORIENTACAO);
    });

    it('com Contatos reais label obrigatorio', () => {
        expect(getAbrirTicketContatoLabel(2)).toMatch(/obrigatório/i);
    });

    it('sem Contatos cadastrados bloqueia abertura', () => {
        expect(validateAbrirTicketSubmit(0, null)).toMatch(/Cadastre um Contato/);
    });

    it('F12: orientacao e link quando sem Contato', () => {
        expect(getAbrirTicketSemContatosOrientacao()).toBe(MSG_SEM_CONTATO_ORIENTACAO);
        expect(shouldShowCadastrarContatoLink(0, 10)).toBe(true);
        expect(shouldShowCadastrarContatoLink(2, 10)).toBe(false);
        expect(shouldShowCadastrarContatoLink(0, null)).toBe(false);
    });

    it('exige Contato selecionado quando cliente tem contatos', () => {
        expect(validateAbrirTicketSubmit(2, null)).toMatch(/Selecione o Contato/);
        expect(validateAbrirTicketSubmit(2, 200)).toBeNull();
    });

    it('F11: contrato payload inalterado apos rename DOM', () => {
        const payload = buildAbrirTicketPayloadFromForm(cliente, form, contato);
        expect(payload.contatoWhatsappId).toBe(200);
        expect(payload.contatoSolicitanteId).toBeUndefined();
    });
});

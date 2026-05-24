import { describe, expect, it } from 'vitest';
import {
    appendQueryParam,
    buildAuditoriaFilterParams,
    buildRelatorioBuscaParams,
    buildIndicadoresChamadosParams,
    buildSatisfacaoResumoParams,
    buildTicketBuscaParams,
    formatDateIsoToBr
} from '@shared/ui/query-params.js';

describe('queryParams.js', () => {
    it('não envia parâmetros vazios', () => {
        const params = new URLSearchParams();
        appendQueryParam(params, 'status', '   ');
        appendQueryParam(params, 'cliente', null);
        expect(params.toString()).toBe('');
    });

    it('monta dataInicio/dataFim de auditoria com horário', () => {
        const params = buildAuditoriaFilterParams({
            dataInicio: '2026-01-01',
            dataFim: '2026-01-31',
            analistaId: '5'
        });
        expect(params.get('dataInicio')).toBe('2026-01-01T00:00:00');
        expect(params.get('dataFim')).toBe('2026-01-31T23:59:59');
        expect(params.get('analistaId')).toBe('5');
    });

    it('preserva filtros válidos no relatório', () => {
        const params = buildRelatorioBuscaParams({
            dataInicio: '2026-02-01',
            dataFim: '2026-02-28',
            status: 'ABERTO',
            prioridade: 'ALTA',
            slaPrimeiroAtendimentoStatus: 'VENCIDO',
            escalonado: 'false'
        });
        expect(params.get('dataInicio')).toBe('2026-02-01');
        expect(params.get('status')).toBe('ABERTO');
        expect(params.get('escalonado')).toBe('false');
        expect(params.has('grupo')).toBe(false);
    });

    it('envia origemTicket no relatório quando informada', () => {
        const params = buildRelatorioBuscaParams({ origemTicket: 'RECEPTIVO_WHATSAPP' });
        expect(params.get('origemTicket')).toBe('RECEPTIVO_WHATSAPP');
        const vazio = buildRelatorioBuscaParams({ origemTicket: '' });
        expect(vazio.has('origemTicket')).toBe(false);
    });

    it('monta filtros de motivo e pesquisa no relatório', () => {
        const params = buildRelatorioBuscaParams({
            motivoId: '12',
            statusPesquisa: 'PENDENTE',
            notaAvaliacao: '5',
            envioStatus: 'SIMULADO'
        });
        expect(params.get('motivoId')).toBe('12');
        expect(params.get('statusPesquisa')).toBe('PENDENTE');
        expect(params.get('notaAvaliacao')).toBe('5');
        expect(params.get('envioStatus')).toBe('SIMULADO');
    });

    it('não envia escalonado inválido no relatório', () => {
        const params = buildRelatorioBuscaParams({ escalonado: '' });
        expect(params.has('escalonado')).toBe(false);
    });

    it('monta busca de tickets', () => {
        const qs = buildTicketBuscaParams({
            textoLivre: 'TK-1',
            campoObsoleto: 'ignorado',
            dataInicio: '2026-03-01',
            dataFim: '2026-03-10'
        }).toString();
        expect(qs).toContain('textoLivre=TK-1');
        expect(qs).not.toContain('campoObsoleto');
        expect(qs).toContain('dataInicio=2026-03-01');
        expect(qs).toContain('dataFim=2026-03-10');
    });

    it('monta parâmetros de indicadores chamados (apenas período)', () => {
        const params = buildIndicadoresChamadosParams({
            dataInicio: '2026-01-01',
            dataFim: '2026-01-31'
        });
        expect(params.get('dataInicio')).toBe('2026-01-01');
        expect(params.get('dataFim')).toBe('2026-01-31');
        expect(params.has('classificacaoCliente')).toBe(false);
    });

    it('monta parâmetros de satisfação (resumo)', () => {
        const params = buildSatisfacaoResumoParams({
            dataInicio: '2026-04-01',
            dataFim: '2026-04-30'
        });
        expect(params.get('dataInicio')).toBe('2026-04-01');
        expect(params.get('dataFim')).toBe('2026-04-30');
    });

    it('formata data ISO para exibição brasileira', () => {
        expect(formatDateIsoToBr('2026-05-20')).toBe('20/05/2026');
        expect(formatDateIsoToBr('')).toBe('-');
    });

    it('monta filtros gerenciais de satisfação', () => {
        const params = buildSatisfacaoResumoParams({
            dataInicio: '2026-05-01',
            dataFim: '2026-05-31',
            nota: '4',
            statusTicket: 'RESOLVIDO',
            termoCliente: 'Cliente X'
        });
        expect(params.get('nota')).toBe('4');
        expect(params.get('statusTicket')).toBe('RESOLVIDO');
        expect(params.get('termoCliente')).toBe('Cliente X');
    });

    it('relatório envia clienteId e omite cliente quando há id', () => {
        const comId = buildRelatorioBuscaParams({ clienteId: '66', cliente: 'legado' });
        expect(comId.get('clienteId')).toBe('66');
        expect(comId.has('cliente')).toBe(false);
        const vazio = buildRelatorioBuscaParams({ clienteId: '' });
        expect(vazio.has('clienteId')).toBe(false);
    });

    it('satisfação envia clienteId e não envia termoCliente quando há id', () => {
        const params = buildSatisfacaoResumoParams({
            clienteId: '12',
            termoCliente: 'X'
        });
        expect(params.get('clienteId')).toBe('12');
        expect(params.has('termoCliente')).toBe(false);
    });
});

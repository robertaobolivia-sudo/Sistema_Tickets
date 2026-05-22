import { describe, expect, it } from 'vitest';
import {
    MSG_ERRO,
    extrairTextoErroBackend,
    mensagemErroApi,
    mensagemErroLogin,
    mensagemParaExibirUsuario
} from '../core/messages.js';

describe('messages.js', () => {
    describe('mensagemErroLogin', () => {
        it('retorna mensagem amigável para login inválido (401)', () => {
            const msg = mensagemErroLogin({ status: 401 }, { erro: 'Credenciais inválidas' });
            expect(msg).toBe(MSG_ERRO.LOGIN_INVALIDO);
        });

        it('não expõe erro técnico de header/token no login', () => {
            const msg = mensagemErroLogin(
                { status: 500 },
                { erro: 'header X-Analista-Token ausente' }
            );
            expect(msg).toBe(MSG_ERRO.LOGIN_INVALIDO);
            expect(msg).not.toMatch(/header|token/i);
        });
    });

    describe('mensagemErroApi', () => {
        it('401 vira sessão expirada', () => {
            const msg = mensagemErroApi({ status: 401 }, {}, 'fallback');
            expect(msg).toBe(MSG_ERRO.SESSAO_EXPIRADA);
        });

        it('403 sem admin vira sem permissão', () => {
            const msg = mensagemErroApi({ status: 403 }, { erro: 'Sem permissao' }, 'fallback');
            expect(msg).toBe(MSG_ERRO.SEM_PERMISSAO);
        });

        it('403 administrador vira acesso admin', () => {
            const msg = mensagemErroApi(
                { status: 403 },
                { erro: 'Somente administrador pode executar' },
                'fallback'
            );
            expect(msg).toBe(MSG_ERRO.ACESSO_ADMIN);
        });

        it('não repassa mensagem técnica de token ao usuário', () => {
            const msg = mensagemErroApi(
                { status: 403 },
                { erro: 'token de sessao invalido' },
                'fallback'
            );
            expect(msg).not.toMatch(/token de sessao/i);
            expect(msg).toBe(MSG_ERRO.SESSAO_EXPIRADA);
        });

        it('500 vira operação falhou', () => {
            const msg = mensagemErroApi({ status: 500 }, {}, 'fallback');
            expect(msg).toBe(MSG_ERRO.OPERACAO_FALHOU);
        });
    });

    describe('mensagemParaExibirUsuario', () => {
        it('substitui mensagem técnica por amigável', () => {
            const msg = mensagemParaExibirUsuario('header X-Analista-Id invalido');
            expect(msg).toBe(MSG_ERRO.SESSAO_EXPIRADA);
        });

        it('mantém mensagem de negócio não técnica', () => {
            const msg = mensagemParaExibirUsuario('Cliente inativo');
            expect(msg).toBe('Cliente inativo');
        });
    });

    describe('extrairTextoErroBackend', () => {
        it('lê campo erro, message e mensagem', () => {
            expect(extrairTextoErroBackend({ erro: ' A ' })).toBe('A');
            expect(extrairTextoErroBackend({ message: 'B' })).toBe('B');
            expect(extrairTextoErroBackend({ mensagem: 'C' })).toBe('C');
        });
    });
});

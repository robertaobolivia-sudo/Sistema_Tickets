import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('../core/state.js', () => ({
    getLoggedAnalyst: vi.fn()
}));

import { getLoggedAnalyst } from '../core/state.js';
import {
    canAccessAuditoria,
    canAccessPage,
    canManageConfiguracoes,
    canManageEtiquetas,
    formatPerfilAcessoLabel,
    isAdminPerfil,
    resolvePerfilAcessoCode
} from '../core/permissions.js';

function mockPerfil(perfilAcesso) {
    getLoggedAnalyst.mockReturnValue({ id: 1, perfilAcesso });
}

describe('permissions.js', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('resolvePerfilAcessoCode (fallback)', () => {
        it('usa perfil válido', () => {
            expect(resolvePerfilAcessoCode({ perfilAcesso: 'ADMIN' })).toBe('ADMIN');
            expect(resolvePerfilAcessoCode({ perfilAcesso: 'SUPERVISOR' })).toBe('SUPERVISOR');
        });

        it('fallback ANALISTA para valor inválido ou ausente', () => {
            expect(resolvePerfilAcessoCode({ perfilAcesso: 'ROOT' })).toBe('ANALISTA');
            expect(resolvePerfilAcessoCode({})).toBe('ANALISTA');
            expect(resolvePerfilAcessoCode(null)).toBe('ANALISTA');
        });
    });

    describe('formatPerfilAcessoLabel', () => {
        it('formata rótulos conhecidos', () => {
            expect(formatPerfilAcessoLabel('ADMIN')).toBe('Administrador');
            expect(formatPerfilAcessoLabel('ANALISTA')).toBe('Analista');
        });
    });

    describe('ADMIN', () => {
        beforeEach(() => mockPerfil('ADMIN'));

        it('acessa áreas administrativas', () => {
            expect(isAdminPerfil()).toBe(true);
            expect(canManageConfiguracoes()).toBe(true);
            expect(canAccessAuditoria()).toBe(true);
            expect(canAccessPage('clientes')).toBe(true);
            expect(canAccessPage('configuracoes')).toBe(true);
            expect(canAccessPage('auditoria')).toBe(true);
            expect(canAccessPage('indicadores')).toBe(true);
        });
    });

    describe('ANALISTA', () => {
        beforeEach(() => mockPerfil('ANALISTA'));

        it('não acessa áreas administrativas', () => {
            expect(isAdminPerfil()).toBe(false);
            expect(canManageConfiguracoes()).toBe(false);
            expect(canManageEtiquetas()).toBe(false);
            expect(canAccessAuditoria()).toBe(false);
            expect(canAccessPage('clientes')).toBe(false);
            expect(canAccessPage('configuracoes')).toBe(false);
            expect(canAccessPage('auditoria')).toBe(false);
        });

        it('acessa tickets e abrir ticket', () => {
            expect(canAccessPage('tickets')).toBe(true);
            expect(canAccessPage('abrir-ticket')).toBe(true);
            expect(canAccessPage('dashboard')).toBe(true);
        });
    });

    describe('SUPERVISOR', () => {
        beforeEach(() => mockPerfil('SUPERVISOR'));

        it('acessa Configurações para etiquetas, não área admin completa', () => {
            expect(canAccessPage('configuracoes')).toBe(true);
            expect(canManageEtiquetas()).toBe(true);
            expect(canAccessAuditoria()).toBe(false);
            expect(canManageConfiguracoes()).toBe(false);
        });

        it('não acessa cadastro de clientes (somente ADMIN)', () => {
            expect(canAccessPage('clientes')).toBe(false);
        });

        it('acessa relatórios e atendentes', () => {
            expect(canAccessPage('relatorios')).toBe(true);
            expect(canAccessPage('atendentes')).toBe(true);
            expect(canAccessPage('indicadores')).toBe(true);
        });
    });

    describe('ANALISTA e indicadores', () => {
        beforeEach(() => mockPerfil('ANALISTA'));

        it('não acessa indicadores', () => {
            expect(canAccessPage('indicadores')).toBe(false);
        });
    });
});

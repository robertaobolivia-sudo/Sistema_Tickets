import { test, expect, type Page } from '@playwright/test';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const massa = JSON.parse(
    fs.readFileSync(path.join(__dirname, '..', '.massa.json'), 'utf8')
) as { email: string; senha: string };

const OPERACIONAIS = ['Indevido', 'Contato Pessoal', 'Propaganda'] as const;

async function login(page: Page) {
    await page.goto('/');
    await page.getByTestId('login-email').fill(massa.email);
    await page.getByTestId('login-password').fill(massa.senha);
    await page.getByTestId('login-submit').click();
    await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 20_000 });
}

async function abrirClientesContatos(page: Page) {
    await page.locator('#navClientesToggle').click();
    await page.locator('[data-page="clientes"][data-clientes-mode="contatos"]').click();
    await expect(page.locator('#clientesViewContatos:not(.hidden)')).toBeVisible({ timeout: 15_000 });
    await expect(page.locator('#contatosGestaoTableBody tr').first()).toBeVisible({ timeout: 20_000 });
}

/** Evita combinação acidental (ex.: avaliação ruim + etiqueta) que zera a lista. */
async function resetContatosGestaoFiltrosAvancados(page: Page) {
    const ruim = page.locator('#contatosGestaoFiltroAvaliacaoRuim');
    const abertos = page.locator('#contatosGestaoFiltroTicketsAbertos');
    const semEtiqueta = page.locator('#contatosGestaoFiltroSemEtiqueta');
    if (await ruim.isChecked()) {
        await ruim.uncheck();
    }
    if (await abertos.isChecked()) {
        await abertos.uncheck();
    }
    if (await semEtiqueta.isChecked()) {
        await semEtiqueta.uncheck();
    }
}

async function garantirFiltrosContatosLimpos(page: Page) {
    await page.locator('#contatosGestaoFiltrosAvancados').click();
    await resetContatosGestaoFiltrosAvancados(page);
    const limparResp = page.waitForResponse(
        r => r.url().includes('/api/contatos?') && r.request().method() === 'GET' && r.ok()
    );
    await page.locator('#contatosGestaoLimparFiltrosAvancados').click();
    await limparResp;
}

async function apiHeadersFromStorage(page: Page) {
    return page.evaluate(() => {
        const raw = localStorage.getItem('suporteTicketsAnalista');
        if (!raw) {
            return null;
        }
        try {
            const s = JSON.parse(raw) as { id?: number; authToken?: string };
            if (!s?.id || !s?.authToken) {
                return null;
            }
            return {
                'X-Analista-Id': String(s.id),
                'X-Analista-Token': s.authToken,
                'Content-Type': 'application/json'
            };
        } catch {
            return null;
        }
    });
}

async function idsEtiquetasOperacionais(page: Page) {
    const headers = await apiHeadersFromStorage(page);
    expect(headers).not.toBeNull();
    return page.evaluate(
        async ({ nomes, headers }) => {
            const res = await fetch('/api/etiquetas/ativas', { headers });
            if (!res.ok) {
                return { ok: false, status: res.status, map: {} as Record<string, number> };
            }
            const lista = (await res.json()) as Array<{ id: number; nome: string }>;
            const map: Record<string, number> = {};
            for (const nome of nomes) {
                const found = lista.find(
                    e => String(e.nome || '').trim().toLowerCase() === nome.toLowerCase()
                );
                if (found?.id) {
                    map[nome] = found.id;
                }
            }
            return { ok: true, map, nomes: lista.map(e => e.nome) };
        },
        { nomes: [...OPERACIONAIS], headers }
    );
}

async function etiquetasVinculadasContato(page: Page, contatoId: number) {
    const headers = await apiHeadersFromStorage(page);
    return page.evaluate(
        async ({ id, headers }) => {
            const res = await fetch(`/api/contatos/${id}/etiquetas`, { headers });
            if (!res.ok) {
                return [] as string[];
            }
            const lista = (await res.json()) as Array<{ nome?: string }>;
            return lista.map(e => String(e.nome ?? ''));
        },
        { id: contatoId, headers }
    );
}

async function listarContatosGestao(
    page: Page
): Promise<Array<{ id?: number; nome?: string; whatsapp?: string }>> {
    const headers = await apiHeadersFromStorage(page);
    expect(headers).not.toBeNull();
    return page.evaluate(
        async ({ headers }) => {
            const res = await fetch('/api/contatos?gestao=true', { headers });
            if (!res.ok) {
                return [];
            }
            return (await res.json()) as Array<{ id?: number; nome?: string; whatsapp?: string }>;
        },
        { headers }
    );
}

async function abrirEditarContatoGestao(
    page: Page,
    contato: { id?: number; nome?: string; whatsapp?: string }
) {
    const id = Number(contato.id);
    expect(id).toBeGreaterThan(0);
    await garantirFiltrosContatosLimpos(page);
    const termo = String(contato.whatsapp || contato.nome || id).trim().slice(-12);
    await page.locator('#contatosGestaoBusca').fill(termo);
    const listaResp = page.waitForResponse(
        r => r.url().includes('/api/contatos?') && r.request().method() === 'GET' && r.ok()
    );
    await listaResp;
    const editar = page.locator(`button[data-contato-gestao-acao="editar"][data-id="${id}"]`);
    await expect(editar).toBeVisible({ timeout: 20_000 });
    await editar.click();
}

async function contatosGestaoPorEtiqueta(page: Page, etiquetaId: number) {
    const headers = await apiHeadersFromStorage(page);
    return page.evaluate(
        async ({ etiquetaId, headers }) => {
            const res = await fetch(`/api/contatos?gestao=true&etiquetaId=${etiquetaId}`, {
                headers
            });
            if (!res.ok) {
                return [] as number[];
            }
            const lista = (await res.json()) as Array<{ id?: number }>;
            return lista.map(c => Number(c.id)).filter(Boolean);
        },
        { etiquetaId, headers }
    );
}

async function snapshotTicketsContato(page: Page, contatoId: number) {
    const headers = await apiHeadersFromStorage(page);
    return page.evaluate(
        async ({ id, headers }) => {
            const res = await fetch(`/api/contatos/${id}/historico-tickets`, { headers });
            if (!res.ok) {
                return [];
            }
            const lista = (await res.json()) as Array<{ numeroTicket?: string; status?: string }>;
            return lista.map(t => ({
                numero: t.numeroTicket ?? '',
                status: t.status ?? ''
            }));
        },
        { id: contatoId, headers }
    );
}

async function marcarEtiquetaNoModal(page: Page, nomeEtiqueta: string) {
    const chip = page.locator(
        `#contatoGestaoEditEtiquetas label.contato-gestao-etiqueta-chip:has-text("${nomeEtiqueta}") input[type="checkbox"]`
    );
    await expect(chip).toBeVisible({ timeout: 10_000 });
    if (!(await chip.isChecked())) {
        await chip.check();
    }
    await expect(page.locator('#contatoGestaoEditOperacionalAviso:not(.hidden)')).toBeVisible({
        timeout: 5_000
    });
    await page.locator('#contatoGestaoEditSalvar').click();
    await expect(page.locator('#modalContatoGestaoEdit.ativo')).toBeHidden({ timeout: 15_000 });
}

test.describe('Sprint 272 — smoke etiquetas operacionais em Contatos', () => {
    test('catálogo, marcação, lista, filtro, histórico e sem alterar tickets', async ({ page }) => {
        test.setTimeout(180_000);

        await login(page);
        await abrirClientesContatos(page);
        await page.locator('#contatosGestaoFiltrosAvancados').click();
        await resetContatosGestaoFiltrosAvancados(page);

        const cat = await idsEtiquetasOperacionais(page);
        expect(cat.ok).toBe(true);
        for (const nome of OPERACIONAIS) {
            expect(cat.map[nome], `etiqueta ${nome} no catálogo`).toBeGreaterThan(0);
        }

        await page.locator('#contatosGestaoFiltrosAvancados').click();
        const filtroEtiqueta = page.locator('#contatosGestaoFiltroEtiqueta');
        for (const nome of OPERACIONAIS) {
            await expect(filtroEtiqueta.locator(`option:has-text("${nome}")`)).toBeAttached();
        }

        const gestaoLista = await listarContatosGestao(page);
        expect(gestaoLista.length).toBeGreaterThan(1);
        const contatoA = gestaoLista[0];
        const contatoB = gestaoLista[1];
        const idA = Number(contatoA.id);
        const idB = Number(contatoB.id);
        expect(idA).toBeGreaterThan(0);
        expect(idB).toBeGreaterThan(0);

        const ticketsAntesA = await snapshotTicketsContato(page, idA);
        const ticketsAntesB = await snapshotTicketsContato(page, idB);

        await abrirEditarContatoGestao(page, contatoA);
        await marcarEtiquetaNoModal(page, 'Propaganda');
        await expect
            .poll(async () => (await etiquetasVinculadasContato(page, idA)).join(','))
            .toContain('Propaganda');

        await abrirEditarContatoGestao(page, contatoB);
        await marcarEtiquetaNoModal(page, 'Indevido');
        await expect
            .poll(async () => (await etiquetasVinculadasContato(page, idB)).join(','))
            .toContain('Indevido');

        await garantirFiltrosContatosLimpos(page);
        await page.locator('#contatosGestaoBusca').fill('');
        await page.waitForResponse(
            r => r.url().includes('/api/contatos?') && r.request().method() === 'GET' && r.ok()
        );
        await abrirEditarContatoGestao(page, contatoA);
        await page.locator('#contatoGestaoEditCancelar').click();
        await expect(page.locator('#modalContatoGestaoEdit.ativo')).toBeHidden();
        await garantirFiltrosContatosLimpos(page);
        await page.locator('#contatosGestaoBusca').fill('');
        await page.waitForResponse(
            r => r.url().includes('/api/contatos?') && r.request().method() === 'GET' && r.ok()
        );

        const rowPropaganda = page.locator(
            `#contatosGestaoTableBody tr.contato-gestao-row--operacional:has(button[data-id="${idA}"])`
        );
        await expect(rowPropaganda).toBeVisible({ timeout: 15_000 });
        await expect(rowPropaganda.locator('.contato-gestao-etiqueta-badge--operacional')).toContainText(
            'Propaganda'
        );

        const rowIndevido = page.locator(
            `#contatosGestaoTableBody tr.contato-gestao-row--operacional:has(button[data-id="${idB}"])`
        );
        await expect(rowIndevido).toBeVisible();
        await expect(rowIndevido.locator('.contato-gestao-etiqueta-badge--operacional')).toContainText(
            'Indevido'
        );

        await page.locator('#contatosGestaoFiltrosAvancados').click();
        await resetContatosGestaoFiltrosAvancados(page);
        await filtroEtiqueta.selectOption(String(cat.map.Propaganda));
        const listaFiltradaResp = page.waitForResponse(
            r => r.url().includes('/api/contatos?') && r.request().method() === 'GET' && r.ok()
        );
        await page.locator('#contatosGestaoAplicarFiltros').click();
        await listaFiltradaResp;
        await expect(
            page.locator(`#contatosGestaoTableBody tr:has(button[data-id="${idA}"])`)
        ).toBeVisible({ timeout: 15_000 });
        const limparResp = page.waitForResponse(
            r => r.url().includes('/api/contatos?') && r.request().method() === 'GET' && r.ok()
        );
        await page.locator('#contatosGestaoLimparFiltrosAvancados').click();
        await limparResp;
        await expect(
            page.locator(`button[data-contato-gestao-acao="historico"][data-id="${idA}"]`)
        ).toBeVisible({ timeout: 15_000 });

        await page.locator(`button[data-contato-gestao-acao="editar"][data-id="${idA}"]`).click();
        await expect(page.locator('#contatoGestaoEditOperacionalAviso:not(.hidden)')).toBeVisible();
        await page.locator('#contatoGestaoEditCancelar').click();
        await expect(page.locator('#modalContatoGestaoEdit.ativo')).toBeHidden();

        const histBtn = page.locator(`button[data-contato-gestao-acao="historico"][data-id="${idA}"]`);
        await expect(histBtn).toBeVisible({ timeout: 15_000 });
        const histResp = page.waitForResponse(
            r => r.url().includes(`/api/contatos/${idA}/historico-tickets`) && r.ok()
        );
        await histBtn.click({ force: true });
        await histResp;
        const historicoPanel = page.locator('.contato-historico-panel');
        await expect(historicoPanel).toBeVisible({ timeout: 15_000 });
        await expect(historicoPanel).toContainText(/Histórico/i);

        const verConversa = historicoPanel.locator('button[data-contato-ver-conversa]').first();
        if (await verConversa.count()) {
            await verConversa.click();
            await expect(page.locator('#page-chats.active')).toBeVisible({ timeout: 15_000 });
        }

        const idsFiltrados = await contatosGestaoPorEtiqueta(page, cat.map.Propaganda);
        expect(idsFiltrados).toContain(idA);

        const ticketsDepoisA = await snapshotTicketsContato(page, idA);
        const ticketsDepoisB = await snapshotTicketsContato(page, idB);
        expect(ticketsDepoisA).toEqual(ticketsAntesA);
        expect(ticketsDepoisB).toEqual(ticketsAntesB);
    });
});

import { test, expect, type Page } from '@playwright/test';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const massa = JSON.parse(
    fs.readFileSync(path.join(__dirname, '..', '.massa.json'), 'utf8')
) as { email: string; senha: string };

async function login(page: Page) {
    await page.goto('/');
    await page.getByTestId('login-email').fill(massa.email);
    await page.getByTestId('login-password').fill(massa.senha);
    await page.getByTestId('login-submit').click();
    await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 20_000 });
}

async function assertMainScrollAtTop(page: Page) {
    await expect
        .poll(async () =>
            page.evaluate(() => {
                const main = document.querySelector('.main-content');
                const active = document.querySelector('.main-content > .page.active');
                return (main?.scrollTop ?? 0) + (active?.scrollTop ?? 0);
            })
        )
        .toBe(0);
}

async function assertPageHeaderNearTop(page: Page, pageKey: string) {
    const header = page.locator(`#page-${pageKey}.active .page-header`).first();
    await expect(header).toBeVisible({ timeout: 10_000 });
    const layoutOffset = await page.evaluate(pageKey => {
        const header = document.querySelector(`#page-${pageKey}.active .page-header`);
        const topbar = document.querySelector('.app-topbar');
        if (!header || !topbar) return 9999;
        const h = header.getBoundingClientRect();
        const t = topbar.getBoundingClientRect();
        return h.top - t.bottom;
    }, pageKey);
    expect(layoutOffset).toBeGreaterThan(-48);
    expect(layoutOffset).toBeLessThan(80);
}

async function assertChatsNearTop(page: Page) {
    await assertMainScrollAtTop(page);
    const tabs = page.locator('#page-chats.active .chats-tabs');
    await expect(tabs).toBeVisible({ timeout: 15_000 });
    const layoutOffset = await page.evaluate(() => {
        const tabs = document.querySelector('#page-chats.active .chats-tabs');
        const topbar = document.querySelector('.app-topbar');
        if (!tabs || !topbar) return 9999;
        const r = tabs.getBoundingClientRect();
        const t = topbar.getBoundingClientRect();
        return r.top - t.bottom;
    });
    expect(layoutOffset).toBeGreaterThan(-48);
    expect(layoutOffset).toBeLessThan(80);
}

async function openNavPage(page: Page, dataPage: string) {
    if (dataPage === 'clientes') {
        await page.locator('#navClientesToggle').click();
        await page.locator('[data-page="clientes"][data-clientes-mode="listagem"]').click();
        return;
    }
    if (dataPage === 'indicadores') {
        await page.locator('#navIndicadoresToggle').click();
        await page
            .locator('[data-indicadores-sub="indicadores-visao-geral"]')
            .click();
        return;
    }
    await page.locator(`.sidebar-nav > .nav-item[data-page="${dataPage}"]`).click();
}

async function visitAndAssertTop(page: Page, dataPage: string, useHeader = true) {
    await openNavPage(page, dataPage);
    await assertMainScrollAtTop(page);
    if (dataPage === 'chats') {
        await assertChatsNearTop(page);
        return;
    }
    if (useHeader) {
        await assertPageHeaderNearTop(page, dataPage);
    }
}

test.describe('Sprint 270 — scroll no topo ao navegar', () => {
    test('rotas principais e submenus abrem sem deslocamento', async ({ page }) => {
        test.setTimeout(180_000);
        const consoleErrors: string[] = [];
        page.on('console', msg => {
            if (msg.type() === 'error') consoleErrors.push(msg.text());
        });

        await login(page);
        await visitAndAssertTop(page, 'dashboard');

        const routeCycle = [
            'tickets',
            'chats',
            'clientes',
            'relatorios',
            'atendentes',
            'abrir-ticket',
            'auditoria',
            'configuracoes',
            'dashboard',
            'tickets',
            'chats'
        ];
        for (const route of routeCycle) {
            await visitAndAssertTop(page, route);
        }

        await page.locator('[data-page="clientes"][data-clientes-mode="contatos"]').click();
        await assertMainScrollAtTop(page);

        await page.locator('#navIndicadoresToggle').click();
        await page.locator('[data-indicadores-sub="indicadores-chamados"]').click();
        await assertMainScrollAtTop(page);
        await assertPageHeaderNearTop(page, 'indicadores');

        await page.locator('.topbar-perfil-btn').click();
        await page.locator('[data-user-menu="perfil"]').click();
        await assertMainScrollAtTop(page);
        await assertPageHeaderNearTop(page, 'perfil');

        await page.locator('.topbar-perfil-btn').click();
        await page.locator('[data-user-menu="tema"]').click();
        await visitAndAssertTop(page, 'relatorios');

        await page.locator('.topbar-perfil-btn').click();
        await page.locator('[data-user-menu="tema"]').click();
        await visitAndAssertTop(page, 'chats');
        await expect(page.locator('#chatsLista .chats-list-item').first()).toBeVisible({
            timeout: 15_000
        });

        const critical = consoleErrors.filter(
            t =>
                !/favicon/i.test(t) &&
                !/Failed to load resource.*404/i.test(t) &&
                !/Failed to load resource.*403/i.test(t)
        );
        expect(critical).toEqual([]);
    });
});

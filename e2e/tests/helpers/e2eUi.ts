import { expect, type Page } from '@playwright/test';

export const FORBIDDEN_CHATS_LABELS = ['Carteira', 'Conexão', 'Revenda', 'Subcliente'];
export const LEGADO_JSON_KEYS = ['conexao', 'carteira', 'contatoSolicitanteId', 'contatoCliente'];

export function isCriticalConsoleError(text: string): boolean {
    const t = text.toLowerCase();
    if (t.includes('favicon')) return false;
    if (t.includes('failed to load resource') && (t.includes('404') || t.includes('403'))) return false;
    return true;
}

export async function loginUi(page: Page, email: string, senha: string) {
    await page.goto('/');
    await expect(page.getByTestId('login-email')).toBeVisible();
    await page.getByTestId('login-email').fill(email);
    await page.getByTestId('login-password').fill(senha);
    await page.getByTestId('login-submit').click();
    await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 25_000 });
}

export async function abrirSubmenuClientes(page: Page, mode: 'listagem' | 'contatos') {
    const toggle = page.locator('#navClientesToggle');
    const submenu = page.locator('#navClientesSubmenu');
    const sub = page.locator(`[data-page="clientes"][data-clientes-mode="${mode}"]`);
    if ((await toggle.getAttribute('aria-expanded')) !== 'true') {
        await toggle.click();
    }
    await expect(submenu).not.toHaveClass(/hidden/, { timeout: 5_000 });
    await sub.click();
    await expect(page.locator('#page-clientes')).toHaveClass(/active/, { timeout: 15_000 });
}

export async function abrirRelatoriosDetalhado(page: Page) {
    const relToggle = page.locator('#navRelatoriosToggle');
    if ((await relToggle.getAttribute('aria-expanded')) !== 'true') {
        await relToggle.click();
    }
    await expect(page.locator('#navRelatoriosSubmenu')).not.toHaveClass(/hidden/);
    await page.locator('[data-page="relatorios"][data-relatorios-sub="detalhado"]').click();
    await expect(page.locator('#page-relatorios.active')).toBeVisible({ timeout: 15_000 });
}

export async function gerarRelatorioUi(page: Page, origem: string, clienteId: number) {
    await page.locator('#relatorioFiltroOrigem').selectOption(origem);
    const clienteSelect = page.locator('#relatorioFiltroCliente');
    await expect(clienteSelect.locator(`option[value="${clienteId}"]`)).toHaveCount(1, {
        timeout: 20_000
    });
    await clienteSelect.selectOption(String(clienteId));

    const relResp = page.waitForResponse(
        r =>
            r.url().includes('/api/tickets/busca') &&
            r.url().includes(`origemTicket=${origem}`) &&
            r.request().method() === 'GET' &&
            r.ok()
    );
    await page.evaluate(() => {
        document.getElementById('relatorioGerarBtn')?.click();
    });
    const resp = await relResp;
    return (await resp.json()) as unknown[];
}

export async function abrirTicketNoChats(page: Page, numeroTicket: string) {
    const busca = page.locator('#chatsBusca');
    const card = page.getByTestId(`chats-card-${numeroTicket}`);
    let cardVisible = false;

    for (const tab of ['fila', 'atendendo'] as const) {
        await page.evaluate(t => {
            document.querySelector(`[data-chats-tab="${t}"]`)?.dispatchEvent(
                new MouseEvent('click', { bubbles: true })
            );
        }, tab);
        await busca.fill('');
        await busca.fill(numeroTicket.replace('TK-', ''));
        try {
            await expect(card).toBeVisible({ timeout: 12_000 });
            cardVisible = true;
            await page.waitForTimeout(400);
            await card.click({ force: true, timeout: 10_000 });
            await expect(page.locator('#chatsPanelChamadoDl')).toContainText(numeroTicket, {
                timeout: 20_000
            });
            break;
        } catch {
            /* próxima aba */
        }
    }
    expect(cardVisible, `ticket ${numeroTicket} deve aparecer no Chats`).toBe(true);

    await expect(page.getByTestId('chats-panel-cliente')).toBeVisible({ timeout: 20_000 });
    await expect(page.getByTestId('chats-panel-contato')).toBeVisible({ timeout: 20_000 });
    await expect(page.locator('#chatsPanelBlockEntrada h3')).toHaveText(/Entrada do atendimento/);
    await expect(page.getByTestId('chats-panel-chamado')).toBeVisible();

    const panelText = await page.locator('#page-chats .chats-panel').innerText();
    for (const label of FORBIDDEN_CHATS_LABELS) {
        expect(panelText).not.toMatch(new RegExp(`(^|\\n)${label}(\\n|$)`, 'i'));
    }
}

export function assertChatsNetworkSemLegado(urls: string[]) {
    const bad = urls.filter(
        u => u.includes('/api/carteiras') || u.includes('/uploads/conexoes')
    );
    expect(bad, 'Chats não deve chamar Carteira/conexões uploads').toEqual([]);
}

export function assertTicketJsonSemLegado(ticket: Record<string, unknown>, ctx: string) {
    for (const key of LEGADO_JSON_KEYS) {
        expect(ticket[key], `${ctx} não deve expor ${key}`).toBeFalsy();
    }
}

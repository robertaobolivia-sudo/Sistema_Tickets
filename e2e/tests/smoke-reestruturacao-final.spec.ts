import { test, expect, type Page } from '@playwright/test';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { criarMassaF42, loginApi, type F42Massa } from './helpers/f42Massa.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const baseUrl = process.env.E2E_BASE_URL || 'http://localhost:8080';

const credenciais = JSON.parse(
    fs.readFileSync(path.join(__dirname, '..', '.massa.json'), 'utf8')
) as { email: string; senha: string };

const FORBIDDEN_CHATS_LABELS = ['Carteira', 'Conexão', 'Revenda', 'Subcliente'];

function isCriticalConsoleError(text: string): boolean {
    const t = text.toLowerCase();
    if (t.includes('favicon')) return false;
    if (t.includes('failed to load resource') && (t.includes('404') || t.includes('403'))) return false;
    return true;
}

async function loginUi(page: Page) {
    await page.goto('/');
    await expect(page.getByTestId('login-email')).toBeVisible();
    await page.getByTestId('login-email').fill(credenciais.email);
    await page.getByTestId('login-password').fill(credenciais.senha);
    await page.getByTestId('login-submit').click();
    await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 25_000 });
}

async function abrirSubmenuClientes(page: Page, mode: 'listagem' | 'contatos') {
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

test.describe('F42 — smoke reestruturação (login → ticket → chats → relatórios)', () => {
    let f42: F42Massa;
    let numeroTicket = '';

    test.beforeAll(async () => {
        const { headers } = await loginApi(baseUrl, credenciais.email, credenciais.senha);
        f42 = await criarMassaF42(baseUrl, headers);
        const massaPath = path.join(__dirname, '..', '.massa-f42.json');
        fs.writeFileSync(massaPath, JSON.stringify(f42, null, 2), 'utf8');
    });

    test('fluxo principal pós F41', async ({ page }) => {
        test.setTimeout(240_000);
        const consoleErrors: string[] = [];

        page.on('console', msg => {
            if (msg.type() === 'error' && isCriticalConsoleError(msg.text())) {
                consoleErrors.push(msg.text());
            }
        });

        await loginUi(page);

        await expect(page.locator('#page-dashboard.active, #page-dashboard')).toBeVisible({
            timeout: 10_000
        });
        await expect(page.locator('#page-dashboard .page-header h1')).toBeVisible();

        await abrirSubmenuClientes(page, 'listagem');
        await expect(page.locator('#clienteListaBusca')).toBeVisible();
        const listaBusca = page.locator('#clienteListaBusca');
        const listaResp = page.waitForResponse(
            r => r.url().includes('/api/clientes') && r.request().method() === 'GET' && r.ok()
        );
        await listaBusca.fill(f42.clienteNome.slice(0, 20));
        await listaResp;
        await expect(page.locator('#clientesListaTableBody')).toContainText(f42.clienteNome, {
            timeout: 20_000
        });

        await abrirSubmenuClientes(page, 'contatos');
        await expect(page.locator('#clientesViewContatos:not(.hidden)')).toBeVisible({ timeout: 15_000 });
        await expect(page.locator('#contatosGestaoBusca')).toBeVisible();
        const contResp = page.waitForResponse(
            r => r.url().includes('/api/contatos') && r.request().method() === 'GET' && r.ok()
        );
        await page.locator('#contatosGestaoBusca').fill(f42.contatoNome.slice(0, 18));
        await contResp;
        await expect(page.locator('#contatosGestaoTableBody')).toContainText(f42.contatoNome, {
            timeout: 20_000
        });

        await page.locator('button.nav-item[data-page="abrir-ticket"]').click();
        await expect(page.locator('#page-abrir-ticket.active')).toBeVisible({ timeout: 15_000 });

        const clienteSearchResp = page.waitForResponse(
            r => r.url().includes('/api/clientes') && r.request().method() === 'GET' && r.ok()
        );
        await page.locator('#clienteBusca').fill(f42.clienteNome.slice(0, 24));
        await clienteSearchResp;
        await page.locator('#clienteResultados .cliente-search-item').first().click();
        await expect(page.locator('#clienteSelecionadoBox')).not.toHaveClass(/hidden/);
        await expect(page.locator('#ticketContatoGroup')).not.toHaveClass(/hidden/, { timeout: 15_000 });

        await page.locator('#ticketContatoWhatsapp').selectOption(String(f42.contatoId));
        await page.locator('#canal').fill('WhatsApp');
        await page.locator('#mensagem').fill(`E2E F42 abertura manual ${f42.ts}`);

        const createResp = page.waitForResponse(
            r =>
                r.url().includes('/api/tickets') &&
                r.request().method() === 'POST' &&
                (r.status() === 200 || r.status() === 201)
        );
        await page.locator('#ticketForm button[type="submit"]').click();
        const created = await createResp;
        const ticketBody = (await created.json()) as {
            numeroTicket?: string;
            clienteId?: number;
            contatoId?: number;
            origemTicket?: string;
        };
        expect(ticketBody.numeroTicket).toBeTruthy();
        expect(ticketBody.clienteId).toBe(f42.clienteId);
        expect(ticketBody.contatoId).toBe(f42.contatoId);
        expect(ticketBody.origemTicket).toBe('ATIVO_MANUAL');
        numeroTicket = ticketBody.numeroTicket!;
        await expect(page.locator('#alertBoxTicket')).toContainText(/sucesso/i, { timeout: 10_000 });

        const chatsNetwork: string[] = [];
        const onChatsRequest = (req: { url: () => string }) => {
            chatsNetwork.push(req.url());
        };
        page.on('request', onChatsRequest);

        await page.getByTestId('nav-chats').click();
        await expect(page.locator('#page-chats.active')).toBeVisible({ timeout: 15_000 });
        await page.getByTestId('chats-tab-fila').click();

        await page.locator('#chatsBusca').fill(numeroTicket.replace('TK-', ''));
        const card = page.getByTestId(`chats-card-${numeroTicket}`);
        await expect(card).toBeVisible({ timeout: 25_000 });
        await card.click();

        await expect(page.getByTestId('chats-panel-cliente')).toBeVisible();
        await expect(page.getByTestId('chats-panel-contato')).toBeVisible();
        await expect(page.locator('#chatsPanelBlockEntrada h3')).toHaveText(/Entrada do atendimento/);
        await expect(page.getByTestId('chats-panel-chamado')).toBeVisible();
        await expect(page.locator('#chatsPanelChamadoDl')).toContainText(numeroTicket);

        const panelText = await page.locator('#page-chats .chats-panel').innerText();
        for (const label of FORBIDDEN_CHATS_LABELS) {
            expect(panelText, `painel Chats não deve exibir rótulo legado "${label}"`).not.toMatch(
                new RegExp(`(^|\\n)${label}(\\n|$)`, 'i')
            );
        }

        const carteiraCalls = chatsNetwork.filter(
            u => u.includes('/api/carteiras') || u.includes('/uploads/conexoes')
        );
        expect(carteiraCalls, 'Chats não deve chamar Carteira/conexões uploads').toEqual([]);
        page.off('request', onChatsRequest);

        const relToggle = page.locator('#navRelatoriosToggle');
        if ((await relToggle.getAttribute('aria-expanded')) !== 'true') {
            await relToggle.click();
        }
        await expect(page.locator('#navRelatoriosSubmenu')).not.toHaveClass(/hidden/);
        await page.locator('[data-page="relatorios"][data-relatorios-sub="detalhado"]').click();
        await expect(page.locator('#page-relatorios.active')).toBeVisible({ timeout: 15_000 });
        await expect(page.locator('#relatorioFiltroOrigem')).toBeVisible();
        await page.locator('#relatorioFiltroOrigem').selectOption('ATIVO_MANUAL');

        const relResp = page.waitForResponse(
            r =>
                r.url().includes('/api/tickets/busca') &&
                r.request().method() === 'GET' &&
                r.ok()
        );
        await page.evaluate(() => {
            document.getElementById('relatorioGerarBtn')?.click();
        });
        await relResp;
        await expect(page.locator('#relatorioTicketsBody')).toBeVisible({ timeout: 20_000 });
        await expect(page.locator('#relatorioTotal')).not.toHaveText('', { timeout: 5_000 });

        await page.locator('button.nav-item[data-page="configuracoes"]').click();
        await expect(page.locator('#page-configuracoes.active')).toBeVisible({ timeout: 15_000 });
        await expect(page.locator('#configConexoesRevendaSection')).toBeVisible();
        await expect(page.locator('#configConexoesRevendaSection')).toContainText(/Conexões/i);

        expect(
            consoleErrors.filter(isCriticalConsoleError),
            `console errors: ${consoleErrors.join(' | ')}`
        ).toEqual([]);
    });
});

import { test, expect, type Page } from '@playwright/test';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { criarMassaF43Receptivo, loginApi, type F43Massa } from './helpers/f43Massa.js';

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
    await page.getByTestId('login-email').fill(credenciais.email);
    await page.getByTestId('login-password').fill(credenciais.senha);
    await page.getByTestId('login-submit').click();
    await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 25_000 });
}

test.describe('F43 — smoke RECEPTIVO_WHATSAPP + matriz', () => {
    let f43: F43Massa;

    test.beforeAll(async () => {
        const { headers } = await loginApi(baseUrl, credenciais.email, credenciais.senha);
        f43 = await criarMassaF43Receptivo(baseUrl, headers);
        fs.writeFileSync(
            path.join(__dirname, '..', '.massa-f43.json'),
            JSON.stringify(f43, null, 2),
            'utf8'
        );
    });

    test('integração simulada + Chats', async ({ page }) => {
        test.setTimeout(120_000);
        const consoleErrors: string[] = [];
        page.on('console', msg => {
            if (msg.type() === 'error' && isCriticalConsoleError(msg.text())) {
                consoleErrors.push(msg.text());
            }
        });

        expect(f43.origemTicket).toBe('RECEPTIVO_WHATSAPP');
        expect(f43.contatoId).toBeGreaterThan(0);
        expect(f43.matrizId).toBeGreaterThan(0);

        await loginUi(page);

        const chatsNetwork: string[] = [];
        const onReq = (req: { url: () => string }) => chatsNetwork.push(req.url());
        page.on('request', onReq);

        const ticketsLoad = page.waitForResponse(
            r =>
                r.url().includes('/api/tickets') &&
                r.request().method() === 'GET' &&
                r.ok()
        );
        await page.getByTestId('nav-chats').click();
        await expect(page.locator('#page-chats.active')).toBeVisible({ timeout: 15_000 });
        await ticketsLoad;

        const busca = page.locator('#chatsBusca');
        const cardSelector = `button[data-chats-ticket="${f43.numeroTicket}"]`;
        let cardVisible = false;
        for (const tab of ['fila', 'atendendo'] as const) {
            await page.evaluate(t => {
                document.querySelector(`[data-chats-tab="${t}"]`)?.dispatchEvent(
                    new MouseEvent('click', { bubbles: true })
                );
            }, tab);
            await busca.fill('');
            await busca.fill(f43.numeroTicket.replace('TK-', ''));
            const card = page.locator(cardSelector);
            try {
                await expect(card).toBeVisible({ timeout: 12_000 });
                cardVisible = true;
                await page.waitForTimeout(400);
                await card.click({ force: true, timeout: 10_000 });
                break;
            } catch {
                /* próxima aba */
            }
        }
        expect(cardVisible, `ticket ${f43.numeroTicket} deve aparecer no Chats`).toBe(true);

        await expect(page.getByTestId('chats-panel-cliente')).toBeVisible();
        await expect(page.getByTestId('chats-panel-contato')).toBeVisible();
        await expect(page.locator('#chatsPanelBlockEntrada h3')).toHaveText(/Entrada do atendimento/);
        await expect(page.getByTestId('chats-panel-chamado')).toBeVisible();
        await expect(page.locator('#chatsPanelChamadoDl')).toContainText(f43.numeroTicket);

        const panelText = await page.locator('#page-chats .chats-panel').innerText();
        for (const label of FORBIDDEN_CHATS_LABELS) {
            expect(panelText).not.toMatch(new RegExp(`(^|\\n)${label}(\\n|$)`, 'i'));
        }

        const entradaText = await page.locator('#chatsPanelEntradaDl').innerText();
        expect(entradaText.length).toBeGreaterThan(0);

        const carteiraCalls = chatsNetwork.filter(
            u => u.includes('/api/carteiras') || u.includes('/uploads/conexoes')
        );
        expect(carteiraCalls).toEqual([]);
        page.off('request', onReq);

        expect(consoleErrors.filter(isCriticalConsoleError)).toEqual([]);
    });
});

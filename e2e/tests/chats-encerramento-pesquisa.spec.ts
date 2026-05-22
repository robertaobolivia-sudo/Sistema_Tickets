import { test, expect, type Page } from '@playwright/test';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { openTicketDetailFromTicketsSearch } from './helpers/ticketsUi.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const baseUrl = process.env.E2E_BASE_URL || 'http://localhost:8080';

const massa = JSON.parse(
    fs.readFileSync(path.join(__dirname, '..', '.massa.json'), 'utf8')
) as {
    email: string;
    senha: string;
    numeroTicket: string;
    grupoId: string;
    subgrupoId: string;
    motivoId: string;
};

function normalizePublicLink(href: string): string {
    try {
        const u = new URL(href, baseUrl);
        return `${u.pathname}${u.search}`;
    } catch {
        return href.trim();
    }
}

async function loginAndOpenChats(page: Page) {
    await page.goto('/');
    await expect(page.getByTestId('login-email')).toBeVisible();
    await page.getByTestId('login-email').fill(massa.email);
    await page.getByTestId('login-password').fill(massa.senha);
    await page.getByTestId('login-submit').click();
    await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 20_000 });
    await page.getByTestId('nav-chats').click();
}

async function assertDetailSatisfacao(
    page: Page,
    expected: { status: RegExp | string; envio?: string; linkPath?: string }
) {
    const statusEl = page.getByTestId('detail-satisfacao-status');
    await expect(statusEl).toBeVisible();
    if (expected.status instanceof RegExp) {
        await expect(statusEl).toHaveText(expected.status);
    } else {
        await expect(statusEl).toHaveText(expected.status, { ignoreCase: true });
    }

    if (expected.envio) {
        const envioRow = page.locator('#detailSatisfacaoEnvioRow');
        await expect(envioRow).not.toHaveClass(/hidden/);
        await expect(page.getByTestId('detail-satisfacao-envio')).toHaveText(expected.envio);
    }

    if (expected.linkPath) {
        const linkRow = page.locator('#detailSatisfacaoLinkRow');
        const linkHidden = await linkRow.evaluate(el => el.classList.contains('hidden'));
        if (!linkHidden) {
            const link = page.getByTestId('detail-satisfacao-link');
            await expect(link).toBeVisible();
            const href = await link.getAttribute('href');
            expect(href).toBeTruthy();
            expect(normalizePublicLink(href!)).toBe(expected.linkPath);
        }
    }
}

test.describe('Chats → encerramento com pesquisa → avaliação pública', () => {
    test('fluxo completo E2E + detalhe satisfação', async ({ page }) => {
        test.setTimeout(180_000);
        const consoleErrors: string[] = [];
        page.on('console', msg => {
            if (msg.type() === 'error') {
                consoleErrors.push(msg.text());
            }
        });

        await loginAndOpenChats(page);
        await page.getByTestId('chats-tab-fila').click();

        const busca = page.locator('#chatsBusca');
        await busca.fill(massa.numeroTicket.replace('TK-', ''));
        const card = page.getByTestId(`chats-card-${massa.numeroTicket}`);
        await expect(card).toBeVisible({ timeout: 15_000 });
        await card.click();

        await expect(page.getByTestId('chats-panel-cliente')).toBeVisible();
        await expect(page.getByTestId('chats-primary-action')).toContainText(/Encerrar ticket/i);
        await page.getByTestId('chats-primary-action').click();

        await expect(page.getByTestId('modal-encerramento')).toHaveClass(/ativo/, { timeout: 10_000 });

        await page.getByTestId('encerrar-grupo').selectOption(massa.grupoId);
        await page.getByTestId('encerrar-subgrupo').selectOption(massa.subgrupoId);
        await page.getByTestId('encerrar-motivo').selectOption(massa.motivoId);
        await page.getByTestId('encerrar-comentario').fill('E2E Playwright encerramento com pesquisa');
        await page.locator('label.encerramento-choice--sim').click();

        const encerrarResponse = page.waitForResponse(
            res =>
                res.url().includes(`/api/tickets/${massa.numeroTicket}/encerrar`) &&
                res.request().method() === 'PUT' &&
                res.status() === 200
        );
        await page.getByTestId('encerrar-confirmar').click();
        const response = await encerrarResponse;
        const body = await response.json();
        expect(body.status).toBe('RESOLVIDO');
        expect(body.avaliacaoLinkPublico).toMatch(/page=avaliacao&token=/);

        const publicUrl = body.avaliacaoLinkPublico as string;
        const publicPath = normalizePublicLink(publicUrl);
        expect(publicPath).toMatch(/page=avaliacao&token=/);

        await expect(page.getByTestId('modal-encerramento')).not.toHaveClass(/ativo/);

        await openTicketDetailFromTicketsSearch(page, massa.numeroTicket);
        await assertDetailSatisfacao(page, {
            status: /Pendente/i,
            envio: 'SIMULADO',
            linkPath: publicPath
        });
        await page.locator('#modalDetalhes #fecharModal').click();
        await expect(page.locator('#modalDetalhes')).not.toHaveClass(/ativo/);

        await page.goto(publicUrl);
        await expect(page.getByTestId('avaliacao-publica-screen')).toHaveClass(/screen-active/);
        await page.getByTestId('avaliacao-nota-5').click();
        await page.locator('#avaliacaoPublicaComentario').fill('E2E avaliação cinco estrelas');
        await page.getByTestId('avaliacao-publica-enviar').click();

        const estado = page.getByTestId('avaliacao-publica-estado');
        await expect(estado).toBeVisible();
        await expect(estado).toContainText(/Obrigado|Sua avaliação foi registrada/i);
        await expect(page.locator('#avaliacaoPublicaForm')).toBeHidden();

        const tokenMatch = publicUrl.match(/token=([^&]+)/);
        expect(tokenMatch).toBeTruthy();
        const token = tokenMatch![1];

        const second = await page.request.post(
            `${baseUrl}/api/public/avaliacoes/${token}/responder`,
            { data: { nota: 4, comentario: 'duplicado' } }
        );
        expect(second.status()).toBe(400);

        await page.goto('/');
        await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 15_000 });
        await openTicketDetailFromTicketsSearch(page, massa.numeroTicket);
        await assertDetailSatisfacao(page, {
            status: /Respondida/i
        });
        await expect(page.locator('#detailSatisfacaoNota')).toContainText('5 / 5');

        const critical = consoleErrors.filter(
            t =>
                !t.includes('favicon') &&
                !t.includes('Failed to load resource') &&
                !t.includes('404')
        );
        expect(critical).toEqual([]);
    });
});

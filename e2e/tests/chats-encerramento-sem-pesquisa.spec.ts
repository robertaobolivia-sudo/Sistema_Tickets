import { test, expect, type Page } from '@playwright/test';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { openTicketDetailFromTicketsSearch } from './helpers/ticketsUi.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const massa = JSON.parse(
    fs.readFileSync(path.join(__dirname, '..', '.massa.json'), 'utf8')
) as {
    email: string;
    senha: string;
    numeroTicketSemPesquisa: string;
    grupoId: string;
    subgrupoId: string;
    motivoId: string;
};

const ticket = massa.numeroTicketSemPesquisa;

async function loginAndOpenChats(page: Page) {
    await page.goto('/');
    await expect(page.getByTestId('login-email')).toBeVisible();
    await page.getByTestId('login-email').fill(massa.email);
    await page.getByTestId('login-password').fill(massa.senha);
    await page.getByTestId('login-submit').click();
    await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 20_000 });
    await page.getByTestId('nav-chats').click();
}

test.describe('Chats → encerramento sem pesquisa', () => {
    test('NAO_ENVIADA, sem link público, sem erro 500', async ({ page }) => {
        test.setTimeout(120_000);
        const serverErrors: string[] = [];
        page.on('response', res => {
            if (res.url().includes('/encerrar') && res.status() >= 500) {
                serverErrors.push(`${res.status()} ${res.url()}`);
            }
        });

        await loginAndOpenChats(page);
        await page.getByTestId('chats-tab-fila').click();

        const busca = page.locator('#chatsBusca');
        await busca.fill(ticket.replace('TK-', ''));
        const card = page.getByTestId(`chats-card-${ticket}`);
        await expect(card).toBeVisible({ timeout: 15_000 });
        await card.click();

        await expect(page.getByTestId('chats-primary-action')).toContainText(/Encerrar ticket/i);
        await page.getByTestId('chats-primary-action').click();
        await expect(page.getByTestId('modal-encerramento')).toHaveClass(/ativo/, { timeout: 10_000 });

        await page.getByTestId('encerrar-grupo').selectOption(massa.grupoId);
        await page.getByTestId('encerrar-subgrupo').selectOption(massa.subgrupoId);
        await page.getByTestId('encerrar-motivo').selectOption(massa.motivoId);
        await page.getByTestId('encerrar-comentario').fill('E2E Playwright encerramento sem pesquisa');
        await page.locator('label.encerramento-choice--nao').click();

        const encerrarResponse = page.waitForResponse(
            res =>
                res.url().includes(`/api/tickets/${ticket}/encerrar`) &&
                res.request().method() === 'PUT'
        );
        await page.getByTestId('encerrar-confirmar').click();
        const response = await encerrarResponse;
        expect(response.status(), 'encerrar não deve retornar 5xx').toBe(200);
        expect(serverErrors).toEqual([]);

        const body = await response.json();
        expect(body.status).toBe('RESOLVIDO');
        expect(body.satisfacaoStatus).toBe('NAO_ENVIADA');
        expect(body.avaliacaoLinkPublico ?? '').toBe('');

        await expect(page.getByTestId('modal-encerramento')).not.toHaveClass(/ativo/);

        await openTicketDetailFromTicketsSearch(page, ticket);
        await expect(page.getByTestId('detail-satisfacao-status')).toHaveText(/Não enviada/i);
        await expect(page.locator('#detailSatisfacaoLinkRow')).toHaveClass(/hidden/);
        await expect(page.getByTestId('detail-satisfacao-link')).toBeHidden();

        await page.locator('#modalDetalhes #fecharModal').click();
    });
});

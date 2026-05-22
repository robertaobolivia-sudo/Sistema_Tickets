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
    numeroTicketSemContato: string;
    grupoId: string;
    subgrupoId: string;
    motivoId: string;
};

const ticket = massa.numeroTicketSemContato;

async function loginUi(page: Page) {
    await page.goto('/');
    await expect(page.getByTestId('login-email')).toBeVisible();
    await page.getByTestId('login-email').fill(massa.email);
    await page.getByTestId('login-password').fill(massa.senha);
    await page.getByTestId('login-submit').click();
    await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 20_000 });
}

async function openDetailAndEncerramentoModal(page: Page) {
    await openTicketDetailFromTicketsSearch(page, ticket);
    await page.getByTestId('detail-encerrar-ticket').click();
    await expect(page.getByTestId('modal-encerramento')).toHaveClass(/ativo/, { timeout: 10_000 });
}

test.describe('Encerramento — ticket sem contato', () => {
    test('aviso visível, Sim desabilitado, NAO_ENVIADA sem link', async ({ page }) => {
        test.setTimeout(120_000);
        const serverErrors: string[] = [];
        page.on('response', res => {
            if (res.url().includes('/encerrar') && res.status() >= 500) {
                serverErrors.push(`${res.status()} ${res.url()}`);
            }
        });

        await loginUi(page);
        await openDetailAndEncerramentoModal(page);

        const aviso = page.locator('#encerramentoSemContatoAviso');
        await expect(aviso).toBeVisible();
        await expect(aviso).toContainText(/não possui contato vinculado/i);
        await expect(page.getByTestId('encerrar-pesquisa-nao')).toBeChecked();
        await expect(page.getByTestId('encerrar-pesquisa-sim')).toBeDisabled();
        await expect(page.locator('label.encerramento-choice--sim')).toHaveClass(/is-disabled/);

        await page.getByTestId('encerrar-grupo').selectOption(massa.grupoId);
        await page.getByTestId('encerrar-subgrupo').selectOption(massa.subgrupoId);
        await page.getByTestId('encerrar-motivo').selectOption(massa.motivoId);
        await page.getByTestId('encerrar-comentario').fill('E2E 222 encerramento ticket sem contato');

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
        expect(body.satisfacaoStatus).not.toBe('PENDENTE');
        expect(body.avaliacaoLinkPublico ?? '').toBe('');

        await expect(page.getByTestId('modal-encerramento')).not.toHaveClass(/ativo/);

        await expect(page.getByTestId('detail-satisfacao-status')).toHaveText(/Não enviada/i, {
            timeout: 15_000
        });
        await expect(page.locator('#detailSatisfacaoLinkRow')).toHaveClass(/hidden/);
    });
});

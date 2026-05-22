import { expect, type Page } from '@playwright/test';

/** Abre detalhe do ticket pela busca avançada (aguarda API; evita corrida com load padrão). */
export async function openTicketDetailFromTicketsSearch(page: Page, numeroTicket: string) {
    await page.locator('button.nav-item[data-page="tickets"]').click();
    await expect(page.locator('#page-tickets')).toHaveClass(/active/, { timeout: 15_000 });
    await page.locator('#ticketLimparFiltrosBtn').click();
    await page.locator('#ticketFiltroTexto').fill(numeroTicket);
    const buscaResponse = page.waitForResponse(
        res =>
            res.url().includes('/api/tickets/busca') &&
            res.request().method() === 'GET' &&
            res.status() === 200
    );
    await page.locator('#ticketBuscaBtn').click();
    await buscaResponse;
    const detailsBtn = page.locator(`button[data-action="details"][data-ticket="${numeroTicket}"]`);
    await expect(detailsBtn).toBeVisible({ timeout: 20_000 });
    await detailsBtn.click();
    await expect(page.locator('#modalDetalhes')).toHaveClass(/ativo/, { timeout: 15_000 });
}

import { test, expect, type Page } from '@playwright/test';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import {
    assertCsvSemLegado,
    assertPdfSemConexao,
    baixarPdf,
    buscarRelatorio,
    decodeCsvBody,
    ensureMassaF44,
    exportarCsv,
    type F44Massa
} from './helpers/f44Massa.js';
import type { ApiHeaders } from './helpers/f42Massa.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const baseUrl = process.env.E2E_BASE_URL || 'http://localhost:8080';

const credenciais = JSON.parse(
    fs.readFileSync(path.join(__dirname, '..', '.massa.json'), 'utf8')
) as { email: string; senha: string };

const LEGADO_JSON_KEYS = ['conexao', 'carteira', 'contatoSolicitanteId', 'contatoCliente'];

async function loginUi(page: Page) {
    await page.goto('/');
    await expect(page.getByTestId('login-email')).toBeVisible();
    await page.getByTestId('login-email').fill(credenciais.email);
    await page.getByTestId('login-password').fill(credenciais.senha);
    await page.getByTestId('login-submit').click();
    await expect(page.locator('#appScreen.screen-active')).toBeVisible({ timeout: 25_000 });
}

async function abrirRelatoriosDetalhado(page: Page) {
    const relToggle = page.locator('#navRelatoriosToggle');
    if ((await relToggle.getAttribute('aria-expanded')) !== 'true') {
        await relToggle.click();
    }
    await expect(page.locator('#navRelatoriosSubmenu')).not.toHaveClass(/hidden/);
    await page.locator('[data-page="relatorios"][data-relatorios-sub="detalhado"]').click();
    await expect(page.locator('#page-relatorios.active')).toBeVisible({ timeout: 15_000 });
}

async function gerarRelatorioUi(page: Page, origem: string, clienteId: number) {
    await page.locator('#relatorioFiltroOrigem').selectOption(origem);
    await page.waitForTimeout(300);
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
    const body = (await resp.json()) as unknown[];
    return body;
}

function assertTicketJsonSemLegado(ticket: Record<string, unknown>, ctx: string) {
    for (const key of LEGADO_JSON_KEYS) {
        expect(ticket[key], `${ctx} não deve expor ${key}`).toBeFalsy();
    }
}

function assertPdfBasico(buf: Buffer) {
    expect(buf.subarray(0, 5).toString('ascii')).toBe('%PDF-');
    expect(buf.length).toBeGreaterThan(500);
    assertPdfSemConexao(buf);
}

test.describe('F44 — smoke Relatórios / CSV / PDF por origem', () => {
    let headers: ApiHeaders;
    let massa: F44Massa;
    let f42ClienteId: number;
    let f43ClienteId: number;

    test.beforeAll(async () => {
        const setup = await ensureMassaF44(baseUrl, credenciais.email, credenciais.senha);
        headers = setup.headers;
        massa = setup.massa;

        const f42 = JSON.parse(
            fs.readFileSync(path.join(__dirname, '..', '.massa-f42.json'), 'utf8')
        ) as { clienteId: number };
        const f43 = JSON.parse(
            fs.readFileSync(path.join(__dirname, '..', '.massa-f43.json'), 'utf8')
        ) as { clienteId: number };
        f42ClienteId = f42.clienteId;
        f43ClienteId = f43.clienteId;
    });

    test('API + UI: origem, CSV, PDF, sem legado', async ({ page }) => {
        test.setTimeout(300_000);

        const manualBusca = await buscarRelatorio(
            baseUrl,
            headers,
            'ATIVO_MANUAL',
            massa.manual.numeroTicket
        );
        expect(manualBusca.some(t => t.numeroTicket === massa.manual.numeroTicket)).toBe(true);
        expect(manualBusca.every(t => t.origemTicket === 'ATIVO_MANUAL')).toBe(true);

        const receptivoBusca = await buscarRelatorio(
            baseUrl,
            headers,
            'RECEPTIVO_WHATSAPP',
            massa.receptivo.numeroTicket
        );
        expect(receptivoBusca.some(t => t.numeroTicket === massa.receptivo.numeroTicket)).toBe(true);
        expect(receptivoBusca.every(t => t.origemTicket === 'RECEPTIVO_WHATSAPP')).toBe(true);

        for (const t of [...manualBusca, ...receptivoBusca]) {
            if (t.numeroTicket === massa.manual.numeroTicket || t.numeroTicket === massa.receptivo.numeroTicket) {
                assertTicketJsonSemLegado(t as Record<string, unknown>, t.numeroTicket ?? '');
            }
        }

        const csvManualBuf = await exportarCsv(
            baseUrl,
            headers,
            'ATIVO_MANUAL',
            massa.manual.numeroTicket
        );
        const csvManual = decodeCsvBody(csvManualBuf);
        expect(csvManual).toContain('Origem ticket');
        expect(csvManual).toContain('ATIVO_MANUAL');
        expect(csvManual).toContain(massa.manual.numeroTicket);
        assertCsvSemLegado(csvManual);

        const csvReceptivoBuf = await exportarCsv(
            baseUrl,
            headers,
            'RECEPTIVO_WHATSAPP',
            massa.receptivo.numeroTicket
        );
        const csvReceptivo = decodeCsvBody(csvReceptivoBuf);
        expect(csvReceptivo).toContain('RECEPTIVO_WHATSAPP');
        expect(csvReceptivo).toContain(massa.receptivo.numeroTicket);
        assertCsvSemLegado(csvReceptivo);

        const pdfManual = await baixarPdf(baseUrl, headers, massa.manual.numeroTicket);
        expect(pdfManual.status).toBe(200);
        expect(pdfManual.contentType).toMatch(/pdf/i);
        assertPdfBasico(pdfManual.buf);

        const ticketManualApi = await fetch(
            `${baseUrl}/api/tickets/${encodeURIComponent(massa.manual.numeroTicket)}`,
            { headers }
        );
        const ticketManualBody = (await ticketManualApi.json()) as Record<string, string>;
        expect(ticketManualBody.origemTicket).toBe('ATIVO_MANUAL');
        expect(ticketManualBody.cliente).toContain(massa.manual.clienteNome.slice(0, 10));
        expect(ticketManualBody.contatoNome).toContain(massa.manual.contatoNome.slice(0, 10));

        const pdfReceptivo = await baixarPdf(baseUrl, headers, massa.receptivo.numeroTicket);
        expect(pdfReceptivo.status).toBe(200);
        assertPdfBasico(pdfReceptivo.buf);

        const ticketReceptivoApi = await fetch(
            `${baseUrl}/api/tickets/${encodeURIComponent(massa.receptivo.numeroTicket)}`,
            { headers }
        );
        const ticketReceptivoBody = (await ticketReceptivoApi.json()) as Record<string, string>;
        expect(ticketReceptivoBody.origemTicket).toBe('RECEPTIVO_WHATSAPP');
        expect(ticketReceptivoBody.cliente).toContain(massa.receptivo.clienteNome.slice(0, 10));
        expect(ticketReceptivoBody.contatoNome).toContain(massa.receptivo.contatoNome.slice(0, 10));

        await loginUi(page);
        await abrirRelatoriosDetalhado(page);

        const uiManual = await gerarRelatorioUi(page, 'ATIVO_MANUAL', f42ClienteId);
        expect(uiManual.length).toBeGreaterThan(0);
        await expect(page.locator('#relatorioTicketsBody')).toContainText(massa.manual.numeroTicket, {
            timeout: 15_000
        });
        await expect(page.locator('#relatorioTicketsBody')).toContainText(/Ativo manual|ATIVO_MANUAL/i);

        const uiReceptivo = await gerarRelatorioUi(page, 'RECEPTIVO_WHATSAPP', f43ClienteId);
        expect(uiReceptivo.length).toBeGreaterThan(0);
        await expect(page.locator('#relatorioTicketsBody')).toContainText(massa.receptivo.numeroTicket, {
            timeout: 15_000
        });

        const tableText = await page.locator('#relatorioTicketsBody').innerText();
        expect(tableText.toLowerCase()).not.toContain('conexão');
        expect(tableText.toLowerCase()).not.toMatch(/(^|\s)carteira(\s|$)/i);
    });
});

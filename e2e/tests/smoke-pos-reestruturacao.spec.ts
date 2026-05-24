import { test, expect } from '@playwright/test';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import {
    abrirRelatoriosDetalhado,
    abrirSubmenuClientes,
    abrirTicketNoChats,
    assertChatsNetworkSemLegado,
    assertTicketJsonSemLegado,
    gerarRelatorioUi,
    isCriticalConsoleError,
    loginUi
} from './helpers/e2eUi.js';
import {
    assertCsvSemLegado,
    assertPdfSemConexao,
    baixarPdf,
    buscarRelatorio,
    decodeCsvBody,
    exportarCsv
} from './helpers/f44Massa.js';
import type { ApiHeaders } from './helpers/f42Massa.js';
import { loginApi } from './helpers/f42Massa.js';
import {
    criarMassaPosReestruturacao,
    type MassaPosReestruturacao
} from './helpers/massaPosReestruturacao.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const baseUrl = process.env.E2E_BASE_URL || 'http://localhost:8080';

const credenciais = JSON.parse(
    fs.readFileSync(path.join(__dirname, '..', '.massa.json'), 'utf8')
) as { email: string; senha: string };

function assertPdfBasico(buf: Buffer) {
    expect(buf.subarray(0, 5).toString('ascii')).toBe('%PDF-');
    expect(buf.length).toBeGreaterThan(500);
    assertPdfSemConexao(buf);
}

test.describe('F45 — suite E2E pós-reestruturação (F42+F43+F44)', () => {
    let headers: ApiHeaders;
    let massa: MassaPosReestruturacao;

    test.beforeAll(async () => {
        const login = await loginApi(baseUrl, credenciais.email, credenciais.senha);
        headers = login.headers;
        massa = await criarMassaPosReestruturacao(baseUrl, headers);
        fs.writeFileSync(
            path.join(__dirname, '..', '.massa-pos-reestruturacao.json'),
            JSON.stringify(massa, null, 2),
            'utf8'
        );
    });

    test('fluxo completo operacional sem legado', async ({ page }) => {
        test.setTimeout(360_000);
        const consoleErrors: string[] = [];
        page.on('console', msg => {
            if (msg.type() === 'error' && isCriticalConsoleError(msg.text())) {
                consoleErrors.push(msg.text());
            }
        });

        const manualBusca = await buscarRelatorio(
            baseUrl,
            headers,
            'ATIVO_MANUAL',
            massa.ticketManual
        );
        expect(manualBusca.some(t => t.numeroTicket === massa.ticketManual)).toBe(true);

        const receptivoBusca = await buscarRelatorio(
            baseUrl,
            headers,
            'RECEPTIVO_WHATSAPP',
            massa.ticketReceptivo
        );
        expect(receptivoBusca.some(t => t.numeroTicket === massa.ticketReceptivo)).toBe(true);

        for (const t of [...manualBusca, ...receptivoBusca]) {
            if (t.numeroTicket === massa.ticketManual || t.numeroTicket === massa.ticketReceptivo) {
                assertTicketJsonSemLegado(t as Record<string, unknown>, String(t.numeroTicket));
            }
        }

        const csvManual = decodeCsvBody(
            await exportarCsv(baseUrl, headers, 'ATIVO_MANUAL', massa.ticketManual)
        );
        expect(csvManual).toContain('Origem ticket');
        expect(csvManual).toContain('ATIVO_MANUAL');
        assertCsvSemLegado(csvManual);

        const csvReceptivo = decodeCsvBody(
            await exportarCsv(baseUrl, headers, 'RECEPTIVO_WHATSAPP', massa.ticketReceptivo)
        );
        expect(csvReceptivo).toContain('RECEPTIVO_WHATSAPP');
        assertCsvSemLegado(csvReceptivo);

        const pdfManual = await baixarPdf(baseUrl, headers, massa.ticketManual);
        expect(pdfManual.status).toBe(200);
        assertPdfBasico(pdfManual.buf);

        const pdfReceptivo = await baixarPdf(baseUrl, headers, massa.ticketReceptivo);
        expect(pdfReceptivo.status).toBe(200);
        assertPdfBasico(pdfReceptivo.buf);

        const ticketManualRes = await fetch(
            `${baseUrl}/api/tickets/${encodeURIComponent(massa.ticketManual)}`,
            { headers }
        );
        expect(ticketManualRes.ok).toBe(true);
        const ticketManualBody = (await ticketManualRes.json()) as Record<string, unknown>;
        expect(ticketManualBody.origemTicket).toBe('ATIVO_MANUAL');
        expect(Number(ticketManualBody.clienteId)).toBe(massa.clienteId);
        expect(Number(ticketManualBody.contatoId)).toBe(massa.contatoId);

        const ticketReceptivoRes = await fetch(
            `${baseUrl}/api/tickets/${encodeURIComponent(massa.ticketReceptivo)}`,
            { headers }
        );
        expect(ticketReceptivoRes.ok).toBe(true);
        const ticketReceptivoBody = (await ticketReceptivoRes.json()) as Record<string, unknown>;
        expect(ticketReceptivoBody.origemTicket).toBe('RECEPTIVO_WHATSAPP');
        expect(Number(ticketReceptivoBody.whatsappMatrizId)).toBe(massa.matrizId);

        await loginUi(page, credenciais.email, credenciais.senha);

        await expect(page.locator('#page-dashboard.active, #page-dashboard')).toBeVisible({
            timeout: 10_000
        });
        await expect(page.locator('#page-dashboard .page-header h1')).toBeVisible();

        const chatsNetwork: string[] = [];
        const onReq = (req: { url: () => string }) => chatsNetwork.push(req.url());
        page.on('request', onReq);

        const ticketsLoad = page.waitForResponse(
            r => r.url().includes('/api/tickets') && r.request().method() === 'GET' && r.ok()
        );
        await page.getByTestId('nav-chats').click();
        await expect(page.locator('#page-chats.active')).toBeVisible({ timeout: 15_000 });
        await ticketsLoad;

        await abrirTicketNoChats(page, massa.ticketManual);
        await abrirTicketNoChats(page, massa.ticketReceptivo);
        assertChatsNetworkSemLegado(chatsNetwork);
        page.off('request', onReq);

        await abrirSubmenuClientes(page, 'listagem');
        await expect(page.locator('#clienteListaBusca')).toBeVisible();
        const listaResp = page.waitForResponse(
            r => r.url().includes('/api/clientes') && r.request().method() === 'GET' && r.ok()
        );
        await page.locator('#clienteListaBusca').fill(massa.clienteNome.slice(0, 20));
        await listaResp;
        await expect(page.locator('#clientesListaTableBody')).toContainText(massa.clienteNome, {
            timeout: 20_000
        });

        await abrirSubmenuClientes(page, 'contatos');
        await expect(page.locator('#contatosGestaoBusca')).toBeVisible();
        const contResp = page.waitForResponse(
            r => r.url().includes('/api/contatos') && r.request().method() === 'GET' && r.ok()
        );
        await page.locator('#contatosGestaoBusca').fill(massa.contatoNome.slice(0, 18));
        await contResp;
        await expect(page.locator('#contatosGestaoTableBody')).toContainText(massa.contatoNome, {
            timeout: 20_000
        });

        await page.locator('button.nav-item[data-page="abrir-ticket"]').click();
        await expect(page.locator('#page-abrir-ticket.active')).toBeVisible({ timeout: 15_000 });

        await abrirRelatoriosDetalhado(page);
        const uiManual = await gerarRelatorioUi(page, 'ATIVO_MANUAL', massa.clienteId);
        expect(uiManual.length).toBeGreaterThan(0);
        await expect(page.locator('#relatorioTicketsBody')).toContainText(massa.ticketManual);

        const uiReceptivo = await gerarRelatorioUi(page, 'RECEPTIVO_WHATSAPP', massa.clienteId);
        expect(uiReceptivo.length).toBeGreaterThan(0);
        await expect(page.locator('#relatorioTicketsBody')).toContainText(massa.ticketReceptivo);

        await page.locator('button.nav-item[data-page="configuracoes"]').click();
        await expect(page.locator('#page-configuracoes.active')).toBeVisible({ timeout: 15_000 });
        await expect(page.locator('#configConexoesRevendaSection')).toBeVisible();
        await expect(page.locator('#configConexoesRevendaSection')).toContainText(/Conexões/i);

        expect(consoleErrors.filter(isCriticalConsoleError)).toEqual([]);
    });
});

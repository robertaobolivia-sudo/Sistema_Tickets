import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { loginApi, type ApiHeaders } from './f42Massa.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
export const MASSA_POS_FILE = '.massa-pos-reestruturacao.json';

export type MassaPosReestruturacao = {
    ts: number;
    clienteNome: string;
    clienteEmpresa: string;
    clienteId: number;
    /** Contato da abertura manual (UI). */
    contatoNome: string;
    contatoWhatsapp: string;
    contatoId: number;
    /** Segundo contato — receptivo WhatsApp (mesmo Cliente, telefone distinto). */
    contatoReceptivoNome: string;
    contatoReceptivoWhatsapp: string;
    contatoReceptivoId: number;
    matrizId: number;
    matrizNumero: string;
    matrizNome: string;
    ticketManual: string;
    ticketReceptivo: string;
};

function massaPath() {
    return path.join(__dirname, '..', '..', MASSA_POS_FILE);
}

function readMassaFile(): MassaPosReestruturacao | null {
    const p = massaPath();
    if (!fs.existsSync(p)) {
        return null;
    }
    return JSON.parse(fs.readFileSync(p, 'utf8')) as MassaPosReestruturacao;
}

function writeMassaFile(massa: MassaPosReestruturacao) {
    fs.writeFileSync(massaPath(), JSON.stringify(massa, null, 2), 'utf8');
}

async function clienteExiste(baseUrl: string, headers: ApiHeaders, id?: number) {
    if (!id) return false;
    const res = await fetch(`${baseUrl}/api/clientes/${id}`, { headers });
    return res.ok;
}

async function ticketExiste(baseUrl: string, headers: ApiHeaders, numero?: string) {
    if (!numero) return false;
    const res = await fetch(`${baseUrl}/api/tickets/${encodeURIComponent(numero)}`, { headers });
    return res.ok;
}

async function fetchTicket(baseUrl: string, headers: ApiHeaders, numero: string) {
    const res = await fetch(`${baseUrl}/api/tickets/${encodeURIComponent(numero)}`, { headers });
    if (!res.ok) {
        throw new Error(`GET ticket ${numero}: ${res.status}`);
    }
    return (await res.json()) as Record<string, unknown>;
}

async function massaValida(
    baseUrl: string,
    headers: ApiHeaders,
    m: MassaPosReestruturacao | null
): Promise<boolean> {
    if (!m?.clienteId || !m.ticketManual || !m.ticketReceptivo) {
        return false;
    }
    if (!(await clienteExiste(baseUrl, headers, m.clienteId))) {
        return false;
    }
    if (!(await ticketExiste(baseUrl, headers, m.ticketManual))) {
        return false;
    }
    if (!(await ticketExiste(baseUrl, headers, m.ticketReceptivo))) {
        return false;
    }
    try {
        const manual = await fetchTicket(baseUrl, headers, m.ticketManual);
        const receptivo = await fetchTicket(baseUrl, headers, m.ticketReceptivo);
        if (manual.origemTicket !== 'ATIVO_MANUAL') return false;
        if (receptivo.origemTicket !== 'RECEPTIVO_WHATSAPP') return false;
        if (Number(manual.clienteId) !== m.clienteId) return false;
        if (Number(receptivo.clienteId) !== m.clienteId) return false;
    } catch {
        return false;
    }
    return true;
}

export async function ensureMassaPosReestruturacao(
    baseUrl: string,
    email: string,
    senha: string
): Promise<{ headers: ApiHeaders; massa: MassaPosReestruturacao }> {
    const { headers } = await loginApi(baseUrl, email, senha);
    const cached = readMassaFile();
    if (await massaValida(baseUrl, headers, cached)) {
        return { headers, massa: cached! };
    }
    const massa = await criarMassaPosReestruturacao(baseUrl, headers);
    writeMassaFile(massa);
    return { headers, massa };
}

export async function criarMassaPosReestruturacao(
    baseUrl: string,
    headers: ApiHeaders
): Promise<MassaPosReestruturacao> {
    const ts = Date.now();
    const clienteNome = `Cliente E2E Pos ${ts}`;
    const clienteEmpresa = `Cliente E2E Pos LTDA ${ts}`;
    const contatoNome = `Contato E2E Manual ${ts}`;
    const contatoWhatsapp = `5511999${String(ts).slice(-7)}`;
    const contatoReceptivoNome = `Contato E2E Receptivo ${ts}`;
    const contatoReceptivoWhatsapp = `5511988${String(ts).slice(-7)}`;
    const matrizNumero = `5511977${String(ts).slice(-7)}`;
    const matrizNome = `Matriz E2E Pos ${ts}`;

    const clienteRes = await fetch(`${baseUrl}/api/clientes`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            nome: clienteNome,
            empresa: clienteEmpresa,
            razaoSocial: clienteEmpresa,
            telefone: '5511988880200',
            telefoneContato: '5511988880201',
            email: `e2e-pos-${ts}@example.test`,
            status: 'ATIVO'
        })
    });
    if (!clienteRes.ok) {
        throw new Error(`Cliente E2E Pos: ${clienteRes.status} ${await clienteRes.text()}`);
    }
    const cliente = (await clienteRes.json()) as { id?: number };
    if (!cliente.id) {
        throw new Error('Cliente E2E Pos sem id');
    }

    const contatoRes = await fetch(`${baseUrl}/api/contatos`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            clienteId: cliente.id,
            nome: contatoNome,
            whatsapp: contatoWhatsapp,
            ativo: true
        })
    });
    if (!contatoRes.ok) {
        throw new Error(`Contato E2E Pos: ${contatoRes.status} ${await contatoRes.text()}`);
    }
    const contato = (await contatoRes.json()) as { id?: number };
    if (!contato.id) {
        throw new Error('Contato E2E Pos sem id');
    }

    const contatoRecRes = await fetch(`${baseUrl}/api/contatos`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            clienteId: cliente.id,
            nome: contatoReceptivoNome,
            whatsapp: contatoReceptivoWhatsapp,
            ativo: true
        })
    });
    if (!contatoRecRes.ok) {
        throw new Error(`Contato receptivo E2E Pos: ${contatoRecRes.status} ${await contatoRecRes.text()}`);
    }
    const contatoRec = (await contatoRecRes.json()) as { id?: number };
    if (!contatoRec.id) {
        throw new Error('Contato receptivo E2E Pos sem id');
    }

    const matrizRes = await fetch(`${baseUrl}/api/whatsapp-matrizes`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            clienteId: cliente.id,
            nome: matrizNome,
            numero: matrizNumero,
            ativo: true
        })
    });
    if (!matrizRes.ok) {
        throw new Error(`Matriz E2E Pos: ${matrizRes.status} ${await matrizRes.text()}`);
    }
    const matriz = (await matrizRes.json()) as { id?: number; numero?: string };
    if (!matriz.id) {
        throw new Error('Matriz E2E Pos sem id');
    }

    const manualRes = await fetch(`${baseUrl}/api/tickets`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            clienteContratanteId: cliente.id,
            contatoWhatsappId: contato.id,
            cliente: clienteNome,
            canal: 'WhatsApp',
            mensagem: `E2E Pos manual ${ts}`,
            prioridade: 'MEDIA'
        })
    });
    if (!manualRes.ok) {
        throw new Error(`Ticket manual E2E Pos: ${manualRes.status} ${await manualRes.text()}`);
    }
    const manualBody = (await manualRes.json()) as { numeroTicket?: string; origemTicket?: string };
    if (!manualBody.numeroTicket || manualBody.origemTicket !== 'ATIVO_MANUAL') {
        throw new Error(`Ticket manual inválido: ${JSON.stringify(manualBody)}`);
    }

    const integracaoRes = await fetch(`${baseUrl}/api/integracoes/whatsapp/mensagens`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            telefone: contatoReceptivoWhatsapp,
            nomeContato: contatoReceptivoNome,
            mensagem: `E2E Pos receptivo ${ts}`,
            canal: 'WHATSAPP',
            whatsappMatrizId: matriz.id,
            origemExternaId: `e2e-pos-receptivo-${ts}`
        })
    });
    if (!integracaoRes.ok) {
        throw new Error(
            `Integração receptiva E2E Pos: ${integracaoRes.status} ${await integracaoRes.text()}`
        );
    }
    const integracao = (await integracaoRes.json()) as { numeroTicket?: string };
    if (!integracao.numeroTicket) {
        throw new Error(`Integração E2E Pos sem numeroTicket: ${JSON.stringify(integracao)}`);
    }

    const receptivo = await fetchTicket(baseUrl, headers, integracao.numeroTicket);
    if (receptivo.origemTicket !== 'RECEPTIVO_WHATSAPP') {
        throw new Error(`Receptivo origem=${receptivo.origemTicket}`);
    }

    return {
        ts,
        clienteNome,
        clienteEmpresa,
        clienteId: cliente.id,
        contatoNome,
        contatoWhatsapp,
        contatoId: contato.id,
        contatoReceptivoNome,
        contatoReceptivoWhatsapp,
        contatoReceptivoId: contatoRec.id,
        matrizId: matriz.id,
        matrizNumero: matriz.numero || matrizNumero,
        matrizNome,
        ticketManual: manualBody.numeroTicket,
        ticketReceptivo: integracao.numeroTicket
    };
}

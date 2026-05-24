import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import type { ApiHeaders } from './f42Massa.js';
import {
    ensureMassaPosReestruturacao,
    type MassaPosReestruturacao
} from './massaPosReestruturacao.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export type F44Massa = {
    manual: {
        numeroTicket: string;
        clienteNome: string;
        contatoNome: string;
        origemTicket: 'ATIVO_MANUAL';
    };
    receptivo: {
        numeroTicket: string;
        clienteNome: string;
        contatoNome: string;
        origemTicket: 'RECEPTIVO_WHATSAPP';
    };
};

function mapF44(m: MassaPosReestruturacao): F44Massa {
    return {
        manual: {
            numeroTicket: m.ticketManual,
            clienteNome: m.clienteNome,
            contatoNome: m.contatoNome,
            origemTicket: 'ATIVO_MANUAL'
        },
        receptivo: {
            numeroTicket: m.ticketReceptivo,
            clienteNome: m.clienteNome,
            contatoNome: m.contatoReceptivoNome,
            origemTicket: 'RECEPTIVO_WHATSAPP'
        }
    };
}

export async function ensureMassaF44(
    baseUrl: string,
    email: string,
    senha: string
): Promise<{ headers: ApiHeaders; massa: F44Massa }> {
    const { headers, massa } = await ensureMassaPosReestruturacao(baseUrl, email, senha);
    const f44 = mapF44(massa);
    fs.writeFileSync(path.join(__dirname, '..', '..', '.massa-f44.json'), JSON.stringify(f44, null, 2), 'utf8');
    return { headers, massa: f44 };
}

export function decodeCsvBody(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    let start = 0;
    if (bytes.length >= 3 && bytes[0] === 0xef && bytes[1] === 0xbb && bytes[2] === 0xbf) {
        start = 3;
    }
    return new TextDecoder('utf-8').decode(bytes.slice(start));
}

export function assertCsvSemLegado(csv: string) {
    const lower = csv.toLowerCase();
    if (lower.includes('conexão') || lower.includes('conexao')) {
        throw new Error('CSV contém coluna/rótulo Conexão operacional');
    }
    if (lower.includes('carteira') && lower.includes('revenda')) {
        throw new Error('CSV contém legado Carteira/Revenda');
    }
}

export function assertPdfSemConexao(pdfBytes: Buffer) {
    const text = pdfBytes.toString('latin1');
    if (text.includes('Conexão') || text.includes('Conexao')) {
        throw new Error('PDF contém rótulo Conexão operacional');
    }
}

export async function buscarRelatorio(
    baseUrl: string,
    headers: ApiHeaders,
    origem: string,
    textoLivre: string
) {
    const q = new URLSearchParams({ origemTicket: origem, textoLivre });
    const res = await fetch(`${baseUrl}/api/tickets/busca?${q}`, { headers });
    if (!res.ok) {
        throw new Error(`busca relatório: ${res.status}`);
    }
    return (await res.json()) as Array<{ numeroTicket?: string; origemTicket?: string }>;
}

export async function exportarCsv(
    baseUrl: string,
    headers: ApiHeaders,
    origem: string,
    textoLivre: string
) {
    const q = new URLSearchParams({ origemTicket: origem, textoLivre });
    const res = await fetch(`${baseUrl}/api/tickets/relatorios/csv?${q}`, { headers });
    if (!res.ok) {
        throw new Error(`CSV export: ${res.status}`);
    }
    return res.arrayBuffer();
}

export async function baixarPdf(baseUrl: string, headers: ApiHeaders, numeroTicket: string) {
    const res = await fetch(`${baseUrl}/api/tickets/${encodeURIComponent(numeroTicket)}/pdf`, {
        headers
    });
    if (!res.ok) {
        throw new Error(`PDF ${numeroTicket}: ${res.status}`);
    }
    const buf = Buffer.from(await res.arrayBuffer());
    return { status: res.status, buf, contentType: res.headers.get('content-type') ?? '' };
}

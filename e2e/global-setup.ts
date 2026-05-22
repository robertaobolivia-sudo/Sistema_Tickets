import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

function maskEmail(value: string): string {
    const at = value.indexOf('@');
    if (at <= 1) return '***';
    return `${value.slice(0, 2)}***${value.slice(at)}`;
}

async function resolverIdsEncerramento(
    baseUrl: string,
    headers: Record<string, string>
): Promise<{ grupoId: string; subgrupoId: string; motivoId: string }> {
    const gruposRes = await fetch(`${baseUrl}/api/grupos-categoria`, { headers });
    if (!gruposRes.ok) {
        throw new Error(`Listar grupos falhou: ${gruposRes.status}`);
    }
    const grupos = (await gruposRes.json()) as Array<{ id: number; ativo?: boolean }>;
    const grupo = grupos.find(g => g.ativo !== false) ?? grupos[0];
    if (!grupo?.id) {
        throw new Error('Nenhum grupo de categoria para encerramento E2E');
    }

    const subsRes = await fetch(`${baseUrl}/api/subgrupos-categoria/grupo/${grupo.id}`, {
        headers
    });
    if (!subsRes.ok) {
        throw new Error(`Listar subgrupos falhou: ${subsRes.status}`);
    }
    const subs = (await subsRes.json()) as Array<{ id: number; ativo?: boolean }>;
    const sub = subs.find(s => s.ativo !== false) ?? subs[0];
    if (!sub?.id) {
        throw new Error('Nenhum subgrupo para encerramento E2E');
    }

    const motivosRes = await fetch(
        `${baseUrl}/api/motivos?subgrupoId=${encodeURIComponent(String(sub.id))}`,
        { headers }
    );
    if (!motivosRes.ok) {
        throw new Error(`Listar motivos falhou: ${motivosRes.status}`);
    }
    const motivos = (await motivosRes.json()) as Array<{ id: number; ativo?: boolean }>;
    const motivo = motivos.find(m => m.ativo !== false) ?? motivos[0];
    if (!motivo?.id) {
        throw new Error('Nenhum motivo ativo para encerramento E2E');
    }

    return {
        grupoId: String(grupo.id),
        subgrupoId: String(sub.id),
        motivoId: String(motivo.id)
    };
}

async function postIntegracaoWhatsapp(
    baseUrl: string,
    headers: Record<string, string>,
    body: Record<string, unknown>
): Promise<Response> {
    return fetch(`${baseUrl}/api/integracoes/whatsapp/mensagens`, {
        method: 'POST',
        headers,
        body: JSON.stringify(body)
    });
}

async function criarTicketAberto(
    baseUrl: string,
    headers: Record<string, string>,
    suffix: number,
    tag: string
): Promise<{ numeroTicket: string; telefone: string }> {
    const telefone = `5511963978${String(suffix).slice(-3)}`;
    const baseBody = {
        telefone,
        nomeContato: `E2E Playwright ${tag}`,
        mensagem: `Abertura E2E ${tag}`,
        canal: 'WHATSAPP',
        origemExternaId: `e2e-${tag}-${suffix}`
    };
    const matrizId = process.env.E2E_WHATSAPP_MATRIZ_ID
        ? Number(process.env.E2E_WHATSAPP_MATRIZ_ID)
        : 2;
    let integracaoRes = await postIntegracaoWhatsapp(baseUrl, headers, {
        ...baseBody,
        whatsappMatrizId: matrizId
    });
    if (!integracaoRes.ok) {
        integracaoRes = await postIntegracaoWhatsapp(baseUrl, headers, baseBody);
    }
    if (!integracaoRes.ok) {
        const err = await integracaoRes.text();
        throw new Error(`Integração (${tag}) falhou: ${integracaoRes.status} ${err}`);
    }
    const integracao = await integracaoRes.json();
    if (!integracao.ticketCriado || !integracao.numeroTicket) {
        throw new Error(`Esperado ticket novo (${tag}); recebido: ${JSON.stringify(integracao)}`);
    }
    return { numeroTicket: integracao.numeroTicket, telefone };
}

async function assertTicketBuscaEncontravel(
    baseUrl: string,
    headers: Record<string, string>,
    numeroTicket: string,
    tag: string
) {
    const url = `${baseUrl}/api/tickets/busca?textoLivre=${encodeURIComponent(numeroTicket)}`;
    const res = await fetch(url, { headers });
    if (!res.ok) {
        const err = await res.text();
        throw new Error(`Busca API (${tag}) falhou: ${res.status} ${err}`);
    }
    const lista = (await res.json()) as Array<{ numeroTicket?: string }>;
    const ok = lista.some(t => t.numeroTicket === numeroTicket);
    if (!ok) {
        throw new Error(
            `Ticket ${numeroTicket} (${tag}) não retornou em GET /api/tickets/busca após criação`
        );
    }
}

async function criarTicketSemContato(
    baseUrl: string,
    headers: Record<string, string>,
    suffix: number
): Promise<{ numeroTicket: string }> {
    const criarRes = await fetch(`${baseUrl}/api/tickets`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            cliente: `E2E Sem Contato ${suffix}`,
            mensagem: 'Abertura E2E 221 sem contato vinculado',
            canal: 'INTERNO',
            prioridade: 'MEDIA'
        })
    });
    if (!criarRes.ok) {
        const err = await criarRes.text();
        throw new Error(`Criação ticket sem contato falhou: ${criarRes.status} ${err}`);
    }
    const criado = await criarRes.json();
    if (!criado.numeroTicket) {
        throw new Error(`Ticket sem contato inválido: ${JSON.stringify(criado)}`);
    }
    if (criado.contatoId != null && criado.contatoId !== '') {
        throw new Error(
            `Esperado ticket sem contatoId; recebido contatoId=${criado.contatoId} (${criado.numeroTicket})`
        );
    }
    return { numeroTicket: criado.numeroTicket };
}

export default async function globalSetup() {
    const baseUrl = process.env.E2E_BASE_URL || 'http://localhost:8080';
    const email = process.env.SMOKE_ADMIN_EMAIL || 'robertaobolivia@gmail.com';
    const senha = process.env.SMOKE_ADMIN_SENHA || '@Hipcom123789';
    const massaPath = path.join(__dirname, '.massa.json');

    console.log(`[e2e setup] baseURL=${baseUrl} analista=${maskEmail(email)}`);

    let loginRes: Response | null = null;
    let lastLoginErr = '';
    for (let attempt = 1; attempt <= 15; attempt++) {
        loginRes = await fetch(`${baseUrl}/api/analistas/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, senha })
        });
        if (loginRes.ok) {
            break;
        }
        lastLoginErr = await loginRes.text().catch(() => '');
        if (loginRes.status >= 500 || loginRes.status === 503) {
            await new Promise(r => setTimeout(r, 2000));
            continue;
        }
        break;
    }
    if (!loginRes?.ok) {
        throw new Error(`Login API falhou: ${loginRes?.status} ${lastLoginErr}`.trim());
    }
    const login = await loginRes.json();
    const headers = {
        'Content-Type': 'application/json',
        'X-Analista-Id': String(login.id),
        'X-Analista-Token': login.authToken
    };

    const encerramentoIds = await resolverIdsEncerramento(baseUrl, headers);
    console.log(
        `[e2e setup] encerramento grupo=${encerramentoIds.grupoId} sub=${encerramentoIds.subgrupoId} motivo=${encerramentoIds.motivoId}`
    );

    const suffix = Date.now();
    const comPesquisa = await criarTicketAberto(baseUrl, headers, suffix, '214-com-pesquisa');
    const semPesquisa = await criarTicketAberto(baseUrl, headers, suffix + 1, '220-sem-pesquisa');
    const semContato = await criarTicketSemContato(baseUrl, headers, suffix + 2);

    await assertTicketBuscaEncontravel(baseUrl, headers, comPesquisa.numeroTicket, 'com-pesquisa');
    await assertTicketBuscaEncontravel(baseUrl, headers, semPesquisa.numeroTicket, 'sem-pesquisa');
    await assertTicketBuscaEncontravel(baseUrl, headers, semContato.numeroTicket, 'sem-contato');

    const massa = {
        email,
        senha,
        telefone: comPesquisa.telefone,
        numeroTicket: comPesquisa.numeroTicket,
        telefoneSemPesquisa: semPesquisa.telefone,
        numeroTicketSemPesquisa: semPesquisa.numeroTicket,
        numeroTicketSemContato: semContato.numeroTicket,
        ...encerramentoIds
    };
    fs.writeFileSync(massaPath, JSON.stringify(massa, null, 2), 'utf8');
    console.log(
        `[e2e setup] com pesquisa=${comPesquisa.numeroTicket} sem pesquisa=${semPesquisa.numeroTicket} sem contato=${semContato.numeroTicket} massa=${massaPath} (sobrescrito)`
    );
}

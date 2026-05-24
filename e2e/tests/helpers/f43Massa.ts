import { loginApi, type ApiHeaders } from './f42Massa.js';

export type F43Massa = {
    ts: number;
    clienteNome: string;
    clienteEmpresa: string;
    clienteId: number;
    matrizId: number;
    matrizNumero: string;
    contatoNome: string;
    contatoWhatsapp: string;
    contatoId: number;
    numeroTicket: string;
    origemTicket: string;
};

export { loginApi };

export async function criarMassaF43Receptivo(
    baseUrl: string,
    headers: ApiHeaders
): Promise<F43Massa> {
    const ts = Date.now();
    const clienteNome = `Cliente F43 Receptivo ${ts}`;
    const clienteEmpresa = `Cliente F43 Receptivo LTDA ${ts}`;
    const contatoNome = `Contato F43 Receptivo ${ts}`;
    const contatoWhatsapp = `5511988${String(ts).slice(-7)}`;
    const matrizNumero = `5511977${String(ts).slice(-7)}`;
    const mensagem = `Smoke receptivo F43 ${ts}`;

    const clienteRes = await fetch(`${baseUrl}/api/clientes`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            nome: clienteNome,
            empresa: clienteEmpresa,
            razaoSocial: clienteEmpresa,
            telefone: '5511988880100',
            telefoneContato: '5511988880101',
            email: `f43-receptivo-${ts}@example.test`,
            status: 'ATIVO'
        })
    });
    if (!clienteRes.ok) {
        throw new Error(`Cliente F43: ${clienteRes.status} ${await clienteRes.text()}`);
    }
    const cliente = (await clienteRes.json()) as { id?: number };
    if (!cliente.id) {
        throw new Error(`Cliente F43 sem id`);
    }

    const matrizRes = await fetch(`${baseUrl}/api/whatsapp-matrizes`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            clienteId: cliente.id,
            nome: `Matriz F43 ${ts}`,
            numero: matrizNumero,
            ativo: true
        })
    });
    if (!matrizRes.ok) {
        throw new Error(`Matriz F43: ${matrizRes.status} ${await matrizRes.text()}`);
    }
    const matriz = (await matrizRes.json()) as { id?: number; numero?: string };
    if (!matriz.id) {
        throw new Error(`Matriz F43 sem id`);
    }

    const integracaoRes = await fetch(`${baseUrl}/api/integracoes/whatsapp/mensagens`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            telefone: contatoWhatsapp,
            nomeContato: contatoNome,
            mensagem,
            canal: 'WHATSAPP',
            whatsappMatrizId: matriz.id,
            origemExternaId: `f43-receptivo-${ts}`
        })
    });
    if (!integracaoRes.ok) {
        throw new Error(
            `Integração receptiva F43: ${integracaoRes.status} ${await integracaoRes.text()}`
        );
    }
    const integracao = (await integracaoRes.json()) as {
        ticketCriado?: boolean;
        numeroTicket?: string;
        aguardandoDecisao?: boolean;
    };
    if (!integracao.numeroTicket) {
        throw new Error(`Integração F43 sem numeroTicket: ${JSON.stringify(integracao)}`);
    }
    if (!integracao.ticketCriado && !integracao.aguardandoDecisao) {
        throw new Error(`Esperado ticket novo F43; resposta: ${JSON.stringify(integracao)}`);
    }

    const ticketRes = await fetch(
        `${baseUrl}/api/tickets/${encodeURIComponent(integracao.numeroTicket)}`,
        { headers }
    );
    if (!ticketRes.ok) {
        throw new Error(`GET ticket F43: ${ticketRes.status} ${await ticketRes.text()}`);
    }
    const ticket = (await ticketRes.json()) as Record<string, unknown>;
    assertTicketReceptivoF43(ticket, {
        clienteId: cliente.id,
        matrizId: matriz.id,
        contatoWhatsapp,
        contatoNome
    });

    return {
        ts,
        clienteNome,
        clienteEmpresa,
        clienteId: cliente.id,
        matrizId: matriz.id,
        matrizNumero: matriz.numero || matrizNumero,
        contatoNome,
        contatoWhatsapp,
        contatoId: Number(ticket.contatoId),
        numeroTicket: integracao.numeroTicket,
        origemTicket: String(ticket.origemTicket)
    };
}

function assertTicketReceptivoF43(
    ticket: Record<string, unknown>,
    expected: {
        clienteId: number;
        matrizId: number;
        contatoWhatsapp: string;
        contatoNome: string;
    }
) {
    expectField(ticket, 'origemTicket', 'RECEPTIVO_WHATSAPP');
    expectField(ticket, 'clienteId', expected.clienteId);
    expectField(ticket, 'whatsappMatrizId', expected.matrizId);
    const contatoId = Number(ticket.contatoId);
    if (!Number.isFinite(contatoId) || contatoId <= 0) {
        throw new Error(`contatoId inválido: ${ticket.contatoId}`);
    }
    if (ticket.conexao != null && ticket.conexao !== '') {
        throw new Error(`campo legado conexao presente: ${ticket.conexao}`);
    }
    if (ticket.carteira != null) {
        throw new Error(`campo legado carteira presente`);
    }
    if (ticket.contatoSolicitanteId != null) {
        throw new Error(`campo legado contatoSolicitanteId presente`);
    }
    const wa = String(ticket.contatoWhatsapp ?? '');
    if (!wa.includes(expected.contatoWhatsapp.slice(-8))) {
        throw new Error(`contatoWhatsapp ticket mismatch: ${wa}`);
    }
}

function expectField(ticket: Record<string, unknown>, key: string, expected: string | number) {
    const actual = ticket[key];
    if (typeof expected === 'number') {
        if (Number(actual) !== expected) {
            throw new Error(`ticket.${key} esperado ${expected}, recebido ${actual}`);
        }
        return;
    }
    if (actual !== expected) {
        throw new Error(`ticket.${key} esperado ${expected}, recebido ${actual}`);
    }
}

export type F42Massa = {
    ts: number;
    clienteNome: string;
    clienteEmpresa: string;
    clienteId: number;
    contatoNome: string;
    contatoWhatsapp: string;
    contatoId: number;
};

export type ApiHeaders = Record<string, string>;

export async function criarMassaF42(
    baseUrl: string,
    headers: ApiHeaders
): Promise<F42Massa> {
    const ts = Date.now();
    const clienteNome = `Cliente F42 Smoke ${ts}`;
    const clienteEmpresa = `Cliente F42 Smoke LTDA ${ts}`;
    const contatoNome = `Contato F42 Smoke ${ts}`;
    const contatoWhatsapp = `5511999${String(ts).slice(-7)}`;

    const clienteRes = await fetch(`${baseUrl}/api/clientes`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
            nome: clienteNome,
            empresa: clienteEmpresa,
            razaoSocial: clienteEmpresa,
            telefone: '5511988880001',
            telefoneContato: '5511988880002',
            email: `f42-smoke-${ts}@example.test`,
            status: 'ATIVO'
        })
    });
    if (!clienteRes.ok) {
        const err = await clienteRes.text();
        throw new Error(`Criar Cliente F42 falhou: ${clienteRes.status} ${err}`);
    }
    const cliente = (await clienteRes.json()) as { id?: number };
    if (!cliente.id) {
        throw new Error(`Cliente F42 sem id: ${JSON.stringify(cliente)}`);
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
        const err = await contatoRes.text();
        throw new Error(`Criar Contato F42 falhou: ${contatoRes.status} ${err}`);
    }
    const contato = (await contatoRes.json()) as { id?: number };
    if (!contato.id) {
        throw new Error(`Contato F42 sem id: ${JSON.stringify(contato)}`);
    }

    return {
        ts,
        clienteNome,
        clienteEmpresa,
        clienteId: cliente.id,
        contatoNome,
        contatoWhatsapp,
        contatoId: contato.id
    };
}

export async function loginApi(
    baseUrl: string,
    email: string,
    senha: string
): Promise<{ id: number; authToken: string; headers: ApiHeaders }> {
    const loginRes = await fetch(`${baseUrl}/api/analistas/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha })
    });
    if (!loginRes.ok) {
        const err = await loginRes.text();
        throw new Error(`Login API F42 falhou: ${loginRes.status} ${err}`);
    }
    const login = (await loginRes.json()) as { id: number; authToken: string };
    return {
        id: login.id,
        authToken: login.authToken,
        headers: {
            'Content-Type': 'application/json',
            'X-Analista-Id': String(login.id),
            'X-Analista-Token': login.authToken
        }
    };
}

/** Mensagens padronizadas exibidas ao usuário (Sprint 71). */
export const MSG_ERRO = {
    SESSAO_EXPIRADA: 'Sessão expirada ou inválida. Faça login novamente.',
    ACESSO_ADMIN: 'Acesso restrito a administradores.',
    SEM_PERMISSAO: 'Você não tem permissão para executar esta ação.',
    SEM_PERMISSAO_PAGINA: 'Você não tem permissão para acessar esta página.',
    LOGIN_INVALIDO: 'E-mail ou senha inválidos.',
    CAMPOS_OBRIGATORIOS: 'Verifique os campos obrigatórios.',
    OPERACAO_FALHOU: 'Não foi possível concluir a operação. Tente novamente.'
};

const REGEX_MSG_TECNICA =
    /header\s+X-Analista|token de sessao|identificacao do analista|sessao invalida|expirada\.?\s*Faca login|nao encontrado para autorizacao|Erro interno do servidor|java\.|stacktrace|undefined|null/i;

export function extrairTextoErroBackend(data) {
    if (data == null) {
        return '';
    }
    if (typeof data === 'string') {
        return data.trim();
    }
    const texto = data.erro ?? data.message ?? data.mensagem ?? '';
    return typeof texto === 'string' ? texto.trim() : '';
}

function ehMensagemTecnica(texto) {
    return Boolean(texto && REGEX_MSG_TECNICA.test(texto));
}

function mensagemTecnicaParaUsuario(texto) {
    if (!texto || !ehMensagemTecnica(texto)) {
        return null;
    }
    if (/administrador/i.test(texto)) {
        return MSG_ERRO.ACESSO_ADMIN;
    }
    if (/sessao|token|header X-Analista|identificacao do analista/i.test(texto)) {
        return MSG_ERRO.SESSAO_EXPIRADA;
    }
    return MSG_ERRO.OPERACAO_FALHOU;
}

/**
 * Resolve mensagem amigável para respostas apiFetch (e fetch autenticado).
 */
export function mensagemErroApi(response, data, fallback) {
    const status = response?.status ?? 0;
    const bruto = extrairTextoErroBackend(data);
    const tecnica = mensagemTecnicaParaUsuario(bruto);

    if (status === 401) {
        return MSG_ERRO.SESSAO_EXPIRADA;
    }
    if (status === 403) {
        if (/administrador/i.test(bruto)) {
            return MSG_ERRO.ACESSO_ADMIN;
        }
        if (tecnica) {
            return tecnica;
        }
        if (/proprio|consultar seus|permissao/i.test(bruto)) {
            return MSG_ERRO.SEM_PERMISSAO;
        }
        return MSG_ERRO.SEM_PERMISSAO;
    }
    if (status === 400) {
        if (/Senha deve ter/i.test(bruto)) {
            return bruto;
        }
        if (bruto && !ehMensagemTecnica(bruto)) {
            return bruto;
        }
        return fallback || MSG_ERRO.CAMPOS_OBRIGATORIOS;
    }
    if (status >= 500) {
        return MSG_ERRO.OPERACAO_FALHOU;
    }
    if (bruto && !ehMensagemTecnica(bruto)) {
        return bruto;
    }
    if (tecnica) {
        return tecnica;
    }
    return fallback || MSG_ERRO.OPERACAO_FALHOU;
}

export function mensagemErroLogin(response, data) {
    const bruto = extrairTextoErroBackend(data);
    if (response.status === 401 || response.status === 403) {
        return MSG_ERRO.LOGIN_INVALIDO;
    }
    if (ehMensagemTecnica(bruto) || /sessao|token|header X-Analista/i.test(bruto)) {
        return MSG_ERRO.LOGIN_INVALIDO;
    }
    if (bruto && /senha|e-mail|email|invalid/i.test(bruto)) {
        return MSG_ERRO.LOGIN_INVALIDO;
    }
    return MSG_ERRO.LOGIN_INVALIDO;
}

export function mensagemParaExibirUsuario(message) {
    if (message == null) {
        return MSG_ERRO.OPERACAO_FALHOU;
    }
    if (typeof message === 'object') {
        return MSG_ERRO.OPERACAO_FALHOU;
    }
    const texto = String(message).trim();
    if (!texto) {
        return MSG_ERRO.OPERACAO_FALHOU;
    }
    if (texto.startsWith('{') && texto.endsWith('}')) {
        return MSG_ERRO.OPERACAO_FALHOU;
    }
    return mensagemTecnicaParaUsuario(texto) || texto;
}

export function mensagemErroSessaoApi(response, data, fallback) {
    return mensagemErroApi(response, data, fallback);
}

export function mensagemErroDashboardApi(response, data, fallback) {
    return mensagemErroApi(response, data, fallback);
}

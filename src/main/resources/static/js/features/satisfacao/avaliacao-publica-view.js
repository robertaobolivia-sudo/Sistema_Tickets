/**
 * Estados da UI pública de avaliação (sem DOM).
 */

export function resolveAvaliacaoPublicaUiState(dados) {
    const status = dados?.status != null ? String(dados.status).toUpperCase() : '';
    const expirada = Boolean(dados?.expirada);
    const jaRespondida = Boolean(dados?.jaRespondida);

    if (jaRespondida || status === 'RESPONDIDA') {
        return {
            mostrarFormulario: false,
            mostrarEstadoFinal: true,
            textoEstadoFinal: 'Obrigado! Sua avaliação foi registrada.',
            subtituloPadrao: '',
            estadoVariant: 'respondida'
        };
    }
    if (expirada || status === 'EXPIRADA') {
        return {
            mostrarFormulario: false,
            mostrarEstadoFinal: true,
            textoEstadoFinal: 'O prazo para responder esta pesquisa encerrou.',
            subtituloPadrao: '',
            estadoVariant: 'expirada'
        };
    }
    if (status === 'PENDENTE') {
        return {
            mostrarFormulario: true,
            mostrarEstadoFinal: false,
            textoEstadoFinal: '',
            subtituloPadrao: 'Como foi seu atendimento?',
            estadoVariant: 'pendente'
        };
    }
    return {
        mostrarFormulario: false,
        mostrarEstadoFinal: true,
        textoEstadoFinal: 'Link inválido ou indisponível.',
        subtituloPadrao: '',
        estadoVariant: 'invalida'
    };
}

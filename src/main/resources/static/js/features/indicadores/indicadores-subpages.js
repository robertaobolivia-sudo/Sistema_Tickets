/**
 * Subpáginas internas da área Indicadores (navegação e rótulos).
 */

export const INDICADORES_SUBPAGES = {
    'indicadores-visao-geral': {
        titulo: 'Visão geral',
        subtitulo: 'Resumo executivo dos indicadores (em construção).'
    },
    'indicadores-chamados': {
        titulo: 'Chamados',
        subtitulo: 'Painéis gerenciais de chamados por período de abertura.'
    },
    'indicadores-satisfacao': {
        titulo: 'Satisfação',
        subtitulo: 'Média, distribuição e evolução das avaliações nos tickets.'
    },
    'indicadores-encerramento-satisfacao': {
        titulo: 'Encerramento e satisfação',
        subtitulo: 'Motivos de encerramento, status da pesquisa e notas respondidas.'
    },
    'indicadores-clientes': {
        titulo: 'Clientes / Revendas',
        subtitulo: 'Indicadores por cliente e revenda (em construção).'
    },
    'indicadores-atendentes': {
        titulo: 'Atendentes',
        subtitulo: 'Indicadores por equipe e atendente (em construção).'
    },
    'indicadores-sla': {
        titulo: 'SLA',
        subtitulo: 'Indicadores de cumprimento de SLA (em construção).'
    }
};

export const INDICADORES_SUBPAGE_DEFAULT = 'indicadores-chamados';

export function isIndicadoresSubpageKey(key) {
    return key != null && Object.prototype.hasOwnProperty.call(INDICADORES_SUBPAGES, key);
}

export function getIndicadoresSubpageMeta(key) {
    if (!isIndicadoresSubpageKey(key)) {
        return INDICADORES_SUBPAGES[INDICADORES_SUBPAGE_DEFAULT];
    }
    return INDICADORES_SUBPAGES[key];
}

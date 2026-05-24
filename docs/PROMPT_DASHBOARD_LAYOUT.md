# Prompt definitivo — Dashboard (layout + paleta Suporte Tickets)

Copie o bloco abaixo para o Cursor (anexe as **3 referências visuais**: mockup layout desejado, se houver; captura **tema claro**; captura **tema escuro**).

---

## INÍCIO DO PROMPT (copiar daqui)

```
OBJETIVO
Deixar a página Dashboard (visão geral, `#page-dashboard`) com o LAYOUT estrutural do mockup de referência (faixa Status Operador → Resumo Operacional em 4 cartões → grid por conexão → rodapé de atualização) e com a PALETA DE CORES das capturas atuais do Suporte Tickets (tema claro e tema escuro). Manter `html[data-theme]` e toda a lógica existente (APIs, refresh, Acompanhar, testids).

IMAGENS DE REFERÊNCIA
1) Layout alvo (hierarquia e composição dos blocos — cards horizontais, badges EM ESPERA/ATENDENDO, botão Acompanhar).
2) Captura tema CLARO do Suporte Tickets (cores reais).
3) Captura tema ESCURO do Suporte Tickets (cores reais).

ESCOPO DE ARQUIVOS
- `src/main/resources/static/index.html` — apenas `#page-dashboard` (markup se necessário).
- `src/main/resources/static/css/pages/dashboard.css` — estilos principais; prefixo `#page-dashboard` para não vazar.
- `src/main/resources/static/css/theme.css` — SOMENTE se precisar tokens `--dash-*` globais; preferir variáveis locais ao dashboard.
- JS: `dashboardPage.js`, `dashboardAnalistasOnlineView.js`, `dashboardOperacaoAgoraView.js`, `dashboardOperacaoClienteB2bView.js` — só classes/HTML do render; NÃO alterar contratos de API.

PROIBIDO
- Remover ou renomear `data-testid` existentes.
- Quebrar tema claro/escuro em outras páginas.
- Adicionar frameworks/CSS externos.
- Mudar textos de navegação global fora do dashboard.
- Alterar regras de negócio ou endpoints.

---

## PALETA DE CORES (obrigatória — duas variantes)

### Tema claro (`:root` ou sem `data-theme="dark"`)
| Token semântico | Hex | Uso |
|-----------------|-----|-----|
| Fundo página | `#f8fafc` | Área principal atrás dos cards |
| Card | `#ffffff` | Todos os cards do dashboard |
| Borda card | `#e2e8f0` | Contorno leve |
| Sidebar (shell) | `#0d1e21` | Menu lateral ST |
| Sidebar item ativo | `#163338` | Dashboard ativo + barra verde à esquerda |
| Texto menu sidebar | `#f1f5f9` | Links no menu escuro |
| Texto primário | `#1e293b` | Títulos, números grandes |
| Texto muted | `#64748b` | Labels (Aguardando, TME, cargo, etc.) |
| Verde marca / ONLINE / ATENDENDO | `#00dfa2` (aceitar `#10b981` se já no app) | Status online, pills verdes, ícones de ação |
| Amarelo EM ESPERA / TME | `#facc15` | Topo/card espera, badges amarelos |
| Vermelho SLA vencido | `#ef4444` | Métrica SLA vencido |
| Roxo FINALIZADOS / NPS | `#8b5cf6` | Card finalizados, NPS |
| Sombra card | suave | ex.: `0 1px 3px rgba(15, 23, 42, 0.08)` |

### Tema escuro (`html[data-theme="dark"]`)
| Token semântico | Hex | Uso |
|-----------------|-----|-----|
| Fundo página | `#12191d` | Priorizar sobre `#0f172a` genérico no dashboard |
| Card | `#1b262c` | KPI e cards de conexão |
| Sidebar | `#0f1418` | Mais escuro que o fundo |
| Texto primário | `#ffffff` | Títulos e valores |
| Texto muted | `#a0a0a0` | Labels, OFFLINE |
| Verde operacional | `#00b894` | ONLINE, ATENDENDO, nomes de conexão, botão Atualizar |
| Amarelo EM ESPERA | `#f1c40f` | Tags e métricas de fila |
| Vermelho SLA | `#e74c3c` | Vencido |
| Roxo FINALIZADOS | `#9b59b6` | Hoje / NPS |
| Borda card | `rgba(255,255,255,0.06)` a `0.1` | Contorno sutil |

Implementar em CSS algo como:
```css
#page-dashboard {
  --dash-bg: #f8fafc;
  --dash-card: #ffffff;
  --dash-text: #1e293b;
  --dash-text-muted: #64748b;
  --dash-accent-green: #00dfa2;
  --dash-accent-yellow: #facc15;
  --dash-accent-red: #ef4444;
  --dash-accent-purple: #8b5cf6;
}
html[data-theme="dark"] #page-dashboard {
  --dash-bg: #12191d;
  --dash-card: #1b262c;
  --dash-text: #ffffff;
  --dash-text-muted: #a0a0a0;
  --dash-accent-green: #00b894;
  --dash-accent-yellow: #f1c40f;
  --dash-accent-red: #e74c3c;
  --dash-accent-purple: #9b59b6;
}
```
Regra: amarelo = espera, verde = atendendo/online, vermelho = SLA vencido, roxo = finalizados — nunca trocar semântica entre blocos.

---

## LAYOUT OBRIGATÓRIO (ordem vertical em `#page-dashboard`)

### 0) Cabeçalho da página (alinhar à referência)
- Esquerda: título **Dashboard** (tipografia forte).
- Direita: botão **Atualizar** (outline no claro; fundo teal no escuro), ícone refresh; utilitários da topbar (chat, sino, avatar) podem permanecer no shell global — só harmonizar espaçamento com a captura.
- Avatar: círculo, nome, cargo (ex. Supervisor N2), ponto verde se online.

### 1) Seção **STATUS OPERADOR**
- Título de seção: uppercase, pequeno, muted — `STATUS OPERADOR`.
- Faixa horizontal scrollável de cards de analista (`#dashboardStatusOperadorGrid`).
- Cada card:
  - Avatar circular (iniciais ou foto).
  - Badge de status no topo: **ONLINE** (verde), **OCUPADO** (amarelo), **AUSENTE** (azul/teal), **OFFLINE** (cinza) — conforme dados reais.
  - Nome + cargo (ex. Analista N1, Supervisor N1).
  - Borda ou faixa lateral na cor do status (verde/amarelo/azul/cinza).
- No tema claro: cards brancos sobre fundo `#f8fafc`.
- No tema escuro: cards `#1b262c`, ONLINE com acento `#00b894`.

### 2) Seção **RESUMO OPERACIONAL**
- Título: `RESUMO OPERACIONAL`.
- **Uma linha com 4 cartões de mesma altura** (grid 4 colunas desktop; 2x2 tablet; 1 coluna mobile mantendo ordem).

**Cartão 1 — EM ESPERA (acento amarelo)**
- Cabeçalho: label `EM ESPERA` + ícone fila/grupo.
- Corpo: label “Aguardando” + número grande (`#dashboardOpFilaQtd`).
- Métrica tempo: `TME` + valor (`#dashboardOpFilaTempo`, formato HH:MM:SS).
- Borda superior ou glow `--dash-accent-yellow`.

**Cartão 2 — ATENDENDO (acento verde)**
- Cabeçalho: `ATENDENDO` + ícone headset.
- “Em Atendimento” + número (`#dashboardOpAtendimentoQtd`).
- `TMA` + tempo (`#dashboardOpAtendimentoTempo`).
- Borda/glow verde `--dash-accent-green`.

**Cartão 3 — SLA (um card, três colunas internas)**
- Cabeçalho: `SLA`.
- Três métricas separadas por divisores verticais:
  - **Vencido** + `#dashboardSlaVivoVencido` (vermelho).
  - **Limite** + `#dashboardSlaVivoProximo` (amarelo).
  - **Dentro** + `#dashboardSlaVivoDentro` (verde).
- Labels podem ser curtas (“Vencido”, “Limite”, “Dentro”) como na captura.

**Cartão 4 — FINALIZADOS (acento roxo)**
- Cabeçalho: `FINALIZADOS` + ícone check.
- “Hoje” ou “Finalizados” + `#dashboardEncDiaFinalizados`.
- `NPS` + `#dashboardAvalMedia` (estrela opcional).
- Borda/glow roxo `--dash-accent-purple`.

Tipografia: números/tempos grandes e bold; labels em `--dash-text-muted`.

### 3) Seção **OPERAÇÃO POR CONEXÃO** (ou **Atendimentos por Conexão** — manter título do produto se já estiver no HTML)
- Título de seção uppercase muted.
- Grid de colunas por cliente/conexão (`#conexoesPendencias` / `dashboard-conexoes-grid`).
- Cada coluna/card:
  - Header: razão social em destaque (maiúsculas ou semibold) + badge **Total: N**.
  - Card de ticket em evidência:
    - Pill grande: **EM ESPERA** (fundo amarelo, texto escuro/branco conforme contraste) ou **ATENDENDO** (verde).
    - Canto: **TME** ou **TMA** conforme estado.
    - Linha contato (ícone + nome).
    - Linha analista ou “Não atribuído”.
    - Botão outline **Acompanhar** + ícone olho (cor do acento do card).
  - Paginação inferior: setas + dots se múltiplos tickets.
- Nome da conexão no escuro: cor `--dash-accent-green` (`#00b894`).

### 4) Rodapé do dashboard
- Esquerda: ícone relógio + texto **Última atualização: DD/MM/AAAA HH:MM:SS** (ligar ao timestamp do último `loadDashboard` / refresh).

---

## ESTILO GERAL
- Border-radius cards: 10–12px (alinhado ao app).
- Espaçamento entre seções: 24–32px.
- Títulos de seção: 11–12px, uppercase, letter-spacing 0.03–0.06em, cor muted.
- Padding interno dos cards: 16–20px.
- Não usar azul genérico de Bootstrap; usar apenas a paleta acima.
- Badges pill com padding horizontal generoso; texto legível em claro e escuro.

---

## MAPEAMENTO DOM EXISTENTE (preservar IDs)
- Status operador: `.dashboard-status-operador`, `#dashboardStatusOperadorGrid`
- Resumo: `.dashboard-resumo-operacional`, `.dashboard-resumo-grupo--espera|--atendendo|--sla|--encerramento`, IDs de métricas listados acima
- Conexões: `.dashboard-conexoes-operacionais`, `#conexoesPendencias`
- Testids: `dashboard-status-operador-grid`, `dashboard-op-fila-qtd`, `dashboard-op-atendimento-qtd`, `dashboard-sla-vivo-*`, `dashboard-enc-dia-finalizados`, `dashboard-aval-media`, etc. — todos permanecem no DOM.

---

## CRITÉRIOS DE ACEITE
1. Lado a lado com as capturas claro/escuro: mesma hierarquia de 3 blocos + rodapé; 4 KPIs na mesma linha em desktop.
2. Cores dentro da tolerância dos hex definidos (não substituir verde por azul slate no fundo do dashboard escuro).
3. `html[data-theme="dark"]` e tema claro funcionam sem regressão.
4. Dados reais continuam carregando; placeholders “—” ou “0” quando vazio.
5. Todos os `data-testid` preservados.
6. Sem erros críticos no console ao abrir Dashboard.
7. HTTP 200; não alterar backend.

---

## ENTREGA
- Listar arquivos alterados.
- Resumo seção a seção do que foi alinhado ao layout e à paleta.
- Se algum elemento do mockup não existir na API (ex. OCUPADO/AUSENTE), usar fallback visual sem quebrar grid.
```

## FIM DO PROMPT

---

## Notas para o time

- O `theme.css` global usa `--bg: #0f172a` e `--surface: #1e293b` no dark; o dashboard deve **sobrescrever** com `#12191d` / `#1b262c` via `--dash-*` para bater com as capturas.
- Verde corporativo existente: `--corp-accent-neon: #00ffaa` — harmonizar com `#00b894` / `#00dfa2` nos acentos do dashboard apenas.

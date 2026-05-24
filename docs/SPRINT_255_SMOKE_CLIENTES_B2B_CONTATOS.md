# Sprint 255 — Smoke Clientes B2B e Contatos

## Status

**APROVADO com ressalva de massa** (API + estrutura validadas; base com 8 contratantes por duplicidade histórica além dos 4 da Sprint 253).

## Ambiente

- App: `http://127.0.0.1:8080/` — HTTP **200**
- Login API: ADMIN (`robertaobolivia@gmail.com`) OK
- Código: Sprint 254 (`page-clientes-full`, submenu Cadastro/Listagem/Contatos)
- Maven/npm: não alterados nesta sprint

## Telas / critérios (API — espelha comportamento do front)

| Critério | Resultado |
|----------|-----------|
| Listagem só contratantes B2B | OK — `/api/clientes` retorna cadastro `Cliente` (sem misturar entidade `Contato` como contratante) |
| Cadastro + resumo contatos | OK — `GET /api/contatos?gestao=true&clienteId={id}` retorna 3 contatos por contratante da massa 253 |
| Contatos → filtro contratante | OK — troca de `clienteId` altera contagem (ex.: Fenix 3; IDs LTDA 87–90) |
| Busca em Contatos | OK — `busca=Contato 1` retorna 4 linhas (um por contratante) |
| Pessoa atendida ≠ Cliente principal | OK — contatos só em `/api/contatos?gestao=true`; nomes tipo "Contato N" não aparecem em `/api/clientes` como razão social |
| Tickets/Chats Cliente + Contato | OK — 12 tickets; **12** com `contatoId` preenchido |

## Contratantes conferidos (massa Sprint 253 — razão social LTDA)

| Contratante | Contatos (gestão) |
|-------------|-------------------|
| Rocha Mendes Comercio LTDA | 3 |
| Status Automacao Industria ME | 3 |
| Fast Comercio Varejo SA | 3 |
| Fenix Servicos Digitais LTDA | 3 |

## Ressalva de massa

- `/api/clientes` lista **8** registros: 4 nomes curtos legados + 4 LTDA da seed 253.
- Smoke funcional da Sprint 254 permanece válido nos 4 LTDA; recomenda-se limpeza/merge dos 4 duplicados em sprint de dados.

## Browser (Glass)

- Navegação automatizada não carregou linhas na tabela sem sessão completa no webview; validação operacional complementada via API (mesmos endpoints do front).
- Checklist manual sugerido: login → Clientes → Listagem (tabela larga) → linha → Cadastro (resumo) → Contatos (filtro + busca) → Tickets/Chats.

## Ajustes de código

Nenhum (somente smoke).

## Próximo passo

- Sprint de dados: manter apenas 4 contratantes LTDA na base dev.
- Evolução produto: ações na tela Contatos (CRUD), fora do escopo 255.

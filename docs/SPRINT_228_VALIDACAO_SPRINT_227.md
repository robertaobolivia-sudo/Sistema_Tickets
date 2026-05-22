# Sprint 228 — Validar Sprint 227 (HTTP 200 + smoke filtro Cliente)

## Objetivo

Fechar validação da Sprint 227: app na 8080, smoke em Indicadores → Encerramento e satisfação.

## Ambiente

- `mvn package -DskipTests` + `java -jar target\suporte-tickets-1.0.0.jar`
- HTTP `GET /` → **200**
- Login smoke: `robertaobolivia@gmail.com` (ADMIN, id 11)

## Smoke (navegador / runtime)

| Critério | Resultado |
|----------|-----------|
| Subpágina abre | OK |
| Select Cliente visível | OK (`indicEncFiltroCliente`) |
| Lista clientes ativos | OK (20 opções = 1 “Todos” + 19 ativos) |
| Request com `clienteId` | OK — `/api/indicadores/encerramento-avaliacao?clienteId=66` (cliente **Audit Test**) |
| “Todos os clientes” sem `clienteId` | OK — `/api/indicadores/encerramento-avaliacao` |
| Console erro crítico | Nenhum no fluxo validado |
| JS servido com filtro ativos | OK — `filterClientesAtivosIndicadores` no JAR após rebuild |

## Observações

- JAR antigo na 8080 servia `encerramentoAvaliacaoView.js` **sem** `filterClientesAtivosIndicadores`; o select ficava só com “Todos os clientes” até `mvn package` e reinício.
- Sessão stale no browser (`403` em `/api/clientes` com token em memória ≠ storage) impede carga da lista; login fresco ou reload + login corrige.

## Testes executados

- Vitest: **não** (sem alteração JS nesta sprint)
- Maven: `package -DskipTests` (redeploy)
- API manual (PowerShell): `GET /api/clientes` 200; indicadores com/sem `clienteId` 200

## Sprint 227

**Aprovada** após rebuild e smoke acima.

## Próximo passo

Seguir backlog da estratégia (ex.: validação GHA Sprint 226 ou próxima feature de indicadores).

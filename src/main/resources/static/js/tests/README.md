# Testes JavaScript (Vitest)

Funções puras do frontend modularizado:

- `core/messages.js`, `core/permissions.js`
- `core/queryParams.js` (filtros auditoria, relatórios, tickets, satisfação)
- `rules/ticketViewRules.js` (status, satisfação, interações)
- `rules/slaViewRules.js` (rótulos e badges SLA)

## Pré-requisito

Node.js 18+ e npm na máquina de desenvolvimento.

## Instalar dependências (uma vez)

```bash
cd src/main/resources/static/js
npm install
```

## Rodar testes

```bash
cd src/main/resources/static/js
npm test
```

Modo watch:

```bash
npm run test:watch
```

Os testes **não** rodam no `mvn clean install`; são executados manualmente ou em CI com o comando acima.

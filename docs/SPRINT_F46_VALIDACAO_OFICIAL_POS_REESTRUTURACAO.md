# Sprint F46 — Validação oficial pós-reestruturação

> **Regras de domínio:** [README_MESTRE.md](../README_MESTRE.md). Este doc é só a **esteira de comandos**.

## 1. Objetivo

Padronizar a esteira mínima para **aprovar sprint relevante** após o marco Cliente / Contato / WhatsApp / Ticket (F41–F45).

## 2. Comandos oficiais

| Etapa | Comando |
|-------|---------|
| Backend | `mvn test` |
| Frontend | `cd src\main\resources\static\js` → `npm test` |
| Build | `mvn package -DskipTests` |
| Servidor | `java -jar target\suporte-tickets-1.0.0.jar` |
| E2E pós-reestruturação | `cd e2e` → `$env:E2E_SKIP_WEB_SERVER='1'` → `npm run test:pos-reestruturacao` |
| **Tudo (Windows)** | `.\scripts\validar-pos-reestruturacao.ps1` |

## 3. Ordem correta no Windows

1. `.\scripts\stop-java-8080.ps1` (antes de `package` ou `clean`)
2. `mvn test`
3. Vitest em `static/js`
4. `mvn package -DskipTests`
5. Subir app (`start-dev-server.ps1` ou `java -jar` em terminal dedicado)
6. Playwright F45 com `E2E_SKIP_WEB_SERVER=1`
7. Confirmar HTTP 200 em `/`

## 4. Evitar lock do JAR

- **Nunca** rode `mvn package` com o JAR aberto na 8080.
- Use `stop-java-8080.ps1` antes de `package` / `clean`.
- Se `Unable to rename ... suporte-tickets-1.0.0.jar` → lock, não bug de código.
- Playwright e `java -jar` em **terminais diferentes**.

## 5. Quando rodar Playwright F45

**Obrigatório** se alterar: Cliente, Contato, Ticket, Abrir Ticket, Chats, Relatórios, CSV/PDF, Config que afete fluxo, auth, rotas globais, shell/sidebar, refatoração grande.

**Opcional** se alterar só: doc, CSS sem fluxo, teste unitário isolado, backend sem tela (Maven cobre).

## 6. Critério de aprovação de sprint

- [ ] `mvn test` OK  
- [ ] Vitest OK  
- [ ] `mvn package -DskipTests` OK  
- [ ] HTTP 200 `/`  
- [ ] `npm run test:pos-reestruturacao` OK (quando escopo central)  
- [ ] `docs/` + `AUDITORIA-003` atualizados se sprint registrada  
- [ ] Nenhum legado no runtime (ver §8)

## 7. Exit code 4294967295 (Windows)

Processo Java morto de fora (agente, `taskkill`, fechar janela, fim de sessão). **Não** é código de erro Spring. Suba de novo com `start-dev-server.ps1`.

## 8. Proteção F45 (legados)

A suite `smoke-pos-reestruturacao.spec.ts` cobre ausência operacional de:

- `ticket.conexao`, `Cliente.carteira`, `ContatoCliente`, `TicketEtiqueta`
- rótulos Carteira/Conexão no Chats
- `/api/carteiras` e `/uploads/conexoes` no Chats
- CSV sem coluna Conexão operacional

## 9. Como rodar F45

```powershell
.\scripts\start-dev-server.ps1
cd e2e
$env:E2E_SKIP_WEB_SERVER = '1'
npm run test:pos-reestruturacao
```

Massa: recriada a cada run (`criarMassaPosReestruturacao`). Snapshot: `e2e/.massa-pos-reestruturacao.json`.

## 10. Troubleshooting

| Sintoma | Ação |
|---------|------|
| package rename failed | `stop-java-8080.ps1` → repetir package |
| Playwright ECONNREFUSED | App em 8080? `start-dev-server.ps1` |
| global-setup 500 | MySQL + credenciais `SMOKE_ADMIN_*` |
| Token API após login UI | F45 faz asserts API **antes** do login UI |
| Java sumiu (4294967295) | Reiniciar `start-dev-server.ps1` |

## 11. Scripts

| Script | Função |
|--------|--------|
| `scripts/stop-java-8080.ps1` | Libera porta 8080 |
| `scripts/start-dev-server.ps1` | package + JAR em janela dedicada |
| `scripts/validar-pos-reestruturacao.ps1` | Esteira completa F46 |

## 12. Próximo bloco

- Job CI só `validar-pos-reestruturacao` (ou subset Maven + F45).
- Deprecar specs F42–F44 isolados quando F45 estável N sprints.

## Resultado (execução local)

| Etapa | Resultado |
|-------|-----------|
| `mvn test` | OK |
| Vitest | 219/219 |
| `mvn package -DskipTests` | OK (após `stop-java-8080`) |
| HTTP 200 | OK com JAR em background (~12s Tomcat) |
| Playwright F45 | 1 passed (~1.7m) |
| `validar-pos-reestruturacao.ps1` | Maven+Vitest+package OK; HTTP timeout 180s se Java não subir a tempo — usar `-NoAutoStartServer` + `start-dev-server.ps1` ou aumentar `$HttpWaitSeconds` |

Correção script: `start-dev-server.ps1` — aspas em mensagem de timeout (parse PowerShell).

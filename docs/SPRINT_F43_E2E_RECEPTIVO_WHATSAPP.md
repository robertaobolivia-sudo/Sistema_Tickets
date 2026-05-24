# Sprint F43 — E2E RECEPTIVO_WHATSAPP + Matriz

## 1. Objetivo

Validar fluxo **receptivo WhatsApp simulado** (sem provider real): Cliente + Matriz → mensagem → Contato auto → Ticket `RECEPTIVO_WHATSAPP` → Chats.

## 2. Massa

Timestamp único por execução; snapshot em `e2e/.massa-f43.json` após `beforeAll`.

## 3. Endpoint receptivo

`POST /api/integracoes/whatsapp/mensagens`  
Headers: `X-Analista-Id`, `X-Analista-Token` (sessão ADMIN).  
Body: `telefone`, `nomeContato`, `mensagem`, `canal`, `whatsappMatrizId`, `origemExternaId`.

Integração interna via `IntegracaoMensagemEntradaService` — **NoopWhatsAppMessageSender** permanece; não é bug.

## 4–7. Resultado API (última run OK)

- Cliente F43 via `POST /api/clientes`
- Matriz via `POST /api/whatsapp-matrizes`
- Mensagem → ticket criado (`ticketCriado=true`)
- Contato resolvido (`contatoId` > 0 no `GET /api/tickets/{numero}`)

## 8. Origem

`origemTicket = RECEPTIVO_WHATSAPP`; `clienteId`, `contatoId`, `whatsappMatrizId` validados no helper.

## 9–11. Chats / Network / Console

Playwright: Login → Chats → busca ticket → painel Cliente/Contato/Entrada/Chamado; sem Carteira/Conexão/Revenda; network sem `/api/carteiras` nem `/uploads/conexoes`; console sem erro crítico (403 asset ignorado).

## 12. Testes

| Comando | Resultado |
|---------|-----------|
| `mvn test` | OK |
| Vitest | 219/219 |
| `mvn package -DskipTests` | OK se jar parado; lock se JVM na 8080 |
| Playwright F43 | **1 passed** |
| HTTP `/` | 200 com jar DEV |

## 13. Bugs

Nenhum de negócio. Spec: sidebar intercepta aba Fila → click `force` + estabilizar lista antes do card.

## 14. Riscos

Jar Windows lock; card Chats detach em refresh (mitigado `force` + pausa curta); global-setup legado 214/220 ainda roda.

## 15. Próximo

Relatórios filtro `RECEPTIVO_WHATSAPP`; CSV/PDF smoke; doc/README legado.

## Comando

```powershell
cd e2e
$env:E2E_SKIP_WEB_SERVER = '1'
npm test -- smoke-receptivo-whatsapp-final.spec.ts
```

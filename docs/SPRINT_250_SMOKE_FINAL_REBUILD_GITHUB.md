# Sprint 250 — Smoke final visual + rebuild + pendência GitHub

Log: **Sprint 250 — Smoke final visual + rebuild + pendência GitHub**.

## Rebuild

- `mvn clean package -DskipTests` — OK
- JAR reiniciado em **8080**
- `index.html` servido com `?v=sprint249` (8 folhas principais)

## Smoke visual (browser)

| Tela | Título h1 (cor) | Azul legado |
|------|-----------------|-------------|
| Login | Corporativo (isolado) | N/A |
| Dashboard | `rgb(226,232,240)` escuro / `rgb(15,47,58)` claro | Não |
| Clientes, Tickets, Relatórios, Indicadores, Config, Atendentes, Auditoria, Abrir ticket, Perfil | Teal/claro corporativo | Não |
| Chats | Layout sem h1 clássico | N/A |
| Topbar avatar | `rgb(15, 47, 58)` | Não |
| Card valor dashboard | `rgb(0, 255, 170)` | Não |
| Menu lateral Perfil | `display: none` (246) | OK |

## GitHub

Push: `remote: Repository not found` — ver seção abaixo.

## Próximo comando push

```powershell
cd "C:\Users\João Falcone\Desktop\Sistema\suporte-tickets"
git push -u origin main
```

Pré-requisitos: repositório `https://github.com/robertaobolivia-sudo/Sistema` criado (vazio ou com histórico compatível) e `git credential` autenticado na conta com permissão de escrita.

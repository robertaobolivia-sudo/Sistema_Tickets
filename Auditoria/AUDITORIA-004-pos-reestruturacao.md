# AUDITORIA-004 — Estado pós-reestruturação (verdade vigente)

**Fonte principal do projeto:** [README_MESTRE.md](../README_MESTRE.md) (leia primeiro).  
Este arquivo complementa com matrizes, riscos e referências de validação.

**Processo:** AUDITORIA-004  
**Data:** 2026-05-24  
**Estado:** `REESTRUTURACAO_CONCLUIDA` (marco F41; validado F42–F46)

> **Para outra IA ou operador:** comece por **`README_MESTRE.md`**, depois este documento + `AUDITORIA-004-context.json`.  
> `AUDITORIA-001` / `002` / `003` (relatórios e JSON antigos) são **histórico pré-reestruturação** — não descrevem o runtime de hoje.

---

## 1. Resumo executivo

A reestruturação **Cliente contratante → Contato atendido → WhatsApp Matriz → Ticket** está concluída no núcleo operacional (F24–F41). F42–F45 validaram E2E (manual, receptivo, relatórios/CSV/PDF, suite única). F46 padronizou a esteira oficial (`validar-pos-reestruturacao.ps1` + `npm run test:pos-reestruturacao`).

Legados **Carteira/Conexão/ContatoCliente/TicketEtiqueta/ticket.conexao** não comandam Ticket, Cliente, Chats nem Relatórios operacionais. Permanecem **isolados** em Config global, `/api/carteiras` e documentação/backups.

WhatsApp **provider real** continua **pendência planejada** (`NoopWhatsAppMessageSender` — não é bug).

---

## 2. Estado atual do domínio

- **Cliente** = contratante F5 (sem `carteira_id` operacional).
- **Contato** = pessoa atendida (WhatsApp, etiquetas `ContatoEtiqueta`).
- **WhatsApp Matriz** = número receptor do Cliente.
- **Ticket** = atendimento; vínculo por `clienteId` + `contatoId` + `origemTicket` + `whatsappMatrizId` (receptivo).
- Ticket ativo: **Cliente + Contato** (não reutilizar ativo de outro Contato).

---

## 3. Modelo oficial atual

```
Cliente contratante
  → Contato atendido
  → WhatsApp Matriz (entrada receptiva)
  → Ticket
```

| Conceito | Regra |
|----------|--------|
| **Origem ticket** | `ATIVO_MANUAL` \| `RECEPTIVO_WHATSAPP` |
| **Etiquetas** | `ContatoEtiqueta` (com `contatoId`) |
| **Chats** | Painéis Cliente + Contato + Entrada + Chamado; arte do Cliente ou gradiente; **sem** Carteira/Conexão/Revenda/Subcliente |
| **Relatórios / CSV** | `origemTicket`, cliente, contato, status, datas; **sem** conexão/carteira operacional |
| **PDF** | Cliente/Contato/Origem; **sem** rótulo Conexão operacional (`TicketPdfServiceF26Test`) |

Detalhe de origem: `Auditoria/REGRA-ORIGEM-TICKET.md`.

---

## 4. O que foi removido (runtime)

| Legado | Estado |
|--------|--------|
| `ticket.conexao` / coluna `tickets.conexao` | DROP F29 + backup |
| `Cliente.carteira_id` / `clientes.carteira_id` | DROP F40 |
| `ContatoCliente` / `contatos_clientes` | DROP F38 |
| `tickets.contato_solicitante_id` | Removido do fluxo |
| `TicketEtiqueta` / `ticket_etiquetas` | DROP F34; runtime Chats usa Contato |
| Fallback Carteira/Conexão no Chats | Removido (F19–F30) |
| Fallback ContatoCliente na abertura manual | Removido (F38) |
| Fallback TicketEtiqueta no Chats | Removido (F34) |
| Dashboard por Conexão | Substituído por Cliente (F26) |
| CSV/PDF com Conexão operacional | Removido (F25–F28) |
| Filtro `conexao` em busca/relatório | Removido (F25) |

---

## 5. O que ficou isolado (ainda existe, fora do núcleo)

| Item | Papel |
|------|--------|
| Entidade/API **Carteira** | Config global; `/api/carteiras` |
| **Configurações → Conexões/Revendas** | Parâmetros legados globais |
| `uploads/conexoes` | Arte/config histórica |
| Patches backup F29, F34, F38, F40 | Migração DEV; não regra de negócio |
| Docs históricas (`PLANO_*`, sprints antigas) | Referência |

**Regra:** Carteira/Conexão **não** comandam Ticket, Cliente, Chats, Relatórios nem Contato no fluxo operacional atual.

---

## 6. O que continua pendente

| Item | Classificação |
|------|----------------|
| **WhatsApp provider real** | Pendência planejada; entrada simulada OK (`POST /api/integracoes/whatsapp/mensagens`, F43) |
| **C04** race/concorrência Chats | Risco técnico futuro; fora do escopo F19–F46 |
| Smoke visual manual pós-deploy | Recomendado, não bloqueante |
| CI gate único F46 | Próximo bloco |
| Limpeza doc/README com termos legados | Contínuo |

---

## 7. Matriz antes × depois

| Área | Antes (pré F41) | Depois (atual) |
|------|-----------------|----------------|
| Cliente | Carteira/revenda no modelo | Cliente puro contratante |
| Abertura manual | ContatoCliente / solicitante | `contatoWhatsappId` → `ATIVO_MANUAL` |
| Entrada WhatsApp | Conexão + webhook solto | Matriz + `RECEPTIVO_WHATSAPP` |
| Chats painel | Carteira, Conexão, Revenda | Cliente, Contato, Entrada, Chamado |
| Etiquetas Chats | TicketEtiqueta fallback | ContatoEtiqueta |
| Relatórios | Conexão/carteira em filtros/CSV | `origemTicket` + cliente/contato |
| Dashboard | Pendências por conexão | Por cliente |
| E2E oficial | Specs dispersos F42–F44 | `smoke-pos-reestruturacao.spec.ts` (F45) |

---

## 8. Matriz de fluxos atuais

| Fluxo | Entrada | Validação |
|-------|---------|-----------|
| Login / shell | UI | F45 E2E |
| Dashboard | UI | F45 |
| Cliente + Contato CRUD | UI + API | F45, Vitest clientes/contatos |
| Ticket ATIVO_MANUAL | UI ou `POST /api/tickets` | F42, F45 |
| Ticket RECEPTIVO_WHATSAPP | `POST /api/integracoes/whatsapp/mensagens` | F43, F45 |
| Chats atendimento | UI | F45; sem `/api/carteiras` no painel |
| Relatórios por origem | UI + `GET /api/tickets/busca` | F44, F45 |
| CSV export | `GET /api/tickets/relatorios/csv` | F44, F45 |
| PDF ticket | `GET /api/tickets/{n}/pdf` | F44, F45 |
| Config Conexões/Revendas | UI isolada | F45 (seção visível, não operacional Ticket) |

---

## 9. Matriz de riscos atualizada

| ID | Antes | Agora |
|----|-------|-------|
| **C01** | POST webhook sem auth | **Mitigado** — integração com sessão/headers; revisar exposição pública em deploy |
| **C02** | Ticket ativo por cliente+telefone genérico | **Mitigado** — ativo por Cliente + Contato (F1–F8, consolidado F41) |
| **C03** | Fallback solicitante / TicketEtiqueta | **Mitigado** — Contato obrigatório; etiquetas no Contato |
| **C04** | Race Chats (seleção/lista) | **Aberto** — fora do escopo; monitorar em refator Chats |
| **M01** | Dashboard agregação em memória | **Reavaliar** se volume crescer; métricas por Cliente |
| **M02** | Auth headers/token local | **Atual** — padrão vigente; não regressão F46 |
| **M03** | Dupla fonte arte Chats (Carteira vs Cliente) | **Mitigado** — arte/gradiente Cliente |
| **M04** | Webhook legado | **Reavaliar** em sprint provider; usar integração documentada |
| **M05** | pageKey permissão desconhecida | **Revisar** se nova rota sem RBAC |

---

## 10. Estado do WhatsApp real

- **Implementação:** `NoopWhatsAppMessageSender` — adapter temporário **planejado**.
- **Não** classificar como defeito de produção para tickets simulados.
- **Entrada validada:** `POST /api/integracoes/whatsapp/mensagens` (F43, F45).
- **Próximo bloco:** provider real (envio + webhook assinado), alinhado ao addendum histórico AUDITORIA-002 (quando existir no repositório).

---

## 11. Estado dos testes

| Camada | Comando | Estado referência F46 |
|--------|---------|------------------------|
| Backend | `mvn test` | OK |
| Frontend | `cd src/main/resources/static/js && npm test` | 219/219 |
| Build | `mvn package -DskipTests` | OK após `stop-java-8080.ps1` |
| HTTP | `GET /` → 200 | OK com app no ar |

---

## 12. Estado do E2E

| Spec | Papel |
|------|--------|
| `e2e/tests/smoke-pos-reestruturacao.spec.ts` | **Oficial** pós-reestruturação (F45) |
| `smoke-reestruturacao-final.spec.ts` | Histórico F42 (ainda útil isolado) |
| `smoke-receptivo-whatsapp-final.spec.ts` | Histórico F43 |
| `smoke-relatorios-csv-pdf-origem.spec.ts` | Histórico F44 |
| `chats-encerramento-*.spec.ts` | Regressão encerramento/pesquisa (214+) |

Massa F45: `e2e/.massa-pos-reestruturacao.json` (recriada a cada run do spec).

Proteções E2E F45: sem `conexao`/`carteira`/`contatoSolicitanteId` no JSON ticket; Chats sem `/api/carteiras` e `/uploads/conexoes`; CSV sem coluna Conexão operacional.

---

## 13. Comando oficial de validação (F46)

```powershell
# Raiz do projeto
.\scripts\validar-pos-reestruturacao.ps1

# Dois terminais (Windows estável)
.\scripts\start-dev-server.ps1
.\scripts\validar-pos-reestruturacao.ps1 -NoAutoStartServer
```

Documentação: `docs/SPRINT_F46_VALIDACAO_OFICIAL_POS_REESTRUTURACAO.md`.

---

## 14. Próximos blocos recomendados

1. **CI/PR:** gate `validar-pos-reestruturacao.ps1 -NoAutoStartServer` + serviço MySQL.
2. **Provider WhatsApp real** (bloco dedicado; não misturar com limpeza doc).
3. **C04** se Chats ganhar refator de estado/lista.
4. **Deprecar** specs E2E F42–F44 quando F45 estável N sprints.
5. **README raiz / docs:** marcar termos legados como histórico.

---

## 15. Instrução para próxima IA

1. Ler **`README_MESTRE.md`** (fonte central).  
2. Ler **`Auditoria/AUDITORIA-004-context.json`**.  
3. Ler **este arquivo** (AUDITORIA-004).  
4. Para validação: `docs/SPRINT_F46_*` e `e2e/README.md`.  
5. **Não** tomar decisões de runtime só com AUDITORIA-001/002/003.  
6. **Não** reintroduzir legados da README_MESTRE §31 no fluxo operacional.

---

## Referências F41–F46

| Sprint | Documento |
|--------|-----------|
| F41 | `Auditoria/REESTRUTURACAO-F41-SMOKE.md` |
| F42 | `docs/SPRINT_F42_PLAYWRIGHT_SMOKE_FINAL.md` |
| F43 | `docs/SPRINT_F43_E2E_RECEPTIVO_WHATSAPP.md` |
| F44 | `docs/SPRINT_F44_RELATORIOS_CSV_PDF_ORIGEM.md` |
| F45 | `docs/SPRINT_F45_SUITE_E2E_POS_REESTRUTURACAO.md` |
| F46 | `docs/SPRINT_F46_VALIDACAO_OFICIAL_POS_REESTRUTURACAO.md` |
| Histórico | `docs/HISTORICO_REESTRUTURACAO_CLIENTE_CONTATO_WHATSAPP_TICKET.md` |

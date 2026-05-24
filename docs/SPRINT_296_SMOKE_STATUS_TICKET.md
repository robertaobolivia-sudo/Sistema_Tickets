# Sprint 296 — Smoke máquina de status do ticket

## Operação

1. Java encerrado → `mvn package -DskipTests` → `java -jar target/suporte-tickets-1.0.0.jar`
2. `GET http://localhost:8080/` → **200**
3. Script: `scripts/sprint296-smoke-status.ps1`

## Tickets usados

| Ticket | Uso |
|--------|-----|
| **TK-000335** | Fluxo completo ABERTO → … → RESOLVIDO → reabertura ABERTO |
| **TK-000336** | EM_ATENDIMENTO → INDEVIDO + bloqueios |

Telefone smoke único por execução (`5511963978xxx`) para evitar conflito de ticket ativo por contato.

## Transições válidas (API)

- ABERTO → EM_ATENDIMENTO (`analistaId` no body)
- EM_ATENDIMENTO → AGUARDANDO_CLIENTE
- AGUARDANDO_CLIENTE → EM_ATENDIMENTO
- EM_ATENDIMENTO → RESOLVIDO via `PUT .../encerrar`
- RESOLVIDO → ABERTO via `PUT .../reabrir`
- EM_ATENDIMENTO → INDEVIDO via `PUT .../classificar-indevido` (`confirmacao: true`)

## Bloqueios testados (HTTP 400 + mensagem)

| Tentativa | Mensagem (trecho) |
|-----------|-------------------|
| PUT status → RESOLVIDO | `Use o endpoint de encerramento para resolver o ticket.` |
| PUT status → INDEVIDO | `Use o endpoint de classificacao indevido com confirmacao do analista.` |
| PUT RESOLVIDO → EM_ATENDIMENTO | `Transicao de status nao permitida: RESOLVIDO -> EM_ATENDIMENTO (contexto: ATUALIZACAO_MANUAL).` |
| Encerrar ticket já RESOLVIDO | `Somente tickets ativos podem ser encerrados.` |
| Reabrir INDEVIDO | `Transicao de status nao permitida: INDEVIDO -> ABERTO (contexto: REABERTURA).` |
| PUT INDEVIDO → EM_ATENDIMENTO | `Transicao de status nao permitida: INDEVIDO -> EM_ATENDIMENTO (contexto: ATUALIZACAO_MANUAL).` |

## Smoke Chats (manual)

1. Login → Chats → abrir **TK-000335** (ou ticket ativo de teste).
2. Iniciar atendimento / aguardar cliente / retomar — status deve seguir transições acima.
3. Encerrar pelo fluxo normal (modal encerramento).
4. Tentar atalho inválido só se existir na UI (não deve expor PUT RESOLVIDO direto).

## Resultado

**Smoke API: OK** (2026-05-22).

## Próximo passo

Smoke UI Chats em ticket real da operação; opcional teste CANCELADO → reabertura.

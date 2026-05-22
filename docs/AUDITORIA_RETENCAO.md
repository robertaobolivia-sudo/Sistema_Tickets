# Retenção e arquivamento básico — auditoria_eventos (Sprint 69)

## Objetivo

Controlar o volume da tabela `auditoria_eventos` com regras simples de retenção, sem exclusão automática.

## Endpoints (somente ADMIN + headers de sessão)

| Método | Caminho | Parâmetros |
|--------|---------|------------|
| GET | `/api/auditoria/eventos/contar-antigos` | `antesDe` (ISO date `YYYY-MM-DD`) |
| DELETE | `/api/auditoria/eventos/antigos` | `antesDe`, `confirmar` (deve ser `true`) |

Headers obrigatórios: `X-Analista-Id`, `X-Analista-Token`.

## Regra de corte

- Considera-se “antigo” todo evento com `dataHora` **estritamente anterior** ao início do dia `antesDe` (`antesDe` às 00:00:00).
- Exemplo: `antesDe=2026-01-15` remove/conta registros de 2026-01-14 23:59:59 para trás.

## Confirmação na exclusão

- `DELETE` sem `confirmar=true` retorna HTTP 400 com mensagem amigável; nenhum registro é apagado.
- Não há job agendado: a limpeza é sempre manual via API (ou UI ADMIN).

## Proteção dos últimos 30 dias

- Na exclusão, `antesDe` não pode ser posterior a `hoje − 30 dias`.
- Contagem (`GET contar-antigos`) não aplica essa trava — permite planejar volume antes de escolher uma data segura.

## O que não está no escopo

- Scheduler, arquivamento em arquivo, exportação/backup automáticos, soft delete, retenção por tenant.

## Consulta existente

`GET /api/auditoria/eventos`, exportação CSV e gravação de novos eventos permanecem inalterados.

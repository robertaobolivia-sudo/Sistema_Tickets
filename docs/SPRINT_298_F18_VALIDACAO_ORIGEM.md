# Sprint F18 — Validação origem ticket (F17 + Relatórios)

## Subir app (DEV)

```powershell
cd "C:\Users\João Falcone\Desktop\Sistema\suporte-tickets"
$proc = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty OwningProcess
if ($proc) { Stop-Process -Id $proc -Force }
mvn package -DskipTests
java -jar target\suporte-tickets-1.0.0.jar
```

## HTTP 200

```powershell
(Invoke-WebRequest -Uri "http://localhost:8080/" -UseBasicParsing).StatusCode
```

## Log F17

Procurar no console:

`Sprint F17: backfill origem_ticket — receptivo=`

(omitido se 0 linhas atualizadas — idempotente)

## Amostra SQL (MySQL)

```sql
SELECT origem_ticket, COUNT(*) AS qtd
FROM tickets
GROUP BY origem_ticket;

SELECT id, numero_ticket, origem_ticket, whatsapp_matriz_id, contato_id
FROM tickets
WHERE origem_ticket IS NULL
LIMIT 20;
```

## Relatório

- UI: filtro **Origem do ticket** → Gerar / Exportar CSV.
- API: `GET /api/tickets?origemTicket=RECEPTIVO_WHATSAPP` (demais filtros opcionais).
- CSV: `GET /api/tickets/relatorios/csv?origemTicket=ATIVO_MANUAL`

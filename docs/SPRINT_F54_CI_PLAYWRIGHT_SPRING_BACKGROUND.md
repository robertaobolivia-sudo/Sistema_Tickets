# Sprint F54 — Spring Boot em background no CI (Playwright F45)

## Problema

`webServer` do Playwright subia o JAR no Actions com exit code 1 (env MySQL / timeout / log invisível).

## Solução (estratégia B)

1. `mvn package -DskipTests`
2. `nohup java -jar target/suporte-tickets-1.0.0.jar` → `spring-boot.log`
3. Loop `curl` até HTTP 200 em `http://localhost:8080/`
4. `E2E_SKIP_WEB_SERVER=1 npm run test:pos-reestruturacao`
5. Em falha: artifact `gate-pos-reestruturacao-debug` (log Spring, Playwright, surefire)

Arquivo: `.github/workflows/pos-reestruturacao.yml`

## Local (igual ao CI)

```powershell
.\scripts\start-dev-server.ps1
cd e2e
$env:E2E_SKIP_WEB_SERVER='1'
npm run test:pos-reestruturacao
```

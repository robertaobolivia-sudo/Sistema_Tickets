# Sprint F55 — Boot Spring no CI (log imediato)

## Causa provável (spring-boot.log típico)

- `Communications link failure` / `Access denied for user 'root'@'...'`  
  → JAR usava `application.properties` (**senha `123456`**) enquanto MySQL do Actions usa **`e2e_ci_root`**.
- Processo **morria** no boot; wait só fazia `curl` até timeout sem ver PID morto.

## Ajuste workflow

`.github/workflows/pos-reestruturacao.yml`:

1. Wait TCP **3306** antes do Java.
2. `java -jar` com **`-Dspring.datasource.*`** explícitos + `env` (mesmo URL/user/password do job).
3. Desliga seeds DEV no CI: `app.dev.clientes-massa-guard=false`, sprint253/256 false.
4. Após 5s: se PID morto → fail + tail log no Actions.
5. Loop HTTP: `kill -0 PID`; a cada 15 tentativas tail 40 linhas; morte → tail 400 linhas.

## Validar

Re-run **Gate pos-reestruturacao**. Se falhar, artifact `gate-pos-reestruturacao-debug` → `spring-boot.log`.

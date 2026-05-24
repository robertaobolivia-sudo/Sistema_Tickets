# Sprint F56 — Profile `ci` para boot do Gate

## Objetivo

Spring Boot sobe rápido no GitHub Actions: sem massa DEV, sem seeds opcionais pesados, SQL silencioso, admin mínimo para F45.

## Profile

```bash
java -jar target/suporte-tickets-1.0.0.jar --spring.profiles.active=ci
```

Arquivo: `src/main/resources/application-ci.properties`

Flags principais:

| Propriedade | CI |
|-------------|-----|
| `app.ci` | `true` |
| `app.dev.seeds.enabled` | `false` |
| `app.dev.massas.enabled` | `false` |
| `app.dev.clientes-massa-guard` | `false` |
| `spring.jpa.show-sql` | `false` |

## Seeds

- **Desligados no CI** (`app.dev.seeds.enabled=false`): `Sprint94AnalistasSeedConfig`, `Sprint95AnalistasOficiaisSeedConfig`, `Sprint951LimpezaAtendentesSeedConfig`, `AnalistaSeedConfig`.
- **Ligados no CI**: `CategoriaSeedConfig`, `Sprint271EtiquetasOperacionaisSeedConfig`, patches F29/F32/F34/F38/F40 (idempotentes).
- **Admin CI**: `CiMinimalAdminSeedConfig` — `robertaobolivia@gmail.com`, senha `SMOKE_ADMIN_SENHA` / `app.ci.admin-password` / fallback `@Hipcom123789`.

## Workflow

`.github/workflows/pos-reestruturacao.yml` — `java -Dspring.profiles.active=ci`; wait HTTP com grep de `Started`, `Tomcat`, `APPLICATION FAILED`, `Exception`, `ERROR` em falha.

## Local DEV

Sem profile `ci`: `app.dev.seeds.enabled=true` (default), comportamento DEV preservado.

## Smoke

```text
mvn test
cd src/main/resources/static/js && npm test
mvn package -DskipTests
# com MySQL Actions-like:
java -jar target/suporte-tickets-1.0.0.jar --spring.profiles.active=ci
curl -fsS http://localhost:8080/
```

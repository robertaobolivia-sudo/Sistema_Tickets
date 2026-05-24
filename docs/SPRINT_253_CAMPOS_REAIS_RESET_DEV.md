# Sprint 253 — Campos reais Cliente + reset DEV

## Campos (colunas)

| Campo UI | Coluna / API |
|----------|----------------|
| Razão social | `razao_social` (+ espelho `empresa`) |
| Responsável | `responsavel` (+ `nome` obrigatório) |
| WhatsApp | `whatsapp` (+ `telefone`) |
| CNPJ, IE, e-mail, endereço, CEP, cidade, UF, site, horário | colunas dedicadas |
| Observações | apenas texto livre |

Leitura legada: prefixos `IE:`, `CEP:`, `Site:`, `Horário:` em `observacoes` ainda são interpretados na API até migração manual dos registros antigos.

## Reset DEV

- Propriedade: `app.sprint253.dev-reset=true` (somente local).
- Runner: `Sprint253DevMassaSeedConfig` — apaga dados operacionais (tickets, contatos, clientes, matriz, etc.), **não** apaga analistas, perfis, categorias/motivos/SLA.
- Massa: Rocha Mendes, Status Automação, FastComércio, Fênix — 3 tickets cada (ABERTO, EM_ATENDIMENTO, RESOLVIDO) + contatos + WhatsApp matriz.
- Admin preservado: `robertaobolivia@gmail.com`.

**Após aplicar a massa**, deixar `app.sprint253.dev-reset=false` para não repetir o wipe a cada restart.

## Backup

`C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_253_Campos_Reais_Reset_Dev`

## Execução

```text
mvn clean install
cd src/main/resources/static/js && npm test
java -jar target/suporte-tickets-1.0.0.jar
```

HTTP: `http://localhost:8080/` → 200

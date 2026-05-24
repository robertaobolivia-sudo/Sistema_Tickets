# Sistema de Gestão de Tickets de Suporte Técnico Remoto

Repositório: [robertaobolivia-sudo/Sistema](https://github.com/robertaobolivia-sudo/Sistema).

> **Este README é apenas uma entrada rápida.**  
> A documentação completa e vigente está em **[README_MESTRE.md](./README_MESTRE.md)**.

## Início rápido

| Ação | Comando / link |
|------|----------------|
| Ler documentação | [README_MESTRE.md](./README_MESTRE.md) |
| Subir app (dev) | `.\scripts\start-dev-server.ps1` |
| Validar pós-reestruturação | `.\scripts\validar-pos-reestruturacao.ps1` |
| **Required check CI** | PR precisa de `Gate pos-reestruturacao` verde — ver [README_MESTRE.md](./README_MESTRE.md) §30 |
| E2E oficial | `cd e2e` → `npm run test:pos-reestruturacao` |
| Auditoria vigente | [Auditoria/AUDITORIA-004-pos-reestruturacao.md](Auditoria/AUDITORIA-004-pos-reestruturacao.md) |

**Requisitos:** Java 21, Maven 3.6+, MySQL 8, Node 18+ (testes).

Clone: `git clone https://github.com/robertaobolivia-sudo/Sistema.git` → pasta `suporte-tickets` (ou raiz conforme layout do repo).

Detalhes de instalação, schema legado e exemplos antigos de API foram **retirados daqui** para evitar conflito com o modelo atual — ver **README_MESTRE.md** §28–§32.

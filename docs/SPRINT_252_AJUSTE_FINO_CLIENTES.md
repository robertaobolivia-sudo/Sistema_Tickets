# Sprint 252 — Ajuste fino visual e reestruturação Clientes

Log: **Sprint 252 — ajuste fino visual e reestruturação Clientes**.

## Visual

- Botões primários no tema escuro: fundo `#0f2f3a` (sidebar), borda discreta, hover suave.
- Verde `#00FFAA` reservado a destaques pontuais (métricas, indicadores ativos).
- Chats: contornos e abas com `--corp-outline-accent` menos agressivo.

## Clientes

- Submenu: **Listagem** | **Novo cadastro**.
- Listagem: só lista, **10 itens/página**, paginação Primeira/Anterior/Próxima/Última (frontend).
- Novo cadastro: só formulário (campos IE/CEP/Site/Horário em `observacoes` estruturado, sem migration).
- Editar na listagem abre modo Novo cadastro com dados carregados.

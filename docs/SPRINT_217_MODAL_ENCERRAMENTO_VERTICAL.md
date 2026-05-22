# Sprint 217 — Modal de encerramento (layout vertical)

## Objetivo

Aderir à especificação UX: fluxo em **coluna única**, pesquisa em **linha compacta**, ações **centralizadas**, sem scroll interno em desktop.

## Ordem visual

1. Categoria  
2. Subcategoria  
3. Motivo  
4. Comentário  
5. Pesquisa (pergunta + Não enviar / Sim, enviar na mesma linha)  
6. Cancelar / Confirmar (centralizados)

## Arquivos

- `static/index.html` — `encerramento-stack`, `encerramento-pesquisa-inline`
- `static/css/modals.css` — estilos Sprint 217

## data-testid

Inalterados (Sprint 216).

## Testes (2026-05-21)

| Verificação | Resultado |
|-------------|-----------|
| Vitest | 137 passed |
| Playwright | 1 passed |
| HTTP 200 | OK |
| Maven | N/A (sem Java) |

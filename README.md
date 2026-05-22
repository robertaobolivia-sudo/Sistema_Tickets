# Sistema de Gestão de Tickets de Suporte Técnico Remoto

Repositório GitHub: [robertaobolivia-sudo/Sistema](https://github.com/robertaobolivia-sudo/Sistema) (Sprint 248).

Sistema completo para gerenciamento de tickets de suporte técnico remoto com integração de webhooks.

## Clone e execução em outra máquina

### Requisitos

| Componente | Versão sugerida |
|------------|-----------------|
| Java JDK | 21 |
| Maven | 3.6+ |
| MySQL | 8.x |
| Node.js | 18+ (Vitest no front e Playwright em `e2e/`) |
| Git | qualquer recente |

### 1. Clonar

```bash
git clone https://github.com/robertaobolivia-sudo/Sistema.git
cd Sistema
```

A raiz do clone contém `pom.xml`, `src/`, `e2e/` e `docs/`.

### 2. MySQL

```sql
CREATE DATABASE suporte_tickets CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Tabelas: criadas/atualizadas pelo Hibernate (`spring.jpa.hibernate.ddl-auto=update` no dev).

### 3. Configurar banco

Edite `src/main/resources/application.properties` (ou variáveis de ambiente / perfil local **não commitado**):

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Use credenciais da sua máquina; não commite senhas reais de produção.

### 4. Build e subir na porta 8080

```bash
mvn clean install
java -jar target/suporte-tickets-1.0.0.jar
```

Alternativa: `mvn spring-boot:run`

Confirme: [http://localhost:8080/](http://localhost:8080/) retorna **HTTP 200**.

### 5. Testes JavaScript (Vitest)

```bash
cd src/main/resources/static/js
npm install
npm test
```

### 6. E2E (Playwright)

```bash
cd e2e
npm ci
npx playwright install chromium
```

Defina no ambiente (não versionar):

- `SMOKE_ADMIN_EMAIL`
- `SMOKE_ADMIN_SENHA`

Com o JAR já em 8080:

```powershell
$env:E2E_SKIP_WEB_SERVER = '1'
npm test
```

Detalhes: `e2e/README.md`. CI: `.github/workflows/e2e.yml` (MySQL service + `mvn package` + Playwright).

### 7. Smoke local rápido

- Login na UI
- Dashboard, Clientes, Tickets, Chats
- Scripts em `scripts/` (PowerShell) conforme cenário

Documentação da reestruturação: `docs/HISTORICO_REESTRUTURACAO_CLIENTE_CONTATO_WHATSAPP_TICKET.md`.

## 📋 Funcionalidades

- ✅ Recebimento automático de tickets via API REST/Webhook
- ✅ Geração de número sequencial automático (TK-000001, TK-000002, etc.)
- ✅ Cadastro automático de clientes
- ✅ Relacionamento de clientes com carteiras de atendimento
- ✅ Registro de data/hora de abertura
- ✅ Armazenamento de mensagem inicial do ticket
- ✅ Controle de status dos tickets
- ✅ Busca de tickets por número

## 🛠️ Tecnologias Utilizadas

- **Java 21** - Linguagem de programação
- **Spring Boot 3.3.0** - Framework web
- **Spring Data JPA** - Persistência de dados
- **MySQL 8** - Banco de dados
- **Maven** - Gerenciador de dependências
- **Lombok** - Redução de boilerplate
- **Spring Validation** - Validação de dados

## 📦 Requisitos

- Java 21 JDK instalado
- Maven 3.6+ instalado
- MySQL 8 instalado e rodando
- Git (opcional)

## 🚀 Instalação e Configuração

### 1. Criar Banco de Dados

Conecte-se ao MySQL e execute:

```sql
CREATE DATABASE suporte_tickets;
USE suporte_tickets;

-- As tabelas serão criadas automaticamente pelo Hibernate
```

### 2. Configurar Credenciais

Abra o arquivo `src/main/resources/application.properties` e ajuste:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/suporte_tickets
spring.datasource.username=root
spring.datasource.password=123456
```

### 3. Compilar o Projeto

```bash
mvn clean compile
```

### 4. Executar a Aplicação

```bash
mvn spring-boot:run
```

A aplicação iniciará em: `http://localhost:8080`

## �️ Interface Web (Sprint 3)

Acesse a interface pelo navegador em:

`http://localhost:8080`

A interface permite:

- Visualizar dashboard com totais de tickets
- Filtrar tickets por status
- Criar ticket manualmente
- Atender ticket (EM_ATENDIMENTO)
- Encerrar ticket
- Ver detalhes completos do ticket

## Sprint 6 - Integracao entre Clientes e Tickets

### Objetivo

Integrar o cadastro de clientes ao fluxo de abertura e consulta de tickets, mantendo os endpoints de tickets existentes e reutilizando o visual atual da aplicacao.

### Funcionalidades implementadas

- Busca de clientes cadastrados na tela **Abrir Ticket**.
- Pesquisa por nome, telefone, empresa, CNPJ, e-mail ou carteira.
- Exibicao de resultados em tempo real durante a digitacao.
- Selecao obrigatoria de um cliente valido antes de criar ticket.
- Preenchimento automatico de:
  - Nome
  - Telefone
  - Telefone de contato
  - E-mail
  - Empresa
  - Carteira
  - Status
- Criacao do ticket usando o endpoint existente:
  - `POST /api/webhooks/tickets`
- Modal de detalhes do ticket exibindo tambem:
  - Empresa
  - Telefone de contato
  - E-mail
  - Carteira
- API de clientes para cadastro, listagem e busca:
  - `POST /api/clientes`
  - `GET /api/clientes`
  - `GET /api/clientes/buscar?termo=...`
  - `GET /api/clientes/{id}`
  - `PUT /api/clientes/{id}`
  - `DELETE /api/clientes/{id}`

### Como testar

1. Compile o projeto:

```bash
mvn clean install
```

2. Execute:

```bash
java -jar target\suporte-tickets-1.0.0.jar
```

3. Acesse:

```text
http://localhost:8080
```

4. Cadastre um cliente pela tela **Clientes** ou pela API `POST /api/clientes`.
5. Acesse **Abrir Ticket**.
6. Pesquise o cliente por nome, telefone, empresa ou CNPJ.
7. Selecione um resultado.
8. Confira os dados preenchidos automaticamente.
9. Informe canal e mensagem.
10. Clique em **Criar Ticket**.
11. Abra o ticket em **Ver detalhes** e confira os dados extras do cliente.

## Sprint 7 - Categorizacao no Encerramento do Ticket

### Objetivo

Obrigar a classificacao do ticket no encerramento, salvando grupo, subgrupo e comentario de encerramento no banco de dados.

### Funcionalidades implementadas

- Cadastro e consulta de grupos de categoria.
- Cadastro e consulta de subgrupos vinculados a um grupo.
- Exclusao logica de grupos e subgrupos (`ativo=false`).
- Validacao de duplicidade de grupo por nome.
- Validacao de duplicidade de subgrupo dentro do mesmo grupo.
- Encerramento de ticket exigindo:
  - `grupoId`
  - `subgrupoId`
  - `comentarioEncerramento`
- Validacao de grupo ativo.
- Validacao de subgrupo ativo e pertencente ao grupo informado.
- Retorno dos campos de categorizacao em `GET /api/tickets` e `GET /api/tickets/{numeroTicket}`.
- Modal simples na tela de Tickets para selecionar grupo, subgrupo e comentario.
- Detalhes do ticket exibindo grupo, subgrupo e comentario de encerramento.

### Novos endpoints

Grupos:

- `POST /api/grupos-categoria`
- `GET /api/grupos-categoria`
- `GET /api/grupos-categoria/{id}`
- `PUT /api/grupos-categoria/{id}`
- `DELETE /api/grupos-categoria/{id}`

Subgrupos:

- `POST /api/subgrupos-categoria`
- `GET /api/subgrupos-categoria`
- `GET /api/subgrupos-categoria/{id}`
- `GET /api/subgrupos-categoria/grupo/{grupoId}`
- `PUT /api/subgrupos-categoria/{id}`
- `DELETE /api/subgrupos-categoria/{id}`

Endpoint alterado:

- `PUT /api/tickets/{numeroTicket}/encerrar`

### Exemplo de JSON para encerrar ticket

```json
{
  "grupoId": 1,
  "subgrupoId": 2,
  "comentarioEncerramento": "Cliente orientado e problema resolvido."
}
```

### Dados iniciais

Ao iniciar a aplicacao, o sistema cria os dados abaixo somente se ainda nao existirem:

- Fiscal: NF-e, NFC-e, SAT, Impostos
- TEF: TLS, PinPad, Adquirente
- Banco de Dados: MySQL, Backup, Performance
- Sistema: Cadastro, Relatorios, Integracoes

### Como testar

1. Compile o projeto:

```bash
mvn clean install
```

2. Execute:

```bash
java -jar target\suporte-tickets-1.0.0.jar
```

3. Abra `http://localhost:8080`.
4. Crie ou use um ticket com status `ABERTO`.
5. Acesse **Tickets** e clique em **Encerrar**.
6. Tente confirmar sem grupo: o sistema deve bloquear.
7. Selecione grupo e tente confirmar sem subgrupo: o sistema deve bloquear.
8. Selecione subgrupo e tente confirmar sem comentario: o sistema deve bloquear.
9. Informe grupo, subgrupo e comentario e confirme o encerramento.
10. Confirme que o status ficou `RESOLVIDO`.
11. Clique em **Ver detalhes** e confira grupo, subgrupo e comentario.

## Sprint 8 - Historico de Interacoes do Ticket

### Objetivo

Registrar uma timeline cronologica com as principais interacoes do ticket, preservando as regras de encerramento categorizado do Sprint 7.

### Funcionalidades implementadas

- Entidade `TicketInteracao` vinculada ao ticket.
- Registro automatico de `ABERTURA` ao criar ticket.
- Registro automatico de `ENCERRAMENTO` ao encerrar ticket.
- Endpoints:
  - `GET /api/tickets/{numeroTicket}/interacoes`
  - `POST /api/tickets/{numeroTicket}/interacoes`
- Interacoes manuais dos tipos:
  - `COMENTARIO`
  - `NOTA_INTERNA`
- Visibilidades:
  - `PUBLICA`
  - `INTERNA`
- Timeline exibida no modal de detalhes do ticket.
- Formulario para adicionar nova interacao sem fechar o modal.

## Sprint 9 - Consolidacao da Tela de Detalhes do Ticket

### Objetivo

Organizar e consolidar o modal de detalhes para apoiar melhor o atendimento tecnico, reunindo dados do ticket, cliente, encerramento e historico de interacoes no mesmo local.

### Melhorias implementadas

- Modal de detalhes organizado em secoes:
  - Dados do Ticket
  - Dados do Cliente
  - Dados de Encerramento
  - Historico de Interacoes
- Dados do ticket exibindo numero, status, canal, conexao, carteira, mensagem inicial e datas.
- Dados do cliente exibindo nome, telefone, telefone de contato, e-mail, empresa, CNPJ, cidade e UF quando disponiveis.
- Dados de encerramento exibindo grupo, subgrupo, comentario e data de encerramento.
- Botao **Atualizar Historico** para recarregar apenas a timeline sem fechar o modal.
- Validacao frontend no formulario de nova interacao:
  - tipo obrigatorio
  - visibilidade obrigatoria
  - mensagem obrigatoria e nao vazia
- Bloqueio do formulario de nova interacao para tickets `RESOLVIDO` ou `CANCELADO`.
- Mensagem exibida em ticket finalizado:
  - `Ticket finalizado. Novas interações não podem ser adicionadas.`
- Inclusao de `cnpj`, `cidade` e `uf` no retorno de tickets para exibicao no modal.

### Como testar

1. Compile o projeto:

```bash
mvn clean install
```

2. Execute:

```bash
java -jar target\suporte-tickets-1.0.0.jar
```

3. Abra `http://localhost:8080`.
4. Abra detalhes de um ticket aberto e confira as secoes do modal.
5. Clique em **Atualizar Historico** e confirme que a timeline recarrega sem fechar o modal.
6. Tente salvar uma interacao sem mensagem e confirme o alerta.
7. Salve um `COMENTARIO` `PUBLICA` e confirme que a mensagem e limpa e o historico recarrega.
8. Encerre o ticket com grupo, subgrupo e comentario.
9. Abra os detalhes do ticket encerrado e confirme os dados de encerramento e o bloqueio de novas interacoes.

## Sprint 9.1 - Analistas, Fila de Atendimento e Ajuste do Fluxo

### Objetivo

Criar a estrutura inicial de analistas, vincular tickets em atendimento a um analista responsavel e ajustar o dashboard para exibir uma fila simples por analista online.

### Funcionalidades implementadas

- Entidade `Analista` com:
  - `id`
  - `nome`
  - `email`
  - `nivel`
  - `fotoUrl`
  - `online`
  - `ativo`
  - `dataCadastro`
- Seed automatico do analista padrao:
  - Nome: `Analista Teste`
  - E-mail: `analista.teste@suporte.local`
  - Nivel: `Nível 1`
  - Foto: nula, usando avatar padrao no frontend
  - Online: `true`
  - Ativo: `true`
- Vinculo de ticket com `analistaResponsavel`.
- Ao clicar em **Atender**, o ticket muda para `EM_ATENDIMENTO` e fica vinculado ao `Analista Teste`.
- Bloqueio de novo atendimento quando o ticket ja esta em atendimento com analista vinculado.
- Dashboard com area **Painel de Atendimento - Analistas Online** em formato Kanban/Card.
- Card do analista com avatar, nome, nivel, quantidade de tickets e mini cards da fila.
- Painel expandido do analista ao clicar no card, com avatar maior, nome, nivel, quantidade e lista rolavel de tickets.
- Remocao do botao **Encerrar** das tabelas do Dashboard e da pagina Tickets.
- Encerramento disponivel somente no modal de detalhes do ticket.
- Botao **Reabrir Ticket** no detalhe para tickets `RESOLVIDO` ou `CANCELADO`.
- Ao reabrir, o ticket volta para `ABERTO`, mantem os dados de encerramento anteriores e registra interacao no historico.

### Endpoints criados

- `GET /api/analistas`
- `GET /api/analistas/online`
- `GET /api/analistas/{id}/tickets`
- `GET /api/analistas/filas`
- `GET /api/dashboard/filas-analistas`
- `PUT /api/tickets/{numeroTicket}/reabrir`

### Como testar

1. Compile o projeto:

```bash
mvn clean install
```

2. Execute:

```bash
java -jar target\suporte-tickets-1.0.0.jar
```

3. Confirme que `GET /api/analistas/online` retorna `Analista Teste`.
4. Crie um ticket novo.
5. Clique em **Atender**.
6. Confirme que o ticket ficou `EM_ATENDIMENTO` e com `analistaResponsavelNome = Analista Teste`.
7. Confirme que o ticket aparece em **Analistas Online** no dashboard.
8. Confirme que **Atender** fica bloqueado para tickets em atendimento.
9. Confirme que as tabelas nao exibem mais **Encerrar**.
10. Abra detalhes do ticket e encerre pelo botao do modal.
11. Confirme que o ticket finalizado exibe **Reabrir Ticket**.
12. Reabra o ticket e confirme que o status voltou para `ABERTO`.

## �📡 Endpoints da API

### Passo 1: Criar Ticket via Webhook

**Método:** `POST`  
**URL:** `/api/webhooks/tickets`  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "cliente": "Mercado Exemplo",
  "telefone": "11999999999",
  "mensagem": "PDV não abre",
  "canal": "webhook.site",
  "conexao": "Carteira Teste"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "numeroTicket": "TK-000001",
  "cliente": "Mercado Exemplo",
  "telefone": "11999999999",
  "canal": "webhook.site",
  "conexao": "Carteira Teste",
  "mensagemInicial": "PDV não abre",
  "status": "ABERTO",
  "dataAbertura": "2026-05-18T14:30:45"
}
```

### Passo 2: Gestão de Tickets

#### 2.1 Listar Todos os Tickets

**Método:** `GET`  
**URL:** `/api/tickets`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "numeroTicket": "TK-000001",
    "cliente": "Mercado Exemplo",
    "telefone": "11999999999",
    "canal": "webhook.site",
    "conexao": "Carteira Teste",
    "mensagemInicial": "PDV não abre",
    "status": "ABERTO",
    "dataAbertura": "2026-05-18T14:30:45",
    "dataPrimeiroAtendimento": null,
    "dataEncerramento": null,
    "tmeMinutosUteis": null,
    "tmaMinutosUteis": null
  }
]
```

#### 2.2 Buscar Ticket por Número

**Método:** `GET`  
**URL:** `/api/tickets/{numeroTicket}`

**Exemplo:**
```
GET /api/tickets/TK-000001
```

**Response (200 OK):**
```json
{
  "id": 1,
  "numeroTicket": "TK-000001",
  "cliente": "Mercado Exemplo",
  "telefone": "11999999999",
  "canal": "webhook.site",
  "conexao": "Carteira Teste",
  "mensagemInicial": "PDV não abre",
  "status": "ABERTO",
  "dataAbertura": "2026-05-18T14:30:45",
  "dataPrimeiroAtendimento": null,
  "dataEncerramento": null,
  "tmeMinutosUteis": null,
  "tmaMinutosUteis": null
}
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "erro": "Ticket não encontrado: TK-999999"
}
```

#### 2.3 Atualizar Status do Ticket

**Método:** `PUT`  
**URL:** `/api/tickets/{numeroTicket}/status`  
**Content-Type:** `application/json`

**Request Body:**
```json
{
  "status": "EM_ATENDIMENTO"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "numeroTicket": "TK-000001",
  "cliente": "Mercado Exemplo",
  "telefone": "11999999999",
  "canal": "webhook.site",
  "conexao": "Carteira Teste",
  "mensagemInicial": "PDV não abre",
  "status": "EM_ATENDIMENTO",
  "dataAbertura": "2026-05-18T14:30:45",
  "dataPrimeiroAtendimento": "2026-05-18T14:35:20",
  "dataEncerramento": null,
  "tmeMinutosUteis": null,
  "tmaMinutosUteis": null
}
```

**Status Válidos:**
- `ABERTO`
- `EM_ATENDIMENTO` (preenche automaticamente `dataPrimeiroAtendimento` se nula)
- `AGUARDANDO_CLIENTE`
- `RESOLVIDO`
- `CANCELADO`

**Response (400 Bad Request) - Status Inválido:**
```json
{
  "status": 400,
  "erro": "Status inválido: STATUS_INEXISTENTE"
}
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "erro": "Ticket não encontrado: TK-999999"
}
```

#### 2.4 Encerrar Ticket

**Método:** `PUT`  
**URL:** `/api/tickets/{numeroTicket}/encerrar`

**Response (200 OK):**
```json
{
  "id": 1,
  "numeroTicket": "TK-000001",
  "cliente": "Mercado Exemplo",
  "telefone": "11999999999",
  "canal": "webhook.site",
  "conexao": "Carteira Teste",
  "mensagemInicial": "PDV não abre",
  "status": "RESOLVIDO",
  "dataAbertura": "2026-05-18T14:30:45",
  "dataPrimeiroAtendimento": "2026-05-18T14:35:20",
  "dataEncerramento": "2026-05-18T14:45:15",
  "tmeMinutosUteis": null,
  "tmaMinutosUteis": null
}
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "erro": "Ticket não encontrado: TK-999999"
}
```

#### 2.5 Listar Tickets por Status

**Método:** `GET`  
**URL:** `/api/tickets/status/{status}`

**Exemplo:**
```
GET /api/tickets/status/ABERTO
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "numeroTicket": "TK-000001",
    "cliente": "Mercado Exemplo",
    "telefone": "11999999999",
    "canal": "webhook.site",
    "conexao": "Carteira Teste",
    "mensagemInicial": "PDV não abre",
    "status": "ABERTO",
    "dataAbertura": "2026-05-18T14:30:45",
    "dataPrimeiroAtendimento": null,
    "dataEncerramento": null,
    "tmeMinutosUteis": null,
    "tmaMinutosUteis": null
  }
]
```

**Response (400 Bad Request) - Status Inválido:**
```json
{
  "status": 400,
  "erro": "Status inválido: STATUS_INEXISTENTE"
}
```

## 🧪 Testando com PowerShell (Windows)

### Criar um Ticket

```powershell
$body = @{
    cliente = "Mercado Exemplo"
    telefone = "11999999999"
    mensagem = "PDV nao abre"
    canal = "teste"
    conexao = "Carteira Teste"
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri "http://localhost:8080/api/webhooks/tickets" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

### Listar Todos os Tickets

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tickets" -Method Get
```

### Buscar um Ticket por Número

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/TK-000001" -Method Get
```

### Atualizar Status do Ticket

```powershell
$body = @{
    status = "EM_ATENDIMENTO"
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri "http://localhost:8080/api/tickets/TK-000001/status" `
    -Method Put `
    -ContentType "application/json" `
    -Body $body
```

### Encerrar um Ticket

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/TK-000001/encerrar" -Method Put
```

### Listar Tickets por Status

```powershell
# Listar tickets abertos
Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/status/ABERTO" -Method Get

# Listar tickets em atendimento
Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/status/EM_ATENDIMENTO" -Method Get

# Listar tickets resolvidos
Invoke-RestMethod -Uri "http://localhost:8080/api/tickets/status/RESOLVIDO" -Method Get
```

## 🏁 Sprint 3 - Frontend Web

A interface web está disponível em:

`http://localhost:8080`

Funcionalidades disponíveis:

- Dashboard com totais de tickets
- Lista de tickets com filtros de status
- Botões para Atender e Encerrar tickets
- Painel de detalhes do ticket
- Formulário para criação manual de ticket
- Atualização de lista com botão "Atualizar"
- Tratamento visual de erros de API

### Como testar pela interface

1. Abra o navegador em `http://localhost:8080`.
2. Confirme que os tickets `TK-000001` e `TK-000002` aparecem na tabela.
3. Use o filtro de status para exibir apenas os tickets desejados.
4. Clique em "Atender" para marcar um ticket como `EM_ATENDIMENTO`.
5. Clique em "Encerrar" para finalizar um ticket como `RESOLVIDO`.
6. Clique em "Ver detalhes" para abrir informações completas do ticket.
7. Preencha o formulário de criação para adicionar um novo ticket e confirme se a tabela é atualizada automaticamente.

## 🧪 Testando com cURL

### Criar um Ticket

```bash
curl -X POST http://localhost:8080/api/webhooks/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": "Mercado Exemplo",
    "telefone": "11999999999",
    "mensagem": "PDV não abre",
    "canal": "webhook.site",
    "conexao": "Carteira Teste"
  }'
```

### Buscar um Ticket

```bash
curl http://localhost:8080/api/webhooks/tickets/TK-000001
```

## 🗄️ Estrutura do Banco de Dados

### Tabela `carteiras`
```sql
CREATE TABLE carteiras (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(100) NOT NULL
);
```

### Tabela `clientes`
```sql
CREATE TABLE clientes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(150) NOT NULL,
  telefone VARCHAR(20),
  carteira_id INT,
  FOREIGN KEY (carteira_id) REFERENCES carteiras(id)
);
```

### Tabela `tickets`
```sql
CREATE TABLE tickets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  numero_ticket VARCHAR(20) UNIQUE NOT NULL,
  cliente_id INT NOT NULL,
  canal VARCHAR(50),
  conexao VARCHAR(100),
  mensagem_inicial TEXT,
  status VARCHAR(30) DEFAULT 'ABERTO',
  data_abertura DATETIME NOT NULL,
  data_primeiro_atendimento DATETIME,
  data_encerramento DATETIME,
  tme_minutos_uteis INT,
  tma_minutos_uteis INT,
  FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);
```

## 📁 Estrutura de Pastas

```
suporte-tickets/
├── src/
│   ├── main/
│   │   ├── java/com/suporte/tickets/
│   │   │   ├── controller/
│   │   │   │   ├── WebhookController.java
│   │   │   │   └── TicketController.java
│   │   │   ├── service/
│   │   │   │   └── TicketService.java
│   │   │   ├── repository/
│   │   │   │   ├── CarteiraRepository.java
│   │   │   │   ├── ClienteRepository.java
│   │   │   │   └── TicketRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Carteira.java
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Ticket.java
│   │   │   │   └── TicketStatus.java
│   │   │   ├── dto/
│   │   │   │   ├── TicketWebhookRequestDTO.java
│   │   │   │   ├── TicketResponseDTO.java
│   │   │   │   ├── AtualizarStatusRequestDTO.java
│   │   │   │   └── ClienteDTO.java
│   │   │   ├── config/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ErrorResponse.java
│   │   │   └── SuporteTicketsApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/suporte/tickets/
├── pom.xml
└── README.md
```

## 🔄 Fluxo de Funcionamento

1. **Recebimento de Webhook**
   - POST em `/api/webhooks/tickets` com dados do ticket

2. **Processamento**
   - Sistema busca cliente pelo telefone
   - Se não encontrar, cria novo cliente automaticamente
   - Busca ou cria carteira se informada

3. **Criação de Ticket**
   - Gera número sequencial (TK-000001, TK-000002, etc.)
   - Define status como "ABERTO"
   - Registra data/hora de abertura
   - Salva a mensagem inicial

4. **Resposta**
   - Retorna dados do ticket criado em JSON

## 📝 Regras de Negócio

- Cada ticket recebe um número único sequencial
- Clientes são criados automaticamente se não existirem
- Carteiras são criadas automaticamente se não existirem
- Status inicial de ticket é sempre "ABERTO"
- Data de abertura é automática e com hora exata

## Sprint 9.5 - Analistas Oficiais, Perfil e Foto

### Funcionalidades implementadas

- O analista **Duty Breaker** foi substituído por **João Falcone**, preservando CPF, nascimento, endereço, celular e login já cadastrado.
- Foram criados os analistas oficiais **Wesley Silva**, **Gustavo Silva** e **Michelle Falcone**.
- Foi criada a página **Perfil**, exibindo os dados cadastrais do analista logado.
- Foi criado upload local de foto de perfil em `POST /api/analistas/{id}/foto`.
- A foto salva preenche `fotoUrl` e aparece nos cards da página **Atendentes**; sem foto, o sistema mantém avatar por iniciais.
- O Dashboard agora colore o tempo de espera por faixa:
  - verde abaixo de 3 minutos;
  - amarelo de 3 a menos de 5 minutos;
  - vermelho e negrito a partir de 5 minutos, com indicador `!`.
- Os cards da página **Atendentes** exibem 2 atendimentos completos por vez e usam rolagem interna para os demais.

## 🔮 Funcionalidades Futuras

- [ ] Departamentos de atendimento
- [ ] Grupos e subgrupos de tickets
- [ ] Níveis de prioridade
- [ ] Sistema de atendentes
- [ ] Cálculo de TME (Tempo Médio de Espera)
- [ ] Cálculo de TMA (Tempo Médio de Atendimento)
- [ ] Horário útil configurável
- [ ] Pesquisa de satisfação
- [ ] Integração com WhatsApp
- [ ] Dashboards e relatórios

## 🐛 Troubleshooting

### Erro de Conexão com MySQL

```
java.sql.SQLNonTransientConnectionException: Could not get a connection, pool error
```

**Solução:** 
- Certifique-se que MySQL está rodando
- Verifique as credenciais em `application.properties`
- Verifique se o banco `suporte_tickets` foi criado

### Erro de Dependências

```
mvn clean install
```

## 📞 Suporte

Para questões ou problemas, consulte a documentação do Spring Boot em: https://spring.io/projects/spring-boot

## 📄 Licença

Este projeto é fornecido como exemplo educacional.

---

**Versão:** 1.0.0  
**Desenvolvido:** 2026

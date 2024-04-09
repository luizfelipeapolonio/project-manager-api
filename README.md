# Project Manager - API REST com Spring

Project Manager API é uma aplicação para gerenciar custos de projetos, do tipo API REST desenvolvida com **Java Spring 
Framework**, com sistema de autenticação Stateless utilizando **Spring Security** e **JWT**. 
Como 
base de dados, 
utiliza o **PostgreSQL** em conjunto com o **Spring Data JPA** e **Hibernate** como ORM. <br />
Com o Project Manager é possível criar usuários, criar workspaces, adicionar e remover usuários dos workspaces, 
criar projetos dentro dos workspaces, e adicionar tarefas nos projetos com base no valor do orçamento.

<br />

<p align="center">
    <a href="#tech">Tecnologias utilizadas</a> •
    <a href="#resources">Funções/Recursos</a> •
    <a href="#auth">Autorização e autenticação</a> •
    <a href="#requirements">Requisitos funcionais</a> •
    <a href="#business">Regras de negócio</a> •
    <a href="#endpoints">Rotas da API</a> •
    <a href="#run">Como rodar a aplicação</a> •
    <a href="#license">Licença</a> •
    <a href="#author">Autor</a>
</p>

<br />

<h2 id="tech">💻 Tecnologias utilizadas</h2>

As ferramentas que foram utilizadas na construção do projeto:
- [Java 17](https://docs.oracle.com/en/java/javase/17/)
- [Spring Boot 3 (Spring 6)](https://spring.io/projects/spring-boot#overview)
- [Spring Security 6](https://docs.spring.io/spring-security/reference/index.html)
- [Maven](https://maven.apache.org/)
- [JPA + Hibernate](https://spring.io/projects/spring-data-jpa#overview)
- [Java Bean Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html#validation-beanvalidation-overview)
- [PostgreSQL](https://www.postgresql.org/)
- [JUnit5 + Mockito](https://docs.spring.io/spring-framework/reference/testing.html)
- [JWT (JSON Web Token)](https://github.com/auth0/java-jwt)
- [Docker](https://www.docker.com/)

<h2 id="resources">🚀 Funções/Recursos</h2>

Principais recursos e funções da aplicação:

- **Autenticação e autorização:** Sistema de autenticação stateless com JWT (JSON Web Token) e autorização/proteção 
  das rotas da API baseado na role de cada usuário, feitos com o módulo Spring Security.
- **Camadas:** Divisão da aplicação em 4 camadas principais: `Model`, `Repository`, `Service` e `Controller`. Fazendo 
  com que as reponsabilidades da aplicação fiquem bem definidas e separadas, melhorando as possibilidades de 
  escalonamento e manutenibilidade.
- **Testes unitários:** Testes unitários das funções com o objetivo de assegurar que o código esteja implementado 
  corretamente, seguindo as regras de negócio e requisitos funcionais da aplicação, promovendo assim, uma maior 
  confiabilidade e manutenibilidade da aplicação. 
- **Tratamento de exceções:** Centralização do tratamento de todas as exceções da aplicação em um `Rest Controller Advice`.
- **DTO(Data Transfer Objects):** Utilização de `Java Records` como DTOs para transferência de dados entre as 
  requisições.
- **Validação:** Validação dos dados das requisições com o Hibernate/Jakarta Validation.
- **Armazenamento:** Armazenamento dos dados em um banco de dados Postgres executando em container Docker.

<h2 id="auth">🔐 Autorização e autenticação</h2>

O sistema de autenticação e autorização da aplicação foi desenvolvido com o módulo **Spring Security**, portanto, 
todas as rotas da API são protegidas por filtros de autorização baseados na role do usuário. <br />
A autenticação de um usuário no sistema ocorre por meio da validação de um token `JWT (JSON Web Token)`, que precisa 
ser enviado em todas as requisições em que autenticação é necessária para poder acessar o recurso.<br />
Cada usuário autenticado pode possuir apenas uma das 3 roles, que são:

|        Role         | Descrição                                                                                                                |
|:-------------------:|:-------------------------------------------------------------------------------------------------------------------------|
|    **_`ADMIN`_**    | Super usuário capaz de executar todas as ações de gerenciamento do sistema, assim como criar, editar e excluir recursos. |
| **_`WRITE_READ`_**  | Usuário com permissão para criar, editar, excluir e visualizar alguns recursos do sistema.                               |
|  **_`READ_ONLY`_**  | Usuário com permissão apenas de visualizar recursos do sistema.                                                          |

> ❕ A primeira vez que a aplicação é executada, um super usuário com a role `ADMIN` é criado e inserido 
automaticamente no banco de dados, para que a partir dele possam ser criados outros usuários e recursos no sistema.

<h2 id="requirements">📋 Requisitos funcionais</h2>

Os requisitos funcionais da aplicação são:

- Um usuário só pode ser excluído se não possuir nenhum workspace, projeto ou tarefa.
- Um projeto só deve existir dentro de um workspace.
- Um usuário só pode criar, atualizar, visualizar e excluir um projeto ou tarefa se for membro do workspace ao qual 
  o projeto pertence.
- Todos os projetos de um usuário devem ser excluídos quando for removido do workspace.

<h2 id="business">📝 Regras de negócio</h2>

O Project Manager é uma aplicação pensada para ser uma ferramenta de gerenciamento de custos de projetos, logo, o 
sistema foi desenvolvido de uma maneira bem específica seguindo algumas regras. São elas:

### User

- Apenas o usuário com a role `ADMIN` pode criar outros usuários.
- Apenas o usuário com a role `ADMIN` pode excluir outros usuários.
- O usuário com a role `ADMIN` pode atruibuir qualquer role para para qualquer usuário criado.
- Todos os usuários autenticados podem ver e atualizar suas informações.
- Apenas o usuário com a role `ADMIN` pode ver todos os usuários criados.

### Workspace

- Apenas o usuário com a role `ADMIN` pode criar um workspace.
- Apenas o usuário com a role `ADMIN`, e que seja o dono do workspace, pode atualizar e deletar um workspace.
- Apenas o usuário com a role `ADMIN`, e que seja o dono do workspace, pode inserir e remover membros (user) em um 
  workspace.
- Todos os usuários autenticados podem visualizar um workspace específico, desde que seja membro deste workspace.
- Apenas o usuário com a role `ADMIN`, e que seja dono do workspace, pode listar todos os membros pertencentes ao 
  workspace.

### Project

- Apenas usuários com a role `ADMIN`, que seja dono ou membro do workspace, e `WRITE_READ`, que seja membro do 
  workspace, podem criar projetos.
- Apenas o usuário com a role `ADMIN`, que seja dono do projeto e membro/dono do workspace ao qual o projeto pertence, e 
  `WRITE_READ`, que seja dono do projeto e membro do workspace ao qual o projeto pertence, podem atualizar os dados do projeto.
- Apenas o usuário com a role `ADMIN`, que seja dono do projeto ou do workspace ao qual o projeto pertence, e 
  `WRITE_READ`, que seja o dono do projeto e membro do workspace, podem excluir um projeto específico.
- Os usuários com a role `ADMIN` e `WRITE_READ` podem listar e excluir seus próprios projetos.
- Todos os usuários autenticados podem visualizar um projeto específico, desde que seja o dono ou membro do 
  workspace ao qual o projeto pertence.
- Apenas o usuário com a role `ADMIN` pode listar e excluir todos os projetos de um usuário específico.
- Todos os usuários autenticados podem visualizar todos os projetos de um workspace, desde que seja dono ou membro do 
  workspace.
- Apenas o usuário com a role `ADMIN`, e que seja dono do workspace, pode excluir todos os projetos do workspace.
- Apenas o usuário com a role `ADMIN`, e que seja dono do workspace, pode listar e excluir todos os projetos de um 
  usuário específico em um workspace específico.

### Task

- Apenas o usuário com a role `ADMIN`, que seja dono ou membro do workspace, e `WRITE_READ`, que seja membro do 
  workspace, podem criar tarefas em um projeto.
- Apenas o usuário com a role `ADMIN`, que seja dono do workspace, do projeto ou da tarefa, e `WRITE_READ`, que seja 
  dono do projeto ou da tarefa, podem excluir e atualizar os dados da tarefa.
- Todos os usuários autenticados que façam parte do workspace podem visualizar uma tarefa específica.
- Todos os usuários autenticados podem visualizar todas as tarefas de um projeto, desde que seja membro do workspace.
- Os usuários com a role `ADMIN` e `WRITE_READ` podem visualizar suas próprias tarefas.
- Apenas o usuário com a role `ADMIN`, que seja dono do workspace ou do projeto, e `WRITE_READ`, que seja dono do 
  projeto, podem excluir todas as tarefas de um projeto específico.
- Apenas o usuário com a role `ADMIN` pode listar todas as tarefas de um usuário específico.

<h2 id="endpoints">🧭 Rotas da API</h2>

### Auth

|     Tipo     | Rota                 | Descrição                                           |  Autenticação  | Autorização         |
|:------------:|:---------------------|:----------------------------------------------------|:--------------:|:--------------------|
| **_`POST`_** | `/api/auth/register` | Criar usuário [requisição/resposta](#auth-register) |      Sim       | Apenas para `ADMIN` |
| **_`POST`_** | `/api/auth/login`    | Logar usuário [requisição/resposta](#auth-login)    |      Não       | Qualquer usuário    |

<br />

### User

|      Tipo      | Rota                       | Descrição                                                        | Autenticação | Autorização                        |
|:--------------:|:---------------------------|:-----------------------------------------------------------------|:------------:|:-----------------------------------|
|  **_`GET`_**   | `/api/users/me`            | Visualizar perfil do usuário autenticado [resposta](#auth-me)    |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` |
|  **_`GET`_**   | `/api/users`               | Listar todos os usuários [resposta](#all-users)                  |     Sim      | Apenas `ADMIN`                     |
| **_`PATCH`_**  | `/api/users/{userId}`      | Atualizar dados do usuário [requisição/resposta](#update-user)   |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` |
|  **_`GET`_**   | `/api/users/{userId}`      | Visualizar perfil de um usuário específico [resposta](#get-user) |     Sim      | Apenas `ADMIN`                     |
| **_`DELETE`_** | `/api/users/{userId}`      | Excluir perfil de um usuário específico [resposta](#delete-user) |     Sim      | Apenas `ADMIN`                     |
| **_`PATCH`_**  | `/api/users/{userId}/role` | Alterar role de um usuário [requisição/resposta](#role)          |     Sim      | Apenas `ADMIN`                     |

<br />

### Workspace

|      Tipo      | Rota                                             | Descrição                                                                      | Autenticação | Autorização                                                     |
|:--------------:|:-------------------------------------------------|:-------------------------------------------------------------------------------|:------------:|:----------------------------------------------------------------|
|  **_`POST`_**  | `/api/workspaces`                                | Criar workspace [requisição/resposta](#create-workspace)                       |     Sim      | Apenas `ADMIN`                                                  |
|  **_`GET`_**   | `/api/workspaces`                                | Listar todos os workspaces do usuário autenticado [resposta](#user-workspaces) |     Sim      | Apenas `ADMIN`                                                  |
| **_`PATCH`_**  | `/api/workspaces/{workspaceId}`                  | Atualizar dados do workspace [requisição/resposta](#update-workspace)          |     Sim      | Apenas `ADMIN` dono do workspace                                |
|  **_`GET`_**   | `/api/workspaces/{workspaceId}`                  | Visualizar um workspace [resposta](#get-workspace)                             |     Sim      | Qualquer membro do workspace `ADMIN`, `WRITE_READ`, `READ_ONLY` |
| **_`DELETE`_** | `/api/workspaces/{workspaceId}`                  | Excluir um workspace [resposta](#delete-workspace)                             |     Sim      | Apenas `ADMIN` dono do workspace                                |
|  **_`GET`_**   | `/api/workspaces/{workspaceId}/members`          | Listar todos os membros do workspace [resposta](#members)                      |     Sim      | Apenas `ADMIN`                                                  |
| **_`PATCH`_**  | `/api/workspaces/{workspaceId}/members/{userId}` | Inserir um membro em um workspace [resposta](#insert-member)                   |     Sim      | Apenas `ADMIN` dono do workspace                                |
| **_`DELETE`_** | `/api/workspaces/{workspaceId}/members/{userId}` | Remover um membro de um workspace [resposta](#remove-member)                   |     Sim      | Apenas `ADMIN` dono do workspace                                |

<br />

### Project

|      Tipo      | Rota                                                          | Descrição                                                                                                                                                                     | Autenticação | Autorização                                                    |
|:--------------:|:--------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------:|:---------------------------------------------------------------|
|  **_`POST`_**  | `/api/projects`                                               | Criar projeto [requisição/resposta](#create-project)                                                                                                                          |     Sim      | Apenas `ADMIN` e `WRITE_READ` dono ou membro do workspace      |
|  **_`GET`_**   | `/api/projects`                                               | Listar todos os projetos do usuário autenticado [resposta](#user-projects)                                                                                                    |     Sim      | Apenas `ADMIN`, `WRITE_READ`                                   |
| **_`DELETE`_** | `/api/projects`                                               | Excluir todos os projetos do usuário autenticado [resposta](#delete-projects)                                                                                                 |     Sim      | Apenas `ADMIN`, `WRITE_READ`                                   |
| **_`PATCH`_**  | `/api/projects/{projectId}`                                   | Atualizar os dados de um projeto [requisição/resposta](#update-project)                                                                                                       |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do projeto                   |
|  **_`GET`_**   | `/api/projects/{projectId}`                                   | Visualizar um projeto [resposta](#get-project)                                                                                                                                |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` membro ou dono do workspace |
| **_`DELETE`_** | `/api/projects/{projectId}`                                   | Excluir um projeto [resposta](#delete-project)                                                                                                                                |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do projeto ou workspace      |
|  **_`GET`_**   | `/api/projects/owner/{ownerId}`                               | Listar todos os projetos de um usuário específico [resposta](#owner-projects)                                                                                                 |     Sim      | Apenas `ADMIN`                                                 |
| **_`DELETE`_** | `/api/projects/owner/{ownerId}`                               | Excluir todos os projetos de um usuário específico [resposta](#delete-owner-projects)                                                                                         |     Sim      | Apenas `ADMIN`                                                 |
|  **_`GET`_**   | `/api/projects/workspaces/{workspaceId}?sortingOrder={order}` | Listar todos os projetos de um workspace específico. Pode ser filtrado pelo campo priority em ordem crescente(`asc`) ou descrescente (`desc`) [resposta](#workspace-projects) |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` membro ou dono do workspace |
| **_`DELETE`_** | `/api/projects/workspaces/{workspaceId}`                      | Excluir todos os projetos de um workspace específico [reposta](#delete-workspace-projects)                                                                                    |     Sim      | Apenas `ADMIN` dono do workspace                               |
|  **_`GET`_**   | `/api/projects/workspaces/{workspaceId}/owner/{ownerId}`      | Listar todos os projetos de um usuário específico em um workspace específico [resposta](#workspace-owner-projects)                                                            |     Sim      | Apenas `ADMIN` dono do workspace                               |
| **_`DELETE`_** | `/api/projects/workspaces/{workspaceId}/owner/{ownerId}`      | Excluir todos os projetos de um usuário específico em um workspace específico [resposta](#delete-workspace-owner-projects)                                                    |     Sim      | Apenas `ADMIN` dono do workspace                               |

<br />

### Task 

|      Tipo      | Rota                              | Descrição                                                                         | Autenticação | Autorização                                                     |
|:--------------:|:----------------------------------|:----------------------------------------------------------------------------------|:------------:|:----------------------------------------------------------------|
|  **_`POST`_**  | `/api/tasks`                      | Criar task [requisição/resposta](#create-task)                                    |     Sim      | Apenas `ADMIN`, `WRITE_READ` membro ou dono do workspace        |
|  **_`GET`_**   | `/api/tasks`                      | Listar todas as tasks do usuário autenticado [resposta](#user-tasks)              |     Sim      | Apenas `ADMIN`, `WRITE_READ`                                    |
|  **_`GET`_**   | `/api/tasks/{taskId}`             | Visualizar uma task [resposta](#get-task)                                         |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` membro ou dono do workspace  |
| **_`DELETE`_** | `/api/tasks/{taskId}`             | Excluir uma task [resposta](#delete-task)                                         |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do workspace, projeto ou task |
| **_`PATCH`_**  | `/api/tasks/{taskId}`             | Atualizar uma task [requisição/resposta](#update-task)                            |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do workspace, projeto ou task |
|  **_`GET`_**   | `/api/tasks/projects/{projectId}` | Listar todas as tasks de um projeto específico [resposta](#project-tasks)         |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` membro ou dono do workspace  |
| **_`DELETE`_** | `/api/tasks/projects/{projectId}` | Excluir todas as tasks de um projeto específico [resposta](#delete-project-tasks) |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do projeto ou do workspace    |
|  **_`GET`_**   | `/api/tasks/owner/{ownerId}`      | Listar todas as tasks de um usuário específico [resposta](#owner-tasks)           |     Sim      | Apenas `ADMIN`                                                  |

<br />

### Requisição e Resposta

**`AUTH`**

<h4 id="auth-register">POST /api/auth/register</h4>

**Requisição**
```json
{
  "name": "User 1",
  "email": "user1@email.com",
  "password": "123456",
  "role": "write_read"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 201,
  "message": "Usuário criado com sucesso",
  "data": {
    "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
    "name": "User 1",
    "email": "user1@email.com",
    "role": "WRITE_READ",
    "createdAt": "2024-03-28T17:31:09.256061",
    "updatedAt": "2024-03-28T17:31:09.256061"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="auth-login">POST /api/auth/login</h4>

**Requisição**
```json
{
  "email": "user1@email.com",
  "password": "123456"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Usuário logado",
  "data": {
    "userInfo": {
      "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
      "name": "User 1",
      "email": "user1@email.com",
      "role": "WRITE_READ",
      "createdAt": "2024-03-28T15:37:40.92",
      "updatedAt": "2024-03-28T15:37:40.92"
    },
    "token": "eyJhbGciOR5cCI6IkpXVCJ9.eyJpc3MiYXBpIiNzEyMjYyOTU0fQ.sCaFqsb1BDH5hOEOjmJwefCiVV24cr6-V4Y"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

**`USER`**

<h4 id="auth-me">GET /api/users/me</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Usuário autenticado",
  "data": {
    "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
    "name": "User 1",
    "email": "user1@email.com",
    "role": "WRITE_READ",
    "createdAt": "2024-03-28T15:37:40.92",
    "updatedAt": "2024-03-28T15:37:40.92"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="all-users">GET /api/users</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os usuários",
  "data": [
    {
      "id": "405c98ba-3251-4b21-8575-7db33576f28a",
      "name": "admin",
      "email": "admin@admin",
      "role": "ADMIN",
      "createdAt": "2024-03-28T15:32:35.32",
      "updatedAt": "2024-03-28T15:32:35.32"
    },
    {
      "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
      "name": "User 1",
      "email": "user1@email.com",
      "role": "WRITE_READ",
      "createdAt": "2024-03-28T15:37:40.92",
      "updatedAt": "2024-03-28T15:37:40.92"
    }
  ]
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="update-user">PATCH /api/users/{userId}</h4>

**Requisição**
```json
{
  "name": "User 1 atualizado",
  "password": "novasenha"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Usuário atualizado com sucesso",
  "data": {
    "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
    "name": "User 1 atualizado",
    "email": "user1@email.com",
    "role": "WRITE_READ",
    "createdAt": "2024-03-28T15:37:40.92",
    "updatedAt": "2024-04-07T15:35:48.243646"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="get-user">GET /api/users/{userId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Usuário encontrado",
  "data": {
    "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
    "name": "User 1",
    "email": "user1@email.com",
    "role": "WRITE_READ",
    "createdAt": "2024-03-28T15:37:40.92",
    "updatedAt": "2024-04-07T15:37:18.93"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="delete-user">DELETE /api/users/{userId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Usuário deletado com sucesso",
  "data": {
    "deletedUser": {
      "id": "472e3467-5147-46e8-9799-84c0ba8dc1f3",
      "name": "User 3",
      "email": "user3@email.com",
      "role": "READ_ONLY",
      "createdAt": "2024-03-28T15:38:05.48",
      "updatedAt": "2024-03-28T15:38:05.48"
    }
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="role">PATCH /api/users/{userId}/role</h4>

**Requisição**
```json
{
  "role": "read_only"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Role atualizada com sucesso",
  "data": {
    "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
    "name": "User 1",
    "email": "user1@email.com",
    "role": "READ_ONLY",
    "createdAt": "2024-03-28T17:31:09.26",
    "updatedAt": "2024-03-28T17:31:09.26"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

**`WORKSPACE`**

<h4 id="create-workspace">POST /api/workspaces</h4>

**Requisição**
```json
{
  "name": "Workspace 1"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 201,
  "message": "Workspace criado com sucesso",
  "data": {
    "id": "64a7efa3-a786-43bf-ada0-d4d2e64b398f",
    "name": "Workspace 1",
    "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
    "createdAt": "2024-03-28T17:10:32.649052",
    "updatedAt": "2024-03-28T17:10:32.649052"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="user-workspaces">GET /api/workspaces</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os seus workspaces",
  "data": [
    {
      "id": "64a7efa3-a786-43bf-ada0-d4d2e64b398f",
      "name": "Workspace 1",
      "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
      "createdAt": "2024-03-28T17:10:32.65",
      "updatedAt": "2024-03-28T17:28:30.28"
    }
  ]
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="update-workspace">PATCH /api/workspaces/{workspaceId}</h4>

**Requisição**
```json
{
  "name": "Workspace 1 atualizado"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Workspace atualizado com sucesso",
  "data": {
    "id": "64a7efa3-a786-43bf-ada0-d4d2e64b398f",
    "name": "Workspace 1 atualizado",
    "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
    "createdAt": "2024-03-28T17:10:32.65",
    "updatedAt": "2024-03-28T17:28:30.275353"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="get-workspace">GET /api/workspaces/{workspaceId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Workspace encontrado",
  "data": {
    "workspace": {
      "id": "69a5e94d-efc2-44ae-9595-c699b99911d9",
      "name": "Workspace 1",
      "ownerId": "97d2a1ad-cd7a-40dc-b2df-9bfe912e6542",
      "createdAt": "2024-03-21T19:37:39.73",
      "updatedAt": "2024-03-21T19:37:39.73"
    },
    "projects": [
      {
        "id": "d61a01b5-9273-467e-a56a-4290b1510efa",
        "name": "Projeto 1",
        "priority": "alta",
        "category": "Desenvolvimento",
        "description": "Descrição do Projeto 1",
        "budget": "25000.00",
        "cost": "2000.00",
        "deadline": "21-08-2025",
        "createdAt": "2024-03-21T19:40:27.4",
        "updatedAt": "2024-03-21T19:42:30.26",
        "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54",
        "workspaceId": "69a5e94d-efc2-44ae-9595-c699b99911d9"
      }
    ],
    "members": [
      {
        "id": "a0648bd1-fb7f-4a4c-916a-c354801dff54",
        "name": "User 1",
        "email": "user1@email.com",
        "role": "WRITE_READ",
        "createdAt": "2024-03-21T19:36:43.72",
        "updatedAt": "2024-03-21T19:36:43.72"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="delete-workspace">DELETE /api/workspaces/{workspaceId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Workspace deletado com sucesso",
  "data": {
    "deletedWorkspace": {
      "id": "64a7efa3-a786-43bf-ada0-d4d2e64b398f",
      "name": "Workspace 1",
      "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
      "createdAt": "2024-03-28T15:38:48.64",
      "updatedAt": "2024-03-28T15:38:48.64"
    }
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="members">GET /api/workspaces/{workspaceId}/members</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os membros do workspace",
  "data": {
    "workspace": {
      "id": "64a7efa3-a786-43bf-ada0-d4d2e64b398f",
      "name": "Workspace 1",
      "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
      "createdAt": "2024-03-28T17:10:32.65",
      "updatedAt": "2024-03-28T17:28:30.28"
    },
    "members": [
      {
        "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
        "name": "User 1",
        "email": "user1@email.com",
        "role": "WRITE_READ",
        "createdAt": "2024-03-28T15:37:40.92",
        "updatedAt": "2024-04-07T15:37:18.93"
      },
      {
        "id": "65fae69d-b3a7-4d75-9a99-da62eda399e9",
        "name": "User 3",
        "email": "user3@email.com",
        "role": "READ_ONLY",
        "createdAt": "2024-03-28T17:31:09.26",
        "updatedAt": "2024-03-28T17:31:09.26"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="insert-member">PATCH /api/workspaces/{workspaceId}/members/{userId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Membro inserido no workspace com sucesso",
  "data": {
    "workspace": {
      "id": "64a7efa3-a786-43bf-ada0-d4d2e64b398f",
      "name": "Workspace 1",
      "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
      "createdAt": "2024-03-28T17:10:32.65",
      "updatedAt": "2024-03-28T17:28:30.28"
    },
    "members": [
      {
        "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
        "name": "User 1",
        "email": "user1@email.com",
        "role": "WRITE_READ",
        "createdAt": "2024-03-28T15:37:40.92",
        "updatedAt": "2024-03-28T15:37:40.92"
      },
      {
        "id": "a36a2e9e-d999-4326-aced-b2c91a04ff2d",
        "name": "User 2",
        "email": "user2@email.com",
        "role": "WRITE_READ",
        "createdAt": "2024-03-28T17:29:27.15",
        "updatedAt": "2024-03-28T17:29:27.15"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="remove-member">DELETE /api/workspaces/{workspaceId}/members/{userId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Membro removido do workspace com sucesso",
  "data": {
    "workspace": {
      "id": "64a7efa3-a786-43bf-ada0-d4d2e64b398f",
      "name": "Workspace 1",
      "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
      "createdAt": "2024-03-28T17:10:32.65",
      "updatedAt": "2024-03-28T17:28:30.28"
    },
    "members": [
      {
        "id": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
        "name": "User 1",
        "email": "user1@email.com",
        "role": "WRITE_READ",
        "createdAt": "2024-03-28T15:37:40.92",
        "updatedAt": "2024-03-28T15:37:40.92"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

**`PROJECT`**
<h4 id="create-project">POST /api/projects</h4>

**Requisição**
```json
{
  "name": "Projeto 1",
  "category": "Infra",
  "description": "Descrição do Projeto 1",
  "budget": "25000.00",
  "priority": "alta",
  "deadline": "21-08-2025",
  "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 201,
  "message": "Projeto criado com sucesso",
  "data": {
    "id": "6e371e5f-6094-4f16-b01f-5b90f531c970",
    "name": "Projeto 1",
    "priority": "alta",
    "category": "Infra",
    "description": "Descrição do Projeto 1",
    "budget": "25000.00",
    "cost": "0",
    "deadline": "21-08-2025",
    "createdAt": "2024-03-28T19:10:35.313285",
    "updatedAt": "2024-03-28T19:10:35.313285",
    "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
    "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="user-projects">GET /api/projects</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os seus projetos",
  "data": [
    {
      "id": "e4b9dc6e-a4c0-4a63-8950-3f5a6f616d17",
      "name": "Projeto 1",
      "priority": "alta",
      "category": "Infra",
      "description": "Descrição do Projeto 1",
      "budget": "25000.00",
      "cost": "0.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-03-28T17:22:00.26",
      "updatedAt": "2024-03-28T17:22:00.26",
      "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
      "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
    },
    {
      "id": "04a2f77a-7551-4888-8f04-751c9ab8f2eb",
      "name": "Projeto 2",
      "priority": "media",
      "category": "Desenvolvimento",
      "description": "Descrição do Projeto 2",
      "budget": "20000.00",
      "cost": "0.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-03-28T17:22:08.81",
      "updatedAt": "2024-03-28T17:22:08.81",
      "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
      "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
    }
  ]
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="delete-projects">DELETE /api/projects</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os seus projetos foram excluídos com sucesso",
  "data": {
    "deletedProjects": [
      {
        "id": "e4b9dc6e-a4c0-4a63-8950-3f5a6f616d17",
        "name": "Projeto 1",
        "priority": "baixa",
        "category": "Infra",
        "description": "Descrição do Projeto 1",
        "budget": "25000.00",
        "cost": "0.00",
        "deadline": "21-08-2025",
        "createdAt": "2024-03-28T17:22:00.26",
        "updatedAt": "2024-03-28T17:22:00.26",
        "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
        "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
      },
      {
        "id": "04a2f77a-7551-4888-8f04-751c9ab8f2eb",
        "name": "Projeto 2",
        "priority": "baixa",
        "category": "Infra",
        "description": "Descrição do Projeto 2",
        "budget": "25000.00",
        "cost": "0.00",
        "deadline": "21-08-2025",
        "createdAt": "2024-03-28T17:22:08.81",
        "updatedAt": "2024-03-28T17:22:08.81",
        "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
        "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="update-project">PATCH /api/projects/{projectId}</h4>

**Requisição**
```json
{
  "name": "Projeto 4 atualizado",
  "category": "Infraestrutura",
  "description": "Descrição do Projeto 4 atualizada",
  "budget": "30000.00",
  "priority": "alta",
  "deadline": "23-09-2025"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Projeto atualizado com sucesso",
  "data": {
    "id": "830c32b2-28b7-4016-a633-e33be63a074a",
    "name": "Projeto 4 atualizado",
    "priority": "alta",
    "category": "Infraestrutura",
    "description": "Descrição do Projeto 4 atualizada",
    "budget": "30000.00",
    "cost": "4000.00",
    "deadline": "23-09-2025",
    "createdAt": "2024-03-28T17:12:52.71",
    "updatedAt": "2024-03-28T22:23:46.411341",
    "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
    "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="get-project">GET /api/projects/{projectId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Projeto encontrado",
  "data": {
    "project": {
      "id": "d61a01b5-9273-467e-a56a-4290b1510efa",
      "name": "Projeto 1",
      "priority": "alta",
      "category": "Desenvolvimento",
      "description": "Descrição do Projeto 1",
      "budget": "25000.00",
      "cost": "11280.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-03-21T19:40:27.4",
      "updatedAt": "2024-03-23T14:53:10.77",
      "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54",
      "workspaceId": "69a5e94d-efc2-44ae-9595-c699b99911d9"
    },
    "tasks": [
      {
        "id": "db972f52-d954-483c-8f54-d3c85cca4440",
        "name": "Task 2",
        "description": "Descrição da task 2",
        "cost": "4280.00",
        "createdAt": "2024-03-23T14:33:20.72",
        "updatedAt": "2024-03-23T14:33:20.72",
        "projectId": "d61a01b5-9273-467e-a56a-4290b1510efa",
        "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54"
      },
      {
        "id": "3c4f5dbc-7b51-41d0-b216-bb44e78dff04",
        "name": "Task 1",
        "description": "Descrição da task 1",
        "cost": "2000.00",
        "createdAt": "2024-03-23T14:52:57.56",
        "updatedAt": "2024-03-23T14:52:57.56",
        "projectId": "d61a01b5-9273-467e-a56a-4290b1510efa",
        "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="delete-project">DELETE /api/projects/{projectId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Projeto excluído com sucesso",
  "data": {
    "deletedProject": {
      "id": "830c32b2-28b7-4016-a633-e33be63a074a",
      "name": "Projeto 4",
      "priority": "baixa",
      "category": "Infra",
      "description": "Descrição do Projeto 4",
      "budget": "25000.00",
      "cost": "4000.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-03-28T17:12:52.71",
      "updatedAt": "2024-03-28T22:23:46.41",
      "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
      "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
    }
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="owner-projects">GET /api/projects/owner/{ownerId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os projetos do usuário de ID: 'f175c9ca-cbf3-4018-98dd-369ba0aa38d5'",
  "data": [
    {
      "id": "e4b9dc6e-a4c0-4a63-8950-3f5a6f616d17",
      "name": "Projeto 1",
      "priority": "baixa",
      "category": "Infra",
      "description": "Descrição do Projeto 4",
      "budget": "25000.00",
      "cost": "0.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-03-28T17:22:00.26",
      "updatedAt": "2024-03-28T17:22:00.26",
      "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
      "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
    },
    {
      "id": "04a2f77a-7551-4888-8f04-751c9ab8f2eb",
      "name": "Projeto 2",
      "priority": "baixa",
      "category": "Infra",
      "description": "Descrição do Projeto 4",
      "budget": "25000.00",
      "cost": "0.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-03-28T17:22:08.81",
      "updatedAt": "2024-03-28T17:22:08.81",
      "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
      "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
    }
  ]
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="delete-owner-projects">DELETE /api/projects/owner/{ownerId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os projetos do usuário de ID: '405c98ba-3251-4b21-8575-7db33576f28a' foram excluídos com sucesso",
  "data": {
    "deletedProjects": [
      {
        "id": "2a0a2b1e-4b74-4f6c-8491-965ebbc0b8f5",
        "name": "Projeto 4",
        "priority": "baixa",
        "category": "Infra",
        "description": "Descrição do Projeto 4",
        "budget": "25000.00",
        "cost": "0.00",
        "deadline": "21-08-2025",
        "createdAt": "2024-03-28T19:10:12.44",
        "updatedAt": "2024-03-28T19:10:12.44",
        "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
        "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
      },
      {
        "id": "6e371e5f-6094-4f16-b01f-5b90f531c970",
        "name": "Projeto 5",
        "priority": "baixa",
        "category": "Infra",
        "description": "Descrição do Projeto 5",
        "budget": "25000.00",
        "cost": "0.00",
        "deadline": "21-08-2025",
        "createdAt": "2024-03-28T19:10:35.31",
        "updatedAt": "2024-03-28T19:10:35.31",
        "ownerId": "405c98ba-3251-4b21-8575-7db33576f28a",
        "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="workspace-projects">GET /api/projects/workspaces/{workspaceId}?sortingOrder={order}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os projetos do workspace de ID: '69a5e94d-efc2-44ae-9595-c699b99911d9'",
  "data": [
    {
      "id": "0e9d6313-c7ec-4c67-a14e-7d5ea4743cca",
      "name": "Projeto 5",
      "priority": "alta",
      "category": "Infra",
      "description": "Descrição do Projeto 5",
      "budget": "25000.00",
      "cost": "0.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-03-26T16:52:48.18",
      "updatedAt": "2024-03-26T16:52:48.18",
      "ownerId": "97d2a1ad-cd7a-40dc-b2df-9bfe912e6542",
      "workspaceId": "69a5e94d-efc2-44ae-9595-c699b99911d9"
    },
    {
      "id": "d61a01b5-9273-467e-a56a-4290b1510efa",
      "name": "Projeto 1",
      "priority": "media",
      "category": "Desenvolvimento",
      "description": "Descrição do Projeto 1",
      "budget": "25000.00",
      "cost": "12280.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-03-21T19:40:27.4",
      "updatedAt": "2024-03-24T21:58:55.86",
      "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54",
      "workspaceId": "69a5e94d-efc2-44ae-9595-c699b99911d9"
    }
  ]
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="delete-workspace-projects">DELETE /api/projects/workspaces/{workspaceId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os projetos do workspace de ID: '3dc5aef9-d24f-4aba-8e28-2aa18a949b2c' excluídos com sucesso",
  "data": {
    "deletedProjects": [
      {
        "id": "b8257cad-09ed-4b89-9b97-5ead3c75c709",
        "name": "Projeto 3",
        "priority": "baixa",
        "category": "Infra",
        "description": "Descrição do Projeto 3",
        "budget": "25000.00",
        "cost": "0.00",
        "deadline": "21-08-2025",
        "createdAt": "2024-03-28T15:43:20.96",
        "updatedAt": "2024-03-28T15:43:20.96",
        "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
        "workspaceId": "3dc5aef9-d24f-4aba-8e28-2aa18a949b2c"
      },
      {
        "id": "0ad75ed2-0ec8-4ff0-b0c3-a8bd4e404ac1",
        "name": "Projeto 2",
        "priority": "baixa",
        "category": "Infra",
        "description": "Descrição do Projeto 2",
        "budget": "25000.00",
        "cost": "0.00",
        "deadline": "21-08-2025",
        "createdAt": "2024-03-28T15:43:12.83",
        "updatedAt": "2024-03-28T16:56:36.33",
        "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
        "workspaceId": "3dc5aef9-d24f-4aba-8e28-2aa18a949b2c"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="workspace-owner-projects">GET /api/projects/workspaces/{workspaceId}/owner/{ownerId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os projetos do usuário de id 'f175c9ca-cbf3-4018-98dd-369ba0aa38d5' no workspace de id '64a7efa3-a786-43bf-ada0-d4d2e64b398f'",
  "data": [
    {
      "id": "fef0bc14-d684-4214-b605-02cc3b8e88ab",
      "name": "Projeto 1",
      "priority": "alta",
      "category": "Desenvolvimento",
      "description": "Descrição do Projeto 1",
      "budget": "25000.00",
      "cost": "0.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-04-07T19:20:53.3",
      "updatedAt": "2024-04-07T19:20:53.3",
      "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
      "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
    },
    {
      "id": "1772e34c-bfc9-4b1f-bca2-dccd71d10f2f",
      "name": "Projeto 2",
      "priority": "alta",
      "category": "Infra",
      "description": "Descrição do Projeto 2",
      "budget": "25000.00",
      "cost": "0.00",
      "deadline": "21-08-2025",
      "createdAt": "2024-04-07T19:21:08.62",
      "updatedAt": "2024-04-07T19:21:08.62",
      "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5",
      "workspaceId": "64a7efa3-a786-43bf-ada0-d4d2e64b398f"
    }
  ]
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="delete-workspace-owner-projects">DELETE /api/projects/workspaces/{workspaceId}/owner/{ownerId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todos os projetos do usuário de ID 'a0648bd1-fb7f-4a4c-916a-c354801dff54' do workspace de ID '57cd724e-5f6d-4c9f-aeb1-954e154cbba7' foram excluídos com sucesso",
  "data": {
    "deletedProjects": [
      {
        "id": "6ae7acda-ad8b-43d7-bb66-e8e55b731fa9",
        "name": "Projeto 3",
        "priority": "baixa",
        "category": "Infra",
        "description": "Descrição do Projeto 3",
        "budget": "25000.00",
        "cost": "0.00",
        "deadline": "21-08-2025",
        "createdAt": "2024-03-26T19:48:52.48",
        "updatedAt": "2024-03-26T19:48:52.48",
        "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54",
        "workspaceId": "57cd724e-5f6d-4c9f-aeb1-954e154cbba7"
      },
      {
        "id": "65602661-bd82-455d-85ae-1b89e1ac630f",
        "name": "Projeto 4",
        "priority": "baixa",
        "category": "Infra",
        "description": "Descrição do Projeto 4",
        "budget": "25000.00",
        "cost": "0.00",
        "deadline": "21-08-2025",
        "createdAt": "2024-03-26T19:48:58.33",
        "updatedAt": "2024-03-26T19:48:58.33",
        "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54",
        "workspaceId": "57cd724e-5f6d-4c9f-aeb1-954e154cbba7"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

**`TASK`**

<h4 id="create-task">POST /api/tasks</h4>

**Requisição**
```json
{
  "name": "Task 1",
  "description": "Descrição da task 1",
  "cost": "1000.00",
  "projectId": "830c32b2-28b7-4016-a633-e33be63a074a"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 201,
  "message": "Task criada com sucesso",
  "data": {
    "id": "34186661-aa70-4152-9cac-3ffca66f348b",
    "name": "Task 1",
    "description": "Descrição da task 1",
    "cost": "1000.00",
    "createdAt": "2024-03-28T17:23:53.614976",
    "updatedAt": "2024-03-28T17:23:53.614976",
    "projectId": "830c32b2-28b7-4016-a633-e33be63a074a",
    "ownerId": "f175c9ca-cbf3-4018-98dd-369ba0aa38d5"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="user-tasks">GET /api/tasks</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todas as suas tasks",
  "data": [
    {
      "id": "db972f52-d954-483c-8f54-d3c85cca4440",
      "name": "Task 2",
      "description": "Descrição da task 2",
      "cost": "4280.00",
      "createdAt": "2024-03-23T14:33:20.72",
      "updatedAt": "2024-03-23T14:33:20.72",
      "projectId": "d61a01b5-9273-467e-a56a-4290b1510efa",
      "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54"
    },
    {
      "id": "3c4f5dbc-7b51-41d0-b216-bb44e78dff04",
      "name": "Task 1",
      "description": "Descrição da task 1",
      "cost": "2000.00",
      "createdAt": "2024-03-23T14:52:57.56",
      "updatedAt": "2024-03-24T21:42:29.66",
      "projectId": "d61a01b5-9273-467e-a56a-4290b1510efa",
      "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54"
    }
  ]
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="get-task">GET /api/tasks/{taskId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Task encontrada",
  "data": {
    "id": "9991bea9-48c5-497f-b833-b5a10f67cd9a",
    "name": "Task 1",
    "description": "Descrição da task 1",
    "cost": "2000.00",
    "createdAt": "2024-03-21T19:42:30.29",
    "updatedAt": "2024-03-21T19:42:30.29",
    "projectId": "d61a01b5-9273-467e-a56a-4290b1510efa",
    "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="delete-task">DELETE /api/tasks/{taskId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Task excluída com sucesso",
  "data": {
    "deletedTask": {
      "id": "cdd8bbed-b8df-4cd1-88d0-02be58c317e6",
      "name": "Task 4",
      "description": "Descrição da task 4",
      "cost": "1000.00",
      "createdAt": "2024-03-28T16:23:20.09",
      "updatedAt": "2024-03-28T16:23:20.09",
      "projectId": "7905905d-30ba-4766-82dd-d91e0818f905",
      "ownerId": "63a129b7-2be9-48e5-b561-99240ab479ba"
    }
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="update-task">PATCH /api/tasks/{taskId}</h4>

**Requisição**
```json
{
  "name": "Task 4 atualizada",
  "description": "Descrição da Task 4 atualizada",
  "cost": "1000.00"
}
```

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Task atualizada com sucesso",
  "data": {
    "id": "46b15bca-46b6-4c05-9e20-3934387b6d68",
    "name": "Task 4 atualizada",
    "description": "Descrição da task 4 atualizada",
    "cost": "1000.00",
    "createdAt": "2024-03-24T21:43:34.81",
    "updatedAt": "2024-03-24T21:58:55.932462",
    "projectId": "d61a01b5-9273-467e-a56a-4290b1510efa",
    "ownerId": "bb45a1dc-b8f9-4b5b-9237-4f133ce41921"
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="project-tasks">GET /api/tasks/projects/{projectId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todas as tasks do projeto de ID: 'd61a01b5-9273-467e-a56a-4290b1510efa'",
  "data": [
    {
      "id": "db972f52-d954-483c-8f54-d3c85cca4440",
      "name": "Task 2",
      "description": "Descrição da task 2",
      "cost": "4280.00",
      "createdAt": "2024-03-23T14:33:20.72",
      "updatedAt": "2024-03-23T14:33:20.72",
      "projectId": "d61a01b5-9273-467e-a56a-4290b1510efa",
      "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54"
    },
    {
      "id": "3c4f5dbc-7b51-41d0-b216-bb44e78dff04",
      "name": "Task 1",
      "description": "Descrição da task 1",
      "cost": "2000.00",
      "createdAt": "2024-03-23T14:52:57.56",
      "updatedAt": "2024-03-24T21:42:29.66",
      "projectId": "d61a01b5-9273-467e-a56a-4290b1510efa",
      "ownerId": "a0648bd1-fb7f-4a4c-916a-c354801dff54"
    }
  ]
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="delete-project-tasks">DELETE /api/tasks/projects/{projectId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todas as tasks do projeto de ID: 'd755f16b-5729-44e4-b0e7-fdc2f11e7231' foram excluídas com sucesso",
  "data": {
    "deletedTasks": [
      {
        "id": "9fb7b897-7971-4bd8-b0a3-8998025c5eda",
        "name": "Task 8",
        "description": "Descrição da task 8",
        "cost": "1000.00",
        "createdAt": "2024-03-26T19:46:37.62",
        "updatedAt": "2024-03-26T19:46:37.62",
        "projectId": "d755f16b-5729-44e4-b0e7-fdc2f11e7231",
        "ownerId": "8e5b432e-cd5d-493f-879b-dfa1a4bd2c89"
      },
      {
        "id": "b081c17d-dedd-425b-9a85-b226f1c9f1ba",
        "name": "Task 9",
        "description": "Descrição da task 9",
        "cost": "1000.00",
        "createdAt": "2024-03-26T19:46:44.71",
        "updatedAt": "2024-03-26T19:46:44.71",
        "projectId": "d755f16b-5729-44e4-b0e7-fdc2f11e7231",
        "ownerId": "8e5b432e-cd5d-493f-879b-dfa1a4bd2c89"
      }
    ]
  }
}
```
[Voltar para as rotas ⬆](#endpoints)

<br />

<h4 id="owner-tasks">GET /api/tasks/owner/{ownerId}</h4>

**Resposta**
```json
{
  "status": "Success",
  "code": 200,
  "message": "Todas as tasks do usuário de ID: '63a129b7-2be9-48e5-b561-99240ab479ba'",
  "data": [
    {
      "id": "10aa3465-d79a-463c-aabb-616ca7ff69ce",
      "name": "Task 3",
      "description": "Descrição da task 3",
      "cost": "1000.00",
      "createdAt": "2024-03-28T16:23:11.76",
      "updatedAt": "2024-03-28T16:23:11.76",
      "projectId": "7905905d-30ba-4766-82dd-d91e0818f905",
      "ownerId": "63a129b7-2be9-48e5-b561-99240ab479ba"
    },
    {
      "id": "cdd8bbed-b8df-4cd1-88d0-02be58c317e6",
      "name": "Task 4",
      "description": "Descrição da task 4",
      "cost": "1000.00",
      "createdAt": "2024-03-28T16:23:20.09",
      "updatedAt": "2024-03-28T16:23:20.09",
      "projectId": "7905905d-30ba-4766-82dd-d91e0818f905",
      "ownerId": "63a129b7-2be9-48e5-b561-99240ab479ba"
    }
  ]
}
```
[Voltar para as rotas ⬆](#endpoints)

<h2 id="run">⚙ Como rodar a aplicação</h2>

### Executando o código localmente
-> Para executar o código localmente, é necessário ter instalado o [Git](https://git-scm.com/), o [Java](https://www.oracle.com/br/java/technologies/downloads/#java17)
devidamente configurado, e o [Docker](https://www.docker.com/).
- Para executar a aplicação, precisamos rodar alguns comandos com o auxílio de um terminal. Primeiro, clone este repositório:

  ```bash
  $ git clone https://github.com/luizfelipeapolonio/project-manager-api
  ```
- Acesse a pasta da aplicação:
  ```bash
  $ cd project-manager-api
  ```
- Crie e inicialize o container Docker do banco de dados da aplicação:
  > ⚠ O Docker já deve estar executando antes de rodar este comando.
  ```bash
  $ docker compose -f docker-compose-local.yml up -d
  ```
- Assumindo que o container Docker do banco de dados Postgres esteja rodando localmente, altere o arquivo `application.properties` da seguinte maneira:
  ```bash
  # Acesse a pasta onde se encontra o arquivo:
  src/main/resources

  # Em um editor de código, altere a linha do arquivo application.properties para:
  spring.datasource.url=jdbc:postgresql://localhost:5432/project_manager_api
  ```

<br />

> ⚠ Se estiver utilizando Windows, use o `PowerShell` para executar todos os comandos abaixo para que funcionem como esperado.
- Na raíz da pasta `project-manager-api`, execute o script do Maven Wrapper para instalar as dependências:
  ```bash
  $ ./mvnw clean install
  ```
- Execute a aplicação como uma aplicação Spring Boot:
  ```bash
  $ ./mvnw spring-boot:run
  ```
  ou abra a aplicação na sua IDE favorita como um projeto Maven e execute como uma aplicação Spring Boot.

- A aplicação ficará acessível no endereço http://localhost:8080

### Executando a aplicação no Docker

-> Para executar a aplicação e o banco de dados em containers basta ter instalado o [Docker](https://www.docker.com/) e o [Git](https://git-scm.com/).
- Primeiro, clone este repositório:

  ```bash
  $ git clone https://github.com/luizfelipeapolonio/project-manager-api
  ```
- Acesse a pasta da aplicação:
  ```bash
  $ cd project-manager-api
  ```
- Gere a imagem Docker da aplicação com o comando:
  > ⚠ O " . " no final do comando é essencial para a execução, portanto, certifique-se de que o comando esteja exatamente igual a este.
  ```bash
  $ docker build -t project_manager_image .
  ```
- Uma vez gerada a imagem, crie e inicialize os containers da aplicação e do banco de dados com o comando:
  ```bash
  $ docker compose up -d
  ```
- Ambos containers serão construídos e inicializados. A aplicação estará acessível no endereço http://localhost:8080

<h2 id="license">📝 Licença</h2>

Este repositório está licenciado pela **MIT LICENSE**. Para mais informações, leia o arquivo [LICENSE](./LICENSE) contido neste repositório.

<h2 id="author">Autor</h2>

Linkedin: [acesse meu perfil](https://www.linkedin.com/in/luiz-felipe-salgado-31a969273/).

Feito com 💜 por luizfelipeapolonio


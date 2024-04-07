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

|      Tipo      | Rota                                                     | Descrição                                                                     | Autenticação | Autorização                                                    |
|:--------------:|:---------------------------------------------------------|:------------------------------------------------------------------------------|:------------:|:---------------------------------------------------------------|
|  **_`POST`_**  | `/api/projects`                                          | Criar projeto [requisição/resposta](#create-project)                          |     Sim      | Apenas `ADMIN` e `WRITE_READ` dono ou membro do workspace      |
|  **_`GET`_**   | `/api/projects`                                          | Listar todos os projetos do usuário autenticado [resposta](#user-projects)    |     Sim      | Apenas `ADMIN`, `WRITE_READ`                                   |
| **_`DELETE`_** | `/api/projects`                                          | Excluir todos os projetos do usuário autenticado [resposta](#delete-projects) |     Sim      | Apenas `ADMIN`, `WRITE_READ`                                   |
| **_`PATCH`_**  | `/api/projects/{projectId}`                              | Atualizar os dados de um projeto                                              |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do projeto                   |
|  **_`GET`_**   | `/api/projects/{projectId}`                              | Visualizar um projeto                                                         |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` membro ou dono do workspace |
| **_`DELETE`_** | `/api/projects/{projectId}`                              | Excluir um projeto                                                            |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do projeto ou workspace      |
|  **_`GET`_**   | `/api/projects/owner/{ownerId}`                          | Listar todos os projetos de um usuário específico                             |     Sim      | Apenas `ADMIN`                                                 |
| **_`DELETE`_** | `/api/projects/owner/{ownerId}`                          | Excluir todos os projetos de um usuário específico                            |     Sim      | Apenas `ADMIN`                                                 |
|  **_`GET`_**   | `/api/projects/workspaces/{workspaceId}`                 | Listar todos os projetos de um workspace específico                           |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` membro ou dono do workspace |
| **_`DELETE`_** | `/api/projects/workspaces/{workspaceId}`                 | Excluir todos os projetos de um workspace específico                          |     Sim      | Apenas `ADMIN` dono do workspace                               |
|  **_`GET`_**   | `/api/projects/workspaces/{workspaceId}/owner/{ownerId}` | Listar todos os projetos de um usuário específico em um workspace específico  |     Sim      | Apenas `ADMIN` dono do workspace                               |
| **_`DELETE`_** | `/api/projects/workspaces/{workspaceId}/owner/{ownerId}` | Excluir todos os projetos de um usuário específico em um workspace específico |     Sim      | Apenas `ADMIN` dono do workspace                               |

<br />

### Task 

|      Tipo      | Rota                              | Descrição                                       | Autenticação | Autorização                                                     |
|:--------------:|:----------------------------------|:------------------------------------------------|:------------:|:----------------------------------------------------------------|
|  **_`POST`_**  | `/api/tasks`                      | Criar task                                      |     Sim      | Apenas `ADMIN`, `WRITE_READ` membro ou dono do workspace        |
|  **_`GET`_**   | `/api/tasks`                      | Listar todas as tasks do usuário autenticado    |     Sim      | Apenas `ADMIN`, `WRITE_READ`                                    |
|  **_`GET`_**   | `/api/tasks/{taskId}`             | Visualizar uma task                             |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` membro ou dono do workspace  |
| **_`DELETE`_** | `/api/tasks/{taskId}`             | Excluir uma task                                |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do workspace, projeto ou task |
| **_`PATCH`_**  | `/api/tasks/{taskId}`             | Atualizar uma task                              |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do workspace, projeto ou task |
|  **_`GET`_**   | `/api/tasks/projects/{projectId}` | Listar todas as tasks de um projeto específico  |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` membro ou dono do workspace  |
| **_`DELETE`_** | `/api/tasks/projects/{projectId}` | Excluir todas as tasks de um projeto específico |     Sim      | Apenas `ADMIN`, `WRITE_READ` dono do projeto ou do workspace    |
|  **_`GET`_**   | `/api/tasks/owner/{ownerId}`      | Listar todas as tasks de um usuário específico  |     Sim      | Apenas `ADMIN`                                                  |

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


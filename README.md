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
- Um usuário só pode criar um projeto ou tarefa se for membro de algum workspace.
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
- Apenas o usuário com a role `ADMIN`, que seja dono do projeto e membro do workspace ao qual o projeto pertence, e 
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

|     Tipo     | Rota                 | Descrição     |  Autenticação  | Autorização         |
|:------------:|:---------------------|:--------------|:--------------:|:--------------------|
| **_`POST`_** | `/api/auth/register` | Criar usuário |      Sim       | Apenas para `ADMIN` |
| **_`POST`_** | `/api/auth/login`    | Logar usuário |      Não       | Qualquer usuário    |

<br />

### User

|      Tipo      | Rota                       | Descrição                                  | Autenticação | Autorização                        |
|:--------------:|:---------------------------|:-------------------------------------------|:------------:|:-----------------------------------|
|  **_`GET`_**   | `/api/users/me`            | Visualizar perfil do usuário autenticado   |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` |
|  **_`GET`_**   | `/api/users`               | Listar todos os usuários                   |     Sim      | Apenas `ADMIN`                     |
| **_`PATCH`_**  | `/api/users/{userId}`      | Atualizar dados do usuário                 |     Sim      | `ADMIN`, `WRITE_READ`, `READ_ONLY` |
|  **_`GET`_**   | `/api/users/{userId}`      | Visualizar perfil de um usuário específico |     Sim      | Apenas `ADMIN`                     |
| **_`DELETE`_** | `/api/users/{userId}`      | Excluir perfil de um usuário específico    |     Sim      | Apenas `ADMIN`                     |
| **_`PATCH`_**  | `/api/users/{userId}/role` | Alterar role de um usuário                 |     Sim      | Apenas `ADMIN`                     |

<br />

### Workspace

|      Tipo      | Rota                                             | Descrição                            | Autenticação | Autorização                                                     |
|:--------------:|:-------------------------------------------------|:-------------------------------------|:------------:|:----------------------------------------------------------------|
|  **_`POST`_**  | `/api/workspaces`                                | Criar workspace                      |     Sim      | Apenas `ADMIN`                                                  |
|  **_`GET`_**   | `/api/workspaces`                                | Listar os próprios workspaces        |     Sim      | Apenas `ADMIN`                                                  |
| **_`PATCH`_**  | `/api/workspaces/{workspaceId}`                  | Atualizar dados do workspace         |     Sim      | Apenas `ADMIN` dono do workspace                                |
|  **_`GET`_**   | `/api/workspaces/{workspaceId}`                  | Visualizar um workspace              |     Sim      | Qualquer membro do workspace `ADMIN`, `WRITE_READ`, `READ_ONLY` |
| **_`DELETE`_** | `/api/workspaces/{workspaceId}`                  | Excluir um workspace                 |     Sim      | Apenas `ADMIN` dono do workspace                                |
|  **_`GET`_**   | `/api/workspaces/{workspaceId}/members`          | Listar todos os membros do workspace |     Sim      | Apenas `ADMIN`                                                  |
| **_`PATCH`_**  | `/api/workspaces/{workspaceId}/members/{userId}` | Inserir um membro em um workspace    |     Sim      | Apenas `ADMIN` dono do workspace                                |
| **_`DELETE`_** | `/api/workspaces/{workspaceId}/members/{userId}` | Remover um membro de um workspace    |     Sim      | Apenas `ADMIN` dono do workspace                                |
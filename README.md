# Project Manager - API REST com Spring

Project Manager API é uma aplicação para gerenciar custos de projetos, do tipo API REST desenvolvida com **Java Spring 
Framework (Spring Boot 3, Spring 6)**, com sistema de autenticação Stateless utilizando **Spring Security** e **JWT**. 
Como 
base de dados, 
utiliza o **PostgreSQL** em conjunto com o **Spring Data JPA** e **Hibernate** como ORM. <br />
Com o Project Manager é possível criar usuários, criar workspaces, adicionar e remover usuários dos workspaces, 
criar projetos dentro dos workspaces, e adicionar tarefas nos projetos com base no valor do orçamento.

<br />

<p align="center">
    <a href="#auth">Autorização</a> •
    <a href="#business">Regras de negócio</a> •
    <a href="#tech">Tecnologias utilizadas</a> •
    <a href="#resources">Funções/Recursos</a> •
    <a href="#endpoints">Rotas da API</a> •
    <a href="#run">Como rodar a aplicação</a> •
    <a href="#license">Licença</a> •
    <a href="#author">Autor</a>
</p>

<br />

<h2 id="auth">Autorização</h2>

O sistema de autenticação e autorização da aplicação foi desenvolvido com o módulo **Spring Security**, portanto, 
todas as rotas da API são protegidas por filtros de autorização baseados na role do usuário. <br />
Cada usuário pode possuir apenas uma das 3 roles, que são:

|        Role         | Descrição                                                                                                                |
|:-------------------:|:-------------------------------------------------------------------------------------------------------------------------|
|    **_`ADMIN`_**    | Super usuário capaz de executar todas as ações de gerenciamento do sistema, assim como criar, editar e excluir recursos. |
| **_`WRITE_READ`_**  | Usuário com permissão para criar, editar, excluir e visualizar alguns recursos do sistema.                               |
|  **_`READ_ONLY`_**  | Usuário com permissão apenas de visualizar recursos do sistema.                                                          |

> ❕ A primeira vez que a aplicação é executada, um super usuário com a role `ADMIN` é criado e inserido 
automaticamente no banco de dados, para que a partir dele possam ser criados outros usuários e recursos no sistema.

<h2 id="business">Regras de negócio</h2>

O Project Manager é uma aplicação pensada para ser uma ferramenta de gerenciamento de custos de projetos, logo, o 
sistema foi desenvolvido de uma maneira bem específica seguindo algumas regras. São elas:

### User

- Apenas usuários com a role `ADMIN` podem criar outros usuários.
- Apenas usuários com a role `ADMIN` podem excluir outros usuários.
- Usuários com a role `ADMIN` podem atruibuir qualquer role para para qualquer usuário criado.
- Todos os usuários podem ver e atualizar suas informações.
- Apenas usuários com a role `ADMIN` podem ver todos os usuários criados.

### Workspace

- Apenas usuários com a role `ADMIN` podem criar um workspace.
- Apenas usuários com a role `ADMIN` e que seja o dono do workspace podem atualizar e deletar um workspace.
- Apenas usuários com a role `ADMIN` e que seja o dono do workspace podem inserir e remover membros (user) em um 
  workspace.



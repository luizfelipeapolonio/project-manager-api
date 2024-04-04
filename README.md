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
    <a href="#auth">Autorização</a> •
    <a href="requirements">Requisitos funcionais</a> •
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
Cada usuário autenticado pode possuir apenas uma das 3 roles, que são:

|        Role         | Descrição                                                                                                                |
|:-------------------:|:-------------------------------------------------------------------------------------------------------------------------|
|    **_`ADMIN`_**    | Super usuário capaz de executar todas as ações de gerenciamento do sistema, assim como criar, editar e excluir recursos. |
| **_`WRITE_READ`_**  | Usuário com permissão para criar, editar, excluir e visualizar alguns recursos do sistema.                               |
|  **_`READ_ONLY`_**  | Usuário com permissão apenas de visualizar recursos do sistema.                                                          |

> ❕ A primeira vez que a aplicação é executada, um super usuário com a role `ADMIN` é criado e inserido 
automaticamente no banco de dados, para que a partir dele possam ser criados outros usuários e recursos no sistema.

<h2 id="requirements">Requisitos funcionais</h2>

Os requisitos funcionais da aplicação são:

- Um usuário só pode ser excluído se não possuir nenhum workspace, projeto ou tarefa.
- Um projeto só deve existir dentro de um workspace.
- Todos os projetos de um usuário devem ser excluídos quando for removido do workspace.

<h2 id="business">Regras de negócio</h2>

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
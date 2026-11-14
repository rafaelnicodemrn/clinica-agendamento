# Clínica Saúde+ — Agendamento de Consultas

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white) ![Angular](https://img.shields.io/badge/Angular-20-DD0031?logo=angular&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

Trabalho bimestral (Angular + Spring Boot + PostgreSQL).
**Autores:** Rafael Luiz Nicodem Ratti e Juliano Carra Rigon

**Tema 4 — Clínica: agendamento de consultas.** Entidades: **Paciente**, **Profissional**, **Consulta**.
Fluxo: 1. Cadastro de pacientes → 2. Lista de profissionais/especialidades → 3. Agenda e marcação de consulta.

## Tecnologias

| Camada   | Stack |
|----------|-------|
| Frontend | Angular 20 (standalone, HttpClient, formulários reativos), Bootstrap 5, CSS |
| Backend  | Spring Boot 4.1 (Java 21), Spring Web MVC, Spring Data JPA, Bean Validation |
| Banco    | PostgreSQL 16 |

## Como executar

Pré-requisitos: JDK 21, Maven, Node 20+ e PostgreSQL.

1. **Banco** (opção Docker): `docker compose up -d` na raiz. Sem Docker, crie um banco `clinica` no PostgreSQL local
   (usuário/senha `postgres`/`postgres`, configurados em `backend/src/main/resources/application.properties`).
2. **Backend** (porta 8080):
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   As tabelas são criadas automaticamente (`ddl-auto=update`) e 5 profissionais de exemplo são inseridos na primeira execução.
3. **Frontend** (porta 4200):
   ```bash
   cd frontend
   npm install
   npm start
   ```
   Acesse http://localhost:4200.

Testes do backend: `cd backend && mvn test`.

## Modelo de dados

```
Paciente 1 ──── N Consulta N ──── 1 Profissional
```

- **Paciente**: nome, CPF (único), data de nascimento, telefone, e-mail.
- **Profissional**: nome, especialidade, registro/CRM (único), e-mail, ativo.
- **Consulta**: paciente, profissional, data/hora, duração (30 min), status (`AGENDADA`, `CONFIRMADA`, `REALIZADA`, `CANCELADA`), observações.

## Organização do código

```
backend/src/main/java/com/clinica/agendamento/
  model/        entidades JPA (Paciente, Profissional, Consulta, StatusConsulta)
  repository/   interfaces JpaRepository
  service/      regras de negócio
  controller/   endpoints REST (ResponseEntity)
  dto/          objetos de entrada da consulta (ConsultaRequest, StatusRequest, HorarioDisponivel)
  exception/    NegocioException + GlobalExceptionHandler
  config/       CORS e dados iniciais

frontend/src/app/
  entidades/    interfaces TypeScript (Paciente, Profissional, Consulta)
  servicos/     services com HttpClient (um por entidade)
  componentes/  telas (lista/formulário de cada entidade, agenda e consultas)
```

## Divisão por integrante (um Controller + Service + Repository cada)

| Entidade | Controller | Service | Repository | Telas Angular |
|----------|-----------|---------|-----------|---------------|
| Paciente (etapa 1) | `PacienteController` | `PacienteService` | `PacienteRepository` | `componentes/paciente-lista` e `paciente-form` |
| Profissional (etapa 2) | `ProfissionalController` | `ProfissionalService` | `ProfissionalRepository` | `componentes/profissional-lista` e `profissional-form` |
| Consulta (etapa 3) | `ConsultaController` | `ConsultaService` | `ConsultaRepository` | `componentes/agenda` (marcação) e `consulta-lista` (gestão) |

O grupo do enunciado prevê três integrantes e cada um assume uma entidade; sugestão de distribuição:
Paciente → Rafael, Consulta/Agenda → Juliano, Profissional → terceiro integrante (ou dividam como preferirem —
o código está separado por entidade justamente para isso).

## API REST

| Método | Rota | Descrição |
|--------|------|-----------|
| GET/POST | `/api/pacientes` | Lista (`?nome=`) / cria |
| GET/PUT/DELETE | `/api/pacientes/{id}` | Busca / atualiza / exclui |
| GET/POST | `/api/profissionais` | Lista (`?especialidade=`) / cria |
| GET | `/api/profissionais/especialidades` | Especialidades distintas |
| GET/PUT/DELETE | `/api/profissionais/{id}` | Busca / atualiza / exclui |
| GET | `/api/consultas` | Lista (`?profissionalId=&pacienteId=&status=`) |
| GET | `/api/consultas/agenda?profissionalId=&data=yyyy-MM-dd` | Grade do dia com horários livres/ocupados |
| POST | `/api/consultas` | Marca consulta |
| PUT | `/api/consultas/{id}` | Remarca |
| PATCH | `/api/consultas/{id}/status` | Confirma / realiza / cancela |
| DELETE | `/api/consultas/{id}` | Exclui |

Respostas: `200`, `201` (criado), `204` (excluído), `404` (id não existe). Erros de validação (`400`) e de regra de negócio (`409`) retornam JSON `{ status, mensagem, campos }`, que o front mostra na tela.

## Regras de negócio (integração das entidades)

- Atendimento de segunda a sexta, 08:00–12:00 e 14:00–18:00, em blocos de 30 minutos.
- Só é possível marcar para data/hora futura e dentro da grade de atendimento.
- Um profissional não pode ter duas consultas ativas no mesmo horário; o mesmo vale para o paciente.
- Profissional inativo não aceita novos agendamentos (e some da tela de agenda).
- Consultas `REALIZADA`/`CANCELADA` não podem mais ser alteradas; consulta futura não pode ser marcada como realizada.
- Paciente ou profissional com consultas registradas não pode ser excluído (o profissional deve ser inativado).
- CPF e registro profissional são únicos.

## Telas (Angular)

1. **Pacientes** — listagem com busca por nome, cadastro/edição com validação (CPF 11 dígitos, telefone, e-mail, data no passado), exclusão.
2. **Profissionais** — cards com filtro por especialidade, cadastro/edição, atalho "Ver agenda".
3. **Agendar** — escolhe paciente, especialidade, profissional e data; mostra a grade de horários; confirma a marcação (também usada para remarcar).
4. **Consultas** — listagem com filtros por profissional e status, confirmar, marcar realizada, remarcar, cancelar e excluir.

Interface responsiva com Bootstrap (navbar colapsável, grid, tabelas rolláveis).

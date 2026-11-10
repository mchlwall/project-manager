# project-manager

[![CI](https://github.com/mchlwall/project-manager/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/mchlwall/project-manager/actions/workflows/ci.yml)

A well-tested project management tool for personal use.

It keeps a list of **projects**, and each project has **tasks**. Everything is
done through a web API (a set of web addresses that accept and return data),
and the data is saved in a single file on disk (an SQLite database).

The badge above shows whether the automatic build and tests passed on the
latest version of the code: green means everything passed, red means
something broke.

## Running it

There are two ways. Pick one.

### Option 1: With Docker (nothing else to install)

Needs [Docker](https://docs.docker.com/get-docker/) or Rancher Desktop running.

```bash
docker compose up --build -d   # build and start the app in the background
curl localhost:8080/health     # should reply {"status":"ok"}
docker compose down            # stop it (your data is kept)
```

Data is saved in the `data/` folder of this repo. Delete that folder to start fresh.

### Option 2: With Java on your machine

Needs Java 21 installed.

```bash
./mvnw spring-boot:run         # start the app (Ctrl+C to stop)
curl localhost:8080/health     # should reply {"status":"ok"}
```

Data is saved in `project-manager.db` in this folder.

## Using it in a browser

Once the app is running (either option), open <http://localhost:8080>.
You can create, rename and delete projects, then open a project to add
tasks, change their status, filter them and delete them. If something
isn't allowed (for example, a blank name), the reason is shown at the top
of the page.

## Running the tests

```bash
./mvnw test
```

Test reports are written to `target/surefire-reports/`. The same tests run
automatically on GitHub for every pull request and every change to `main`.

## What the API does

All data is sent and received as JSON. Errors always come back as
`{"error": "description of the problem"}`.

| Request | What it does |
| --- | --- |
| `GET /health` | Checks the app is running |
| `GET /api/projects` | Lists all projects |
| `POST /api/projects` | Creates a project (name required, must be unique) |
| `GET /api/projects/{id}` | Gets one project |
| `PATCH /api/projects/{id}` | Changes a project's name or description |
| `DELETE /api/projects/{id}` | Deletes a project and all its tasks |
| `POST /api/projects/{id}/tasks` | Adds a task to a project (title required) |
| `GET /api/projects/{id}/tasks` | Lists a project's tasks, optionally `?status=` and/or `?priority=` |
| `GET /api/tasks/{id}` | Gets one task |
| `PATCH /api/tasks/{id}` | Changes a task's details |
| `DELETE /api/tasks/{id}` | Deletes a task |

**Project fields:** `id`, `name`, `description`, `createdAt`, `updatedAt`

**Task fields:** `id`, `projectId`, `title`, `description`,
`status` (`TODO`, `IN_PROGRESS`, `DONE`; starts as `TODO`),
`priority` (`LOW`, `MEDIUM`, `HIGH`; starts as `MEDIUM`),
`dueDate` (for example `2026-12-01`), `createdAt`, `updatedAt`

### Example

```bash
curl -X POST localhost:8080/api/projects \
  -H 'Content-Type: application/json' \
  -d '{"name":"Garden","description":"Spring jobs"}'
```

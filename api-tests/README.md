# API tests

Black-box tests for the project-manager API. They send real HTTP requests to a running copy of the app (REST Assured + JUnit 6), so they test it the way a client would. Built with Maven, using the wrapper in the repo root.

## Running

Start the app, then run the tests from the repo root.

```bash
# Start the app with a throwaway database (recommended, keeps test data out of your real one)
DB_PATH=/tmp/pm-api-tests.db ./mvnw spring-boot:run
# ...or with Docker (uses your real data/ folder)
docker compose up --build -d --wait

./mvnw -f api-tests/pom.xml test                    # all tests
./mvnw -f api-tests/pom.xml test -Dgroups=projects  # one suite
```

Tests target `http://localhost:8080` by default. To point them elsewhere, set `BASE_URL=...` or pass `-DbaseUrl=...`.

## Suites

Each test class has a JUnit `@Tag`, and the tag is the suite name.

| Suite | What it covers |
| --- | --- |
| `smoke` | App is up (`GET /health`) |
| `projects` | `/api/projects` create, read, list, update, delete and validation |
| `unit` | Checks on the test code itself (every test class has a `@Tag`) |

```bash
./api-tests/scripts/run-suite.sh projects   # smoke | projects | unit | all
```

**Adding a suite:** tag the class with `@Tag("name")`, add the name to the list in `scripts/run-suite.sh`, and add a row to the table above. A test class without a `@Tag` fails the build.

## Writing tests

- Send every request through `ApiClient`, and take paths from `Endpoints`. Never call RestAssured directly or write a path inline.
- Class annotations are `@Tag("<suite>")` and `@ExtendWith(ResponseLoggingExtension.class)`. The extension prints the request and response only when a test fails.
- Every test gets a `@DisplayName` such as `"GET /api/projects/{id} returns 404 for an unknown id"`, plus a `@Timeout` from `TestTimeouts`: `S` for a simple GET, `M` for a single write, `L` for multi-step tests.
- Lay each test out as arrange / act / assert, one behaviour per test. Check the status code first, then the fields that matter.
- Each test creates its own data. Name projects with `ProjectRequest.uniqueName()`, which starts with `[api-test] `, and never rely on existing data or on the order tests run in.

## Test data cleanup

`TestDataListener` runs once per test run, with no setup needed:

- **At the start of a local run** (skipped in CI), it deletes `[api-test]` projects older than one hour, which a killed run might have left behind. It then warns if the target has other projects, meaning you're testing against real data.
- **At the end of the run,** it deletes every project the run created through `ApiClient`, and their tasks with them.

## Reports and tooling

```bash
./mvnw -f api-tests/pom.xml allure:serve        # open the Allure report (requests/responses attached)
./mvnw -f api-tests/pom.xml allure:report       # -> target/site/allure-maven-plugin/index.html
./mvnw -f api-tests/pom.xml spotless:apply      # format code (palantir-java-format)
./api-tests/scripts/run-stability-check.sh 'ProjectsTests#renameProject' 5   # run a test N times before merging
```

The first Allure command downloads Node.js and Allure into `api-tests/.allure/`, which is gitignored.

## CI

The `api-tests` job in `.github/workflows/ci.yml` runs after the unit tests. It starts the app with Docker Compose and runs `verify`, which fails on any test failure or unformatted code. The Allure report is uploaded as the `api-test-reports` artifact, and results appear on the PR as "API Tests".

# API test triage runbook

What to do when the API tests fail.

## 1. Where failures show up

- **Pull requests:** the **Run API tests against the running app** job fails, and so does the **API Tests** check (**Checks** tab). The PR can't be considered green until it passes.
- **`main`:** the CI badge in the root README turns red, and the published report at <https://mchlwall.github.io/project-manager/> shows the failures.
- **Locally:** Maven prints `BUILD FAILURE`, with `Failures` or `Errors` above 0 in the summary.

## 2. Find the details

1. **API Tests check:** gives the list of failed tests and their assertion messages. Start here.
2. **Job log**, in the **Run the API tests** step:
   - Failed tests print `Test failed: <name>` followed by the status code, request body and response body, logged by `ResponseLoggingExtension`.
   - If the app itself misbehaved, the **Show the app's logs** step has its output.
3. **Allure report:** for `main`, use the Pages link above. For a PR, download the `api-test-reports` artifact and open `site/allure-maven-plugin/index.html`. Each test has its full HTTP requests and responses attached.

## 3. Re-run

- **In CI:** on the run page, click **Re-run jobs**, then **Re-run failed jobs**.
- **Locally:** start the app (see the [README](../README.md#running)), then run just the failing test:

  ```bash
  ./mvnw -f api-tests/pom.xml test -Dtest='ProjectsTests#renameProject'
  ```

- **Suspected flaky test:** run it several times in a row. A test that fails only sometimes is flaky, not a real bug.

  ```bash
  ./api-tests/scripts/run-stability-check.sh 'ProjectsTests#renameProject' 10
  ```

## 4. Common failures

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| Every test fails with `Connection refused` | App not running, or a different `BASE_URL` | Locally: start the app and check `curl localhost:8080/health`. In CI: read **Show the app's logs** |
| CI fails at **Start the app and wait for health check** | App crashed on startup or never became healthy | `docker compose logs`, or run `docker compose up --build` locally and watch the output |
| `TestTimedOutException` | Slow app or runner | Re-run first. If it keeps happening, move the test to the next `TestTimeouts` tier (S, M, L) |
| `project name already exists` where a test didn't expect it | Two tests used the same name | Every test must use `ProjectRequest.uniqueName()` |
| Passes alone, fails in a full run (or the reverse) | Tests depending on each other or on existing data | Make the test create everything it needs, and don't assert on totals such as list sizes |
| `TestTagsTest` fails | A test class has no `@Tag` | Add a suite tag, see [Suites](../README.md#suites) |
| `spotless:check` fails in `verify` | Code not formatted | `./mvnw -f api-tests/pom.xml spotless:apply`, then commit |
| **Build the Allure report** step fails | Allure's Node.js download failed | Re-run. Test results themselves are unaffected |
| **Publish the API test report** job fails | GitHub Pages isn't enabled | **Settings → Pages → Source: GitHub Actions** |

## 5. Real bug or broken test?

- **The app is wrong:** the response doesn't match the documented behaviour in the root README. Open an issue with:
  - the test name;
  - the request and response, from the job log or the Allure report;
  - the expected behaviour.

  Fix the app, not the test.
- **The test is wrong, or the behaviour changed on purpose:** update the test in the same PR as the app change.
- **The test is flaky and can't be fixed right away:** quarantine it with `@Disabled("<reason> - <issue link>")` so it stops blocking CI, and fix it soon. Never quarantine a test that's catching a real bug.

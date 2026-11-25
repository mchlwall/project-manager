# End-to-End (E2E) Tests

Browser-based end-to-end tests built with [Playwright](https://playwright.dev).

## Prerequisites

- **Node.js** (v18 or higher)
- **[Docker](https://docs.docker.com/get-docker/) or Rancher Desktop** running locally

## First-Time Setup

Run these commands inside the `e2e/` directory:

```bash
npm install
npx playwright install chromium
```

> **Note:** `npx playwright install` downloads the browser binaries to your global user cache (`~/Library/Caches/ms-playwright` on macOS). You will need to re-run this on a fresh machine even if `node_modules` is present.

## Running Tests

Ensure Docker or Rancher Desktop is running before executing tests:

```bash
npx playwright test                 # Run all tests headless
npx playwright test --ui            # Open interactive Playwright UI Mode
npx playwright test --list          # List all detected tests without running them
npx playwright show-report          # View the last HTML test report
```

To clean up the test container afterward:

```bash
docker compose -p project-manager-e2e down
```

## Troubleshooting

- **`docker.sock: connect: no such file or directory` / `Process from config.webServer was not able to start`**  
  Make sure Docker Desktop or Rancher Desktop is open and its engine is running before running Playwright.
- **`Executable doesn't exist`**  
  Run `npx playwright install` to download the required browser binaries.

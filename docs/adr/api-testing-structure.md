0001-one-repo-for-app-and-api-tests.md

- **Context:** the API suite is deployment-agnostic and could live anywhere; placed it here inside the one repo for ease but it could be a separate repo targeting a service
- **Decision:** one repo, app/ and api-tests/ side by side. One clone gives a complete demo, code and tests change in the same PR
- **Consequences:** Can be made consumable if another tool wants to utilize the testing tool
- **Status:** Accepted
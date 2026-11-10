0002-restassured-over-postman-and-pact.md

- **Context:** what the suite must do - run in CI, flag failures, present readible reporting
- **Decision:** chose REST Assured over Postman/Pact due to compatibility with Java and out-of-the-box test reporting with JUnit
- **Consequences:** more code per test
- **Status:** Accepted
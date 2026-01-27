# Wikipedia Finder (backend)

Quick guide: build, tests, format and style checks

- Run tests and checks (includes style):

```bash
./gradlew clean check
```

- Run only unit tests:

```bash
./gradlew test
```

- Auto-format Java sources (Google Java Format via Spotless):

```bash
./gradlew spotlessApply
```

- Validate formatting without applying changes:

```bash
./gradlew spotlessCheck
```

Notes:
- Spotless is configured in `build.gradle` to use google-java-format. The `check` task depends on `spotlessCheck` so style violations will fail the build.
- Prefer using the instance API for `BFS` (create `new BFS()` or inject it) rather than static convenience methods.

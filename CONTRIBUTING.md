# Contributing to Wikipedia Path Finder

Thank you for your interest in contributing to this project!

## Commit Message Format

This repository uses [Conventional Commits](https://www.conventionalcommits.org/) to ensure consistent and meaningful commit messages. All commits must follow this format:

```
<type>(<scope>): <subject>
```

### Format Breakdown

- **type**: The type of change (required)
- **scope**: The area of the codebase affected (optional)
- **subject**: A brief description of the change (required)

### Allowed Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, etc.)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests or correcting existing tests
- **build**: Changes that affect the build system or external dependencies
- **ci**: Changes to CI configuration files and scripts
- **chore**: Other changes that don't modify src or test files
- **revert**: Reverts a previous commit

### Examples

```
feat(frontend): add loading spinner to search
fix(backend): resolve BFS infinite loop edge case
docs(readme): update installation instructions
chore(deps): update dependencies
```

### Validation

Commits are automatically validated using [commitlint](https://commitlint.js.org/). If your commit message doesn't follow the format, the commit will be rejected.

## Getting Started

1. Fork the repository
2. Clone your fork
3. Create a new branch for your feature or fix
4. Make your changes
5. Commit using the conventional commit format
6. Push to your fork and submit a pull request

## Questions?

If you have any questions, feel free to open an issue!

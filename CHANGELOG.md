# ruby-lsp Changelog

## [[Unreleased]]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [[0.0.5]] - 2024-08-12

### Added

- Support for "standardrb" formatter
- Add support for 2024.x versions

### Fixed

- Fixed generation of change notes

## [[0.0.4]] - 2024-08-08

### Changed

- Dependencies - downgrade `org.jetbrains.kotlin.jvm` to `1.9.24`
- Upgrade Gradle Wrapper to `8.8`
- Dependencies - upgrade `org.jetbrains.kotlinx.kover` to `0.8.1`
- Removed the confirmation of disabling a Ruby LSP feature
- Added support for resetting Ruby LSP features to default (https://github.com/vitallium/intellij-plugin-ruby-lsp/issues/20)
- Added "typeHierarchy" feature to the list of default features
- Added support for running Ruby LSP via "bundler"

## [[0.0.3]] - 2024-06-08

### Added

- A dialog for adding a new feature now validates whether the entered feature name is valid and the feature name does not already exist in the enabled list

### Removed

- Action "Restart Ruby LSP Server" from the "Tools" menu
- Excessive notifications when Ruby LSP server restarts
- Configuration setting for restarting Ruby LSP on crash

## [0.0.2] - 2024-05-17

### Added

- Ruby LSP features are configurable now
- Add setting to toggle experimental Ruby LSP features
- Added notifications when Ruby LSP server is stopped or restarted

### Fixed

- Fixed the `experimental` command line param for Ruby LSP
- Fixed plugin icon

## [0.0.1] - 2024-05-17

### Added

- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

[[Unreleased]]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.5...HEAD
[0.0.5]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.4...v0.0.5
[0.0.4]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/vitallium/intellij-plugin-ruby-lsp/commits/v0.0.1

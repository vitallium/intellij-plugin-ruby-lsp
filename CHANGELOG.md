# ruby-lsp Changelog

## [[Unreleased]]

### Added

- Added `Run Test` command for code insights
- Added `Run Test in Terminal` command for code insights

### Changed

- Migrate to `lsp4ij`
- Update `lsp4ij` version to v0.19.0
- Update JVM to `jbr-21`
- Update `gradle` to v9.3.1

### Deprecated

### Removed

### Fixed

### Security

## [0.2.3] - 2025-09-17

### Changed

- Change since build to `251` (2025.1)
- Update Ruby plugin version to `251.23774.435`
- Upgrade Gradle Wrapper to `9.0.0`
- Remove obsolete `pluginSinceBuild` property

### Removed

- Remove fixed plugin verifier version

## [0.1.3] - 2025-08-30

### Added

- Support for `252.*` builds

## [0.1.2] - 2025-08-24

### Changed

- Update `gradle` to v8.14
- Remove unused `gemScriptArgsBuilder`
- Upgrade Java version from 17 to 21

## [0.1.1] - 2025-01-25

### Added

- Support for `251.*` EAP builds
- Add user-friendly names to the formatter options

## [0.1.0] - 2025-01-15

### Added

- Automatically ignore the `.ruby-lsp` directory

### Removed

- Running experimental versions of `ruby-lsp` was removed.

### Fixed

- If experimental features are enabled, add `addon_detection` capability to the `initialization_options`.

## [0.0.9] - 2024-11-04

### Added

- Support EAP versions.

## [0.0.8] - 2024-09-22

### Fixed

- Fixed resetting of features and code actions

## [0.0.7] - 2024-09-03

### Added

- Add support for toggling code actions.

## [0.0.6] - 2024-08-27

### Added

- Support for "rubyfmt" formatter
- Display a warning when a Ruby LSP formatter is available via an addon.

## [0.0.5] - 2024-08-12

### Added

- Support for "standardrb" formatter
- Add support for 2024.x versions

### Fixed

- Fixed generation of change notes

## [0.0.4] - 2024-08-08

### Changed

- Dependencies - downgrade `org.jetbrains.kotlin.jvm` to `1.9.24`
- Upgrade Gradle Wrapper to `8.8`
- Dependencies - upgrade `org.jetbrains.kotlinx.kover` to `0.8.1`
- Removed the confirmation of disabling a Ruby LSP feature
- Added support for resetting Ruby LSP features to default (https://github.com/vitallium/intellij-plugin-ruby-lsp/issues/20)
- Added "typeHierarchy" feature to the list of default features
- Added support for running Ruby LSP via "bundler"

## [0.0.3] - 2024-06-08

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

[[Unreleased]]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.2.3...HEAD
[0.2.3]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.1.3...v0.2.3
[0.1.3]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.9...v0.1.0
[0.0.9]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.8...v0.0.9
[0.0.8]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.7...v0.0.8
[0.0.7]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.6...v0.0.7
[0.0.6]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.5...v0.0.6
[0.0.5]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.4...v0.0.5
[0.0.4]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/vitallium/intellij-plugin-ruby-lsp/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/vitallium/intellij-plugin-ruby-lsp/commits/v0.0.1

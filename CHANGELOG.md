# Changelog

All notable changes to this project will be documented in this file.

## [1.0.1] - 2026-03-31

### Added
- Maven Central publishing support (GPG signing, source/javadoc JARs, central-publishing-maven-plugin)
- POM metadata required by Central: `url`, `licenses`, `developers`, `scm`
- `MAVEN-CENTRAL-PUBLISH.md` publishing guide
- `VERSION.md` and `CHANGELOG.md`

### Changed
- Bumped version from 1.0.0 to 1.0.1

## [1.0.0] - 2026-03-02

### Added
- Initial release of fart-maven-plugin
- Maven plugin that plays fart sounds during builds, inspired by fartscroll.js
- GitHub Actions CI workflow
- Test suite with JUnit 5 and Mockito
- JaCoCo code coverage with badge generation
- Dogfood profile (`-Pdogfood`) to use the plugin on itself during local builds

# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 3.2.0 - 2025-04-15
### Added
- CSV export for repository pushlog entries
- Migrate pushlog-plugin data structure to SQLite

### Changed
- Improved label for push log entry

## 3.1.0 - 2024-12-05
### Added
- Timestamp of first push is shown in "pushed by" contributor row

## 3.0.0 - 2024-09-11
### Changed
- Changeover to AGPLv3 license

## 2.2.2 - 2022-11-03
### Fixed
- Block of event bus ([#8](https://github.com/scm-manager/scm-pushlog-plugin/pull/8))

## 2.2.1 - 2022-10-26
### Fixed
- Prevent writing multiple log entries ([#7](https://github.com/scm-manager/scm-pushlog-plugin/pull/7))

## 2.2.0 - 2021-03-12
### Changed
- Data is not stored using the repository id as key ([#4](https://github.com/scm-manager/scm-pushlog-plugin/pull/4))

## 2.1.1 - 2020-07-01
### Fixed
- Add missing translations

## 2.1.0 - 2020-06-18
### Changed
- Add "Pushed-by" as contributor to changset ([#3](https://github.com/scm-manager/scm-pushlog-plugin/pull/3))

## 2.0.0 - 2020-06-04
### Changed
- Changeover to MIT license ([#2](https://github.com/scm-manager/scm-pushlog-plugin/pull/2))
- Rebuild for api changes from core


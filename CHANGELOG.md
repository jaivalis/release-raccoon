## ⚙️ All important changes to this project are tracked here:

## [Unreleased](https://github.com/jaivalis/release-raccoon/compare/0.3.10...jdevelop)

## [0.3.11](https://github.com/jaivalis/release-raccoon/compare/0.3.9...0.3.10) - 20/09/2024

- Fix: Nullable spotifyUrl on search results

## [0.3.10](https://github.com/jaivalis/release-raccoon/compare/0.3.9...0.3.10) - 18/09/2024

- Feature: `artists/recommended` UI pagination

## [0.3.9](https://github.com/jaivalis/release-raccoon/compare/0.3.8...0.3.9) - 18/09/2024

- Bugfix: MusicbrainzScraper ConstraintViolationException

## [0.3.8](https://github.com/jaivalis/release-raccoon/compare/0.3.7...0.3.8) - 31/08/2024

- Heroku workflow tag deploy automation

## [0.3.7](https://github.com/jaivalis/release-raccoon/compare/0.3.6...0.3.7) - 24/08/2024

- Bugfix: follow recommended Artist
- Added RaccoonUser hashCode override

## [0.3.6](https://github.com/jaivalis/release-raccoon/compare/0.3.5...0.3.6) - 24/08/2024

- Fixed unique Artist name constraints for Postgresql

## [0.3.5](https://github.com/jaivalis/release-raccoon/compare/0.3.4...0.3.5) - 21/08/2024

- Added maven-release-plugin

## [0.3.4](https://github.com/jaivalis/release-raccoon/compare/0.3.2...0.3.3) - 21/08/2024

- fixed compilation errors

## [0.3.3](https://github.com/jaivalis/release-raccoon/compare/0.3.2...0.3.3) - 21/08/2024

- Bugfix: Fixed config name
 
## [0.3.2](https://github.com/jaivalis/release-raccoon/compare/0.3.1...0.3.2) - 21/08/2024

- Added debug logs

## [0.3.1](https://github.com/jaivalis/release-raccoon/compare/0.3.0...0.3.1) - 21/08/2024

- Redirect on /me for UI

## [0.3.0](https://github.com/jaivalis/release-raccoon/compare/0.2.7...0.3.0) - 01/08/2024

- default postgres db

## [0.2.7](https://github.com/jaivalis/release-raccoon/compare/0.2.6...0.2.7) - 24/06/2024

- Notification fixes
- Add carbon-badge

## [0.2.6](https://github.com/jaivalis/release-raccoon/compare/0.2.5...0.2.6) - 23/06/2024

- Moved integration tests into release-raccoon-app module
- Upgrade Quarkus to to 3.11.3
- Fix some trunk.io errors
- Last.fm taste scrape on last 6 months only to improve performance

## [0.2.5](https://github.com/jaivalis/release-raccoon/compare/0.2.5...0.2.4) - 18/06/2024

- Bugfix: Performance issue on taste scrape

## [0.2.4](https://github.com/jaivalis/release-raccoon/compare/0.2.4...0.2.3) - 18/06/2024

- heroku fixes
- Bugfix: SpotifyReleaseMapper `AlbumType`

## [0.2.3](https://github.com/jaivalis/release-raccoon/compare/0.2.3...0.2.2) - 01/06/2024

- dependency version bumps

## [0.2.2](https://github.com/jaivalis/release-raccoon/compare/0.2.2...0.2.1) - 04/01/2024

- Bump Quarkus version

## [0.2.1](https://github.com/jaivalis/release-raccoon/compare/0.2.0...0.2.1) - 20/06/2023

- Recommended artists

## [0.2.0](https://github.com/jaivalis/release-raccoon/compare/0.1.0...0.2.0) - 20/06/2023

- Recommended artists

## [0.1.1](https://github.com/jaivalis/release-raccoon/compare/0.1.0...0.1.1) - 03/06/2023

- Github actions for native image

## [0.1.0](https://github.com/jaivalis/release-raccoon/compare/0.0.1...0.1.0) - 31/05/2023

- Upgrade to Java 17
- Upgrade Quarkus to 2.8.3.Final
- Integration to Musicbrainz for Release scrape
- Integration to Musicbrainz for Artist search
- Unfollow is a `DELETE` call returning no-content
- Artist Search
- Welcome to release-raccoon email upon signup
- Adding an artist to raccoonUser taste should potentially trigger an update email
- Docker builds as part of build
- Added sentry.io integration
- Use LRO for release-scrape (http://restalk-patterns.org/long-running-operation-polling.html)

## 0.0.1

- Introduce Release-Raccoon

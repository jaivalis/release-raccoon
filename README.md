# ReleaseRaccoon 🦝

![Github Actions build](https://github.com/jaivalis/release-raccoon/actions/workflows/build-branches.yml/badge.svg)
[![Known Vulnerabilities](https://snyk.io/test/github/jaivalis/release-raccoon/badge.svg)](https://snyk.io/test/github/jaivalis/release-raccoon)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jaivalis_release-raccoon&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jaivalis_release-raccoon)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jaivalis_release-raccoon&metric=coverage)](https://sonarcloud.io/summary/new_code?id=jaivalis_release-raccoon)

A personalized music release newsletter that keeps you updated with new releases from your favorite artists.

## What is ReleaseRaccoon?

ReleaseRaccoon is a smart music discovery service that:
- 🎵 Tracks new releases from artists you follow
- 📧 Sends personalized digest emails with the latest music
- ⚡ Built with modern Java technologies for performance and reliability

## Features

- **Personalized Notifications**: Get email digests about new releases from artists you follow
- **Multi-Source Data**: Aggregates music data from Spotify, Last.fm, and MusicBrainz
- **Smart Scheduling**: Daily automated checks for new releases
- **Search & Browse**: Full-text search through artists and releases

## Getting Started

### For Users

1. **Sign Up**: Create an account and authenticate via Keycloak
2. **Follow Artists**: Search and follow your favorite artists
3. **Set Preferences**: Configure your notification settings
4. **Receive Updates**: Get daily email digests about new releases

## Technology Stack

- **Backend**: Java 21 with Quarkus framework
- **Database**: PostgreSQL with Liquibase migrations
- **Search**: Elasticsearch via Hibernate Search
- **Authentication**: Keycloak OIDC
- **APIs**: Spotify, Last.fm, MusicBrainz integration
- **Email**: Automated digest notifications
- **Testing**: JUnit 5, Testcontainers, RestAssured

## Quick Start

```shell
# Clone the repository
git clone https://github.com/jaivalis/release-raccoon.git

# Start local development environment
cd release-raccoon
source release-raccoon-app/.env
docker compose --env-file ./release-raccoon-app/.env -f docker/docker-compose.yml up -d

# Run the application
./mvnw compile quarkus:dev -pl release-raccoon-app
```

For detailed setup instructions, see the [Developer Guide](DEVELOPER.md).

## License

This project is licensed under the MIT License - see the LICENSE file for details.

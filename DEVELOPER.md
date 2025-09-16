# Developer Guide

## Local Development

See [guide](dev-guides/local-development.md).

## Build Commands

### Running the Application

```shell
# Start dependencies (DB, Keycloak, Elasticsearch)
source release-raccoon-app/.env
docker compose --env-file ./release-raccoon-app/.env -f docker/docker-compose.yml up -d

# Run in dev mode with live reload
./mvnw compile quarkus:dev -pl release-raccoon-app

# Package the application
./mvnw package
```

### Creating a Native Executable

You can create a native executable using:

```shell
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell
./mvnw package -Pnative -Dquarkus.native.container-build=true
./mvnw package -Pnative -pl release-raccoon-app
```

You can then execute your native executable with: `./target/release.com.raccoon-0.0.1-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

## Testing

```shell
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify

# Run tests for a specific module
./mvnw test -pl release-raccoon-app

# Run a single test
./mvnw test -Dtest=YourTestClass#testMethod -pl release-raccoon-app
```

## Deploying to Heroku

Build the project to get the jar:

```shell
./mvnw package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
```

> This needs to be run from the project root otherwise the following property (pointing to the resource dir) needs to be configured accordingly in [application.properties](release-raccoon-app/src/main/resources/application.properties):
> -H:IncludeResources='${PWD}/release-raccoon-app/src/main/resources/META-INF/resources/.\*'

Login to heroku docker registry (if you haven't already), build the docker image, push it to the heroku repository and deploy to heroku:

```shell
heroku container:login
cp build/release-raccoon-app-0.0.1-SNAPSHOT-runner ./docker && pushd docker && docker build -f Dockerfile.native -t registry.heroku.com/release-raccoon/web .
docker push registry.heroku.com/release-raccoon/web
heroku container:release web --app release-raccoon && popd
```

Check the logs for a successful start:

```shell
heroku logs --app release-raccoon --tail
```

## Project Structure

### Module Structure
- **parent**: Parent POM with dependency management and plugin configurations
- **raccoon-common**: Shared utilities and common code
- **raccoon-entities**: JPA entities and database models
- **scraping**: Music data scrapers (Spotify, Last.fm, MusicBrainz)
- **release-raccoon-app**: Main Quarkus application with REST endpoints
- **report-aggregate**: Jacoco coverage aggregation

### Key Technologies
- **Framework**: Quarkus
- **Java**: 21
- **Database**: PostgreSQL with Liquibase migrations
- **Search**: Elasticsearch via Hibernate Search
- **Authentication**: Keycloak OIDC
- **Build**: Maven
- **Testing**: JUnit 5, Testcontainers, RestAssured, Mockito

### Core Components

**Scrapers** (in `scraping` module):
- `SpotifyScraper`: Fetches data from Spotify API
- `LastfmScraper`: Fetches data from Last.fm API  
- `MusicbrainzScraper`: Fetches data from MusicBrainz API
- `ReleaseScraper`: Orchestrates scraping from all sources

**Services** (in `release-raccoon-app`):
- `NotifyService`: Handles email digest notifications
- `RaccoonMailer`: Email sending functionality
- `UserService`: User management and preferences
- `SearchService`: Full-text search functionality

**Scheduled Jobs**:
- Release scraping: Daily at 10:15 AM (configurable via `RELEASE_SCRAPE_CRON`)
- Email notifications: Daily at 11:15 AM (configurable via `NOTIFY_CRON`)

### Database Schema
Managed via Liquibase migrations in `release-raccoon-app/src/main/resources/db/changelog/`

## Environment Configuration

Key environment variables (see `.env.dist`):
- `DB_USERNAME`, `DB_PASSWORD`: Database credentials
- `KEYCLOAK_PORT`: Keycloak server port
- `NOTIFY_CRON`: Notification schedule
- `RELEASE_SCRAPE_CRON`: Scraping schedule
- `QUARKUS_LOG_SENTRY_DSN`: Sentry error tracking

## Development Setup Requirements

1. Docker for running dependencies
2. Java 21
3. Maven
4. Configure Keycloak realm and generate client secret
5. Set up Liquibase tables (see dev-guides/local-development.md)

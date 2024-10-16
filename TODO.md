- Use Oracle cloud free Arm VM for building native [reference](https://www.youtube.com/watch?v=fh009OWr8Ks)
- Add `Unsubscribe` link to emails
- `Delete account` functionality
- Look into @Location as replacement for [QuteTemplateLoader](release-raccoon-app/src/main/java/com/raccoon/templatedata/QuteTemplateLoader.java)
- Per DomainDrivenDesign book getOrCreate is an anti-pattern. It is useful to know whether the entity existed or was just created. Separate those.
- Separate the domain from the Controllers and the Quarkus Qute dependencies
- Create DTOs for every Entity class (Decouple)
- Cucumber BDD tests
- Remove @Data from entities!!
- api versioning, nest everything under `/api/v1`
- ReleaseScraper implementers should not contain persistence layer dependencies, should create Entities and leave persistence for another anti-corruption layer
- Database reconciliation/enrichment. Missing Artist fields (SpotifyUri, LastfmUri) should be populated
- Dependency cleanup: org.apache.maven.plugins:maven-dependency-plugin:analyze-only

# Additional Integrations

- Add integrations:
  - [bandcamp](https://bandcamp.com) (release scraper)
  - [Rate Your Music](https://rateyourmusic.com) (release/taste scrape)

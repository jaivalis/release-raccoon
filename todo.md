
* Java 17
* Upgrade to latest Quarkus 2.5.4 [upgrade guide](https://github.com/quarkusio/quarkus/wiki/Migration-Guide-2.5)
* Add Qute `base.html` per [example](https://www.morling.dev/blog/quarkus-qute-test-ride/)
* Use Oracle cloud free Arm VM for building native [reference](https://www.youtube.com/watch?v=fh009OWr8Ks)
* Add `Unsubscribe` link to emails
* `Delete account` functionality
* Look into @Location as replacement for [QuteTemplateLoader](release-raccoon-app/src/main/java/com/raccoon/templatedata/QuteTemplateLoader.java)
* Per DomainDrivenDesign book getOrCreate is an anti-pattern. It is useful to know whether the entity existed or was just created. Separate those.

# Additional Integrations
* Add integrations:
  * [bandcamp](https://bandcamp.com) (release scraper)
  * [Rate Your Music](https://rateyourmusic.com) (release/taste scrape)


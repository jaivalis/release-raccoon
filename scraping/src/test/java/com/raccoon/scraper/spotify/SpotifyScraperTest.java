package com.raccoon.scraper.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.repository.ArtistReleaseRepository;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;
import com.wrapper.spotify.enums.AlbumType;
import com.wrapper.spotify.enums.ModelObjectType;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.IntStream;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyScraperTest {

    SpotifyScraper scraper;

    @Mock
    ArtistFactory artistFactoryMock;
    @Mock
    ArtistRepository artistRepositoryMock;
    @Mock
    ArtistReleaseRepository artistReleaseRepository;
    @Mock
    ReleaseRepository releaseRepositoryMock;
    @Mock
    RaccoonSpotifyApi raccoonSpotifyApiMock;

    @Mock
    Paging<AlbumSimplified> albumSimplifiedPagingMock;
    @Mock
    SpotifyUserAuthorizer authorizer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        scraper = new SpotifyScraper(
                artistFactoryMock,
                artistRepositoryMock,
                artistReleaseRepository,
                releaseRepositoryMock,
                raccoonSpotifyApiMock
        );
    }

    private static final SecureRandom random = new SecureRandom();

    public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    private AlbumSimplified[] mockSpotifyAlbums(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
                .mapToObj(this::mockSpotifyAlbum)
                .toArray(AlbumSimplified[]::new);
    }

    private AlbumSimplified mockSpotifyAlbum(int i) {
        return new AlbumSimplified.Builder()
                .setId(String.valueOf(i))
                .setArtists(
                        new ArtistSimplified.Builder()
                                .setName("artistName" + i)
                                .setUri("artistUri" + i)
                                .build()
                )
                .setType(ModelObjectType.ALBUM)
                .setAlbumType(randomEnum(AlbumType.class))
                .setUri("releaseUri" + i)
                .setReleaseDate(LocalDate.now().toString())
                .build();
    }

    // ============================================= ReleaseScraper API ============================================= //
    @Test
    void scrapeReleases() throws IOException, InterruptedException, ParseException, SpotifyWebApiException {
        final var limit = 10;
        when(raccoonSpotifyApiMock.fetchNewReleasesPaginated(anyInt()))
                .thenReturn(albumSimplifiedPagingMock);
        when(albumSimplifiedPagingMock.getItems())
                .thenReturn(mockSpotifyAlbums(0, limit));

        final var releases = scraper.scrapeReleases(Optional.of(limit));

        assertEquals(limit, releases.size());
    }

    @Test
    void scrapeReleasesException() throws ParseException, IOException, InterruptedException, SpotifyWebApiException {
        final var limit = 10;
        when(raccoonSpotifyApiMock.fetchNewReleasesPaginated(anyInt()))
                .thenThrow(IOException.class);

        assertThrows(IOException.class,
                () -> scraper.scrapeReleases(Optional.of(limit)));
    }

    @Test
    void scrapeReleasesInterruptedException() throws ParseException, IOException, InterruptedException, SpotifyWebApiException {
        final var limit = 10;
        when(raccoonSpotifyApiMock.fetchNewReleasesPaginated(anyInt()))
                .thenThrow(InterruptedException.class);

        assertThrows(InterruptedException.class,
                () -> scraper.scrapeReleases(Optional.of(limit)));
    }

    @Test
    void processRelease() {
        scraper.processRelease(mockSpotifyAlbum(100));

        verify(artistRepositoryMock).findByNameOptional("artistName" + 100);
        verify(artistRepositoryMock).persist(any(Artist.class));
    }

    @Test
    @DisplayName("Return empty if url is present")
    void processExistingRelease() {
        var album = mockSpotifyAlbum(100);
        when(releaseRepositoryMock.findBySpotifyUriOptional(album.getUri()))
                .thenReturn(Optional.of(new Release()));

        final var release = scraper.processRelease(album);

        assertTrue(release.isEmpty());
    }

    @Test
    @DisplayName("Can only process AlbumSimplified")
    void processNonAlbumSimplified() {
        final var release = new Release();
        assertThrows(IllegalArgumentException.class,
                () -> scraper.processRelease(release));
    }
    // ========================================== End of ReleaseScraper API ========================================= //
    // ============================================== TasteScraper API ============================================== //

    @Test
    void testFetchTopArtists() throws IOException, ParseException, SpotifyWebApiException {
        when(authorizer.executeGetUsersTopArtists(anyInt()))
                .thenThrow(IOException.class);

        assertEquals(0, scraper.fetchTopArtists(authorizer).size());
    }

    @Test
    void processArtistSuccessful() {
        var uri = "test-uri";
        var name = "test-name";
        Artist artistStub = new Artist();
        artistStub.setName(name);
        com.wrapper.spotify.model_objects.specification.Artist spotifyArtist =
                new com.wrapper.spotify.model_objects.specification.Artist.Builder()
                        .setName(name)
                        .setUri(uri)
                        .build();
        when(artistFactoryMock.getOrCreateArtist(name))
                .thenReturn(artistStub);

        final var artist = scraper.processArtist(spotifyArtist);

        assertEquals(name, artist.getName());
        assertEquals(uri, artist.getSpotifyUri());
        verify(artistFactoryMock).getOrCreateArtist(name);
        verify(artistRepositoryMock).persist(any(Artist.class));
    }

    @Test
    @DisplayName("IllegalArgumentException if type is not com.wrapper.spotify.model_objects.specification.Artist")
    void processArtistNonSpotifyType() {
        Object wrongType = new Object();

        assertThrows(IllegalArgumentException.class,
                () -> scraper.processArtist(wrongType));
    }
    // =========================================== End of TasteScraper API ========================================== //

}
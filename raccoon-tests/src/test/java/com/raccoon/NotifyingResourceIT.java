package com.raccoon;

import com.raccoon.common.ElasticSearchTestResource;
import com.raccoon.entity.repository.UserArtistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comments are not allowed in the `import-test.sql` file so clarifying here. These tests depend on
 * the following data being present in the db:
 *
 * INSERT INTO
 *     Releases
 *     (releaseId, name, type, releasedOn, spotifyUri)
 * VALUES
 *     (300, 'new-release-to-notify', 'ALBUM', (SUBDATE(CURDATE(), 1)), 'spotify:album:zzzzzz');
 *
 * INSERT INTO
 *     RaccoonUser
 *     (user_id, email)
 * VALUES
 *     (300, 'user300@mail.com');
 *
 * INSERT INTO
 *     Artist
 *     (artistId, name)
 * VALUES
 *     (300, 'existentArtist2');
 *
 * INSERT INTO
 *     UserArtist
 *     (user_id, artist_id, hasNewRelease)
 * VALUES
 *     (300, 300, true),
 *     (200, 300, false);
 *
 * INSERT INTO
 *     ArtistRelease
 *     (artist_id, release_id)
 * VALUES
 *     (300, 300);
 */
@Testcontainers
@QuarkusTest
@TestTransaction
@QuarkusTestResource(ElasticSearchTestResource.class)
class NotifyingResourceIT {

    @Inject
    UserArtistRepository userArtistRepository;

    @Inject
    MockMailbox mockMailbox;

    @BeforeEach
    public void setup() {
        mockMailbox.clear();
    }

    @Test
    @DisplayName("Given 1 release from yesterday, notify raccoonUser and unset UserArtist.hasNewRelease")
    void test_should_notify_user_artist_hasNewRelease() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/notify-users")
                .then()
                .statusCode(SC_OK);

        assertEquals(1, mockMailbox.getTotalMessagesSent());
        assertNotNull(mockMailbox.getMessagesSentTo("user300@mail.com"));
        assertEquals(1, mockMailbox.getMessagesSentTo("user300@mail.com").size());
        var uaOptional = userArtistRepository.findByUserIdArtistIdOptional(300L, 300L);
        assertTrue(uaOptional.isPresent());
        assertThat(uaOptional.get().hasNewRelease)
                .as("Release should be marked processed")
                .isFalse();
    }

}

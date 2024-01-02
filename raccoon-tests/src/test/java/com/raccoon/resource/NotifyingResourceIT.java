package com.raccoon.resource;

import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.profile.NotifyingResourceDatabaseProfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestTransaction
@TestProfile(value = NotifyingResourceDatabaseProfile.class)
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
    @TestTransaction
    @DisplayName("Given 1 release from yesterday, notify raccoonUser and unset UserArtist.hasNewRelease")
    void should_notifyUser_and_updateArtistHasNewRelease() {
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

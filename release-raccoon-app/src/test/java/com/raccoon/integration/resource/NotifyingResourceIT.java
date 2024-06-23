package com.raccoon.integration.resource;

import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.integration.profile.NotifyingResourceDatabaseProfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

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

        List<Mail> mailsSent = mockMailbox.getMailsSentTo("user300@mail.com");
        assertThat(mockMailbox.getTotalMessagesSent())
                .isEqualTo(1);
        assertThat(mailsSent)
                .isNotNull()
                .hasSize(1);
        var uaOptional = userArtistRepository.findByUserIdArtistIdOptional(300L, 300L);
        assertThat(uaOptional)
                .isPresent();
        assertThat(uaOptional.get().hasNewRelease)
                .as("Release should be marked processed")
                .isFalse();
    }

}

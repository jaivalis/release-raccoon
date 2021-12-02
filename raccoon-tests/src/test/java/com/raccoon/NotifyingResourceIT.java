package com.raccoon;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.raccoon.entity.repository.UserArtistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@DBRider
@DBUnit(caseSensitiveTableNames = true)
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
    @Order(1)
    @DataSet(value = "datasets/yml/notify.yml")
    @DisplayName("Given 1 release from yesterday, notify user and unset UserArtist.hasNewRelease")
    void test_should_notify_user_artist_hasNewRelease() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/notify-users")
                .then()
                .statusCode(SC_OK);

        assertEquals(1, mockMailbox.getMessagesSentTo("user100@mail.com").size());
        var uaOptional = userArtistRepository.findByUserArtistOptional(100L, 100L);
        assertTrue(uaOptional.isPresent());
        assertFalse(uaOptional.get().getHasNewRelease(), "Release should be marked processed");
    }

}

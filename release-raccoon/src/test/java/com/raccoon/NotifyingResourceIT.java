package com.raccoon;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.notify.MailingService;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.transaction.Transactional;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Testcontainers
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@DBRider
@DBUnit(caseSensitiveTableNames = true)
class NotifyingResourceIT {

    @InjectMock
    MailingService mailingServiceMock;

    @BeforeEach
    public void setup() {
        QuarkusMock.installMockForType(mailingServiceMock, MailingService.class);
        when(mailingServiceMock.send(any(), any(User.class), any())).thenReturn(true);
    }

    @Test
    @Order(1)
    @DataSet(value = "datasets/yml/notify.yml")
    void test_should_notify_user_artist_hasNewRelease() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/notify-users")
                .then()
                .statusCode(SC_OK);

        // exactly once with the expected params
        verify(mailingServiceMock, times(1))
                .send(anyString(), any(User.class), any());
        verify(mailingServiceMock, times(1))
                .send(eq("user1@mail.com"), any(User.class), any());
        UserArtist userArtist = (UserArtist) UserArtist.findByUserArtistOptional(100, 100).get();
        assertThat("Release should be marked processed", !userArtist.getHasNewRelease());
    }

}

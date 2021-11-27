//package com.raccoon.user;
//
//import com.raccoon.registration.RegisteringService;
//
//import org.eclipse.microprofile.jwt.JsonWebToken;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.Optional;
//
//import javax.inject.Inject;
//
//import io.quarkus.test.common.http.TestHTTPEndpoint;
//import io.quarkus.test.junit.QuarkusTest;
//import io.quarkus.test.junit.mockito.InjectMock;
//import io.quarkus.test.security.TestSecurity;
//import io.quarkus.test.security.oidc.Claim;
//import io.quarkus.test.security.oidc.OidcSecurity;
//
//import static com.raccoon.Constants.EMAIL_CLAIM;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//@QuarkusTest()
//@TestHTTPEndpoint(UserProfileResource.class)
//class UserProfileResourceTest {
//
//    @InjectMocks
//    UserProfileResource resource;
//
//    @Mock
//    UserProfileService mockProfileService;
//    @Mock
//    RegisteringService mockRegisteringService;
//    @Mock
//    JsonWebToken mockIdToken;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
////        resource = new UserProfileResource(mockProfileService, mockRegisteringService);
//    }
//
//    @Test
//    @TestSecurity(user = "the coon", roles = "user")
//    @OidcSecurity(claims = {
//            @Claim(key = EMAIL_CLAIM, value = "user@gmail.com")
//    })
//    void registerCallback() {
//        resource.registerCallback();
//
//        verify(mockRegisteringService, times(1)).completeRegistration(any());
//    }
//
//    @Test
//    @TestSecurity(user = "the coon", roles = "user")
//    @OidcSecurity(claims = {
//            @Claim(key = EMAIL_CLAIM, value = "user@gmail.com")
//    })
//    void unfollowArtist() {
//        resource.unfollowArtist(1L);
//
//        verify(mockProfileService, times(1))
//                .unfollowArtist(any(), eq(1L));
//    }
//
//    @Test
//    @TestSecurity(user = "the coon", roles = "user")
//    @OidcSecurity(claims = {
//            @Claim(key = EMAIL_CLAIM, value = "user@gmail.com")
//    })
//    void enableTasteSources() {
//        resource.enableTasteSources(Optional.of("something"), Optional.of(Boolean.TRUE));
//
//        verify(mockProfileService, times(1))
//                .enableTasteSources(any(), eq(Optional.of("something")), eq(Optional.of(Boolean.TRUE)));
//    }
//}
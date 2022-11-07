//package com.raccoon.search;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.Optional;
//
//import io.quarkus.test.common.http.TestHTTPEndpoint;
//import io.quarkus.test.junit.QuarkusTest;
//import io.quarkus.test.junit.mockito.InjectMock;
//import io.quarkus.test.security.TestSecurity;
//import io.quarkus.test.security.oidc.Claim;
//import io.quarkus.test.security.oidc.OidcSecurity;
//
//import static com.raccoon.Constants.EMAIL_CLAIM;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
////@ExtendWith(MockitoExtension.class)
//@QuarkusTest
//@TestHTTPEndpoint(ArtistSearchResource.class)
//class ArtistSearchResourceTest {
//
//    ArtistSearchResource resource;
//
//    @InjectMock
//    SearchService mockSearchService;
//
//    @BeforeEach
//    void setUp() {
//        resource = new ArtistSearchResource(mockSearchService);
//    }
//
//    @Test
//    @TestSecurity(raccoonUser = "username", roles = "raccoonUser")
//    @OidcSecurity(claims = {
//            @Claim(key = EMAIL_CLAIM, value = "raccoonUser@gmail.com")
//    })
//    void searchArtists() {
//        var pattern = "pattern";
//        var limit = Optional.of(99);
//
//        resource.searchArtists(pattern, limit);
//
//        verify(mockSearchService, times(1)).searchArtists("raccoonUser@gmail.com", pattern, limit);
//    }
//
//}

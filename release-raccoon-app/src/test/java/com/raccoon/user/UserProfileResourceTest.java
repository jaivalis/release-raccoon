//package com.raccoon.user;
//
//import com.raccoon.registration.RegisteringService;
//
//import org.eclipse.microprofile.jwt.JsonWebToken;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//class UserProfileResourceTest {
//
//    UserProfileResource resource;
//
//    @Mock
//    UserProfileService mockProfileService;
//    @Mock
//    RegisteringService mockRegisteringService;
//    @Mock
//    JsonWebToken mockToken;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        resource = new UserProfileResource(mockProfileService, mockRegisteringService);
//    }
//
//    @Test
//    void registerCallback() {
//        resource.registerCallback();
//
//        verify(mockRegisteringService, times(1)).completeRegistration(any());
//    }
//
//    @Test
//    void unfollowArtist() {
//        resource.unfollowArtist(1L);
//
//        verify(mockProfileService, times(1))
//                .unfollowArtist(any(), eq(1L));
//    }
//
//    @Test
//    void enableTasteSources() {
//        resource.enableTasteSources(Optional.of("something"), Optional.of(Boolean.TRUE));
//
//        verify(mockProfileService, times(1))
//                .enableTasteSources(any(), eq(Optional.of("something")), eq(Optional.of(Boolean.TRUE)));
//    }
//}
package com.raccoon.search;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.mapping.ArtistSearchResponse;
import com.raccoon.search.impl.HibernateSearcher;
import com.raccoon.search.impl.LastfmSearcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import static com.raccoon.Constants.HIBERNATE_SEARCHER_ID;
import static com.raccoon.Constants.LASTFM_SEARCHER_ID;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    SearchService service;

    @Mock
    HibernateSearcher mockHibernateSearcher;
    @Mock
    LastfmSearcher mockLastfmSearcher;
    @Mock
    Instance<ArtistSearcher> searchers;
    @Mock
    UserRepository userRepository;
    @Mock
    UserArtistRepository userArtistRepository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(searchers.stream()).thenReturn(Stream.of(mockHibernateSearcher, mockLastfmSearcher));
        when(mockLastfmSearcher.getSearcherId()).thenReturn(LASTFM_SEARCHER_ID);
        when(mockHibernateSearcher.getSearcherId()).thenReturn(HIBERNATE_SEARCHER_ID);

        service = new SearchService(searchers, userRepository, userArtistRepository);
    }

    @Test
    @DisplayName("searchArtists(): Invokes search for all searchers")
    void searchArtists() {
        var email = "email";
        var pattern = "pattern";
        var size = Optional.of(10);
        when(mockHibernateSearcher.searchArtist(any(), any())).thenReturn(Collections.emptyList());
        when(mockLastfmSearcher.searchArtist(any(), any())).thenReturn(Collections.emptyList());

        service.searchArtists(email, pattern, size);

        verify(mockHibernateSearcher, times(1)).searchArtist(pattern, size);
        verify(mockLastfmSearcher, times(1)).searchArtist(pattern, size);
    }

    @Test
    @DisplayName("searchArtists(): Populates the result as expected")
    void searchArtistsReturns() {
        var email = "email";
        var pattern = "pattern";
        var size = Optional.of(10);
        ArtistDto stubArtist1 = ArtistDto.builder().name("hibernate").id(3L).build();
        ArtistDto stubArtist2 = ArtistDto.builder().name("hibernate2 followed by user should appear first").id(9L).build();
        ArtistDto stubArtist3 = ArtistDto.builder().name("lastfm").build();
        when(mockHibernateSearcher.searchArtist(pattern, size)).thenReturn(List.of(stubArtist1, stubArtist2));
        when(mockLastfmSearcher.searchArtist(pattern, size)).thenReturn(List.of(stubArtist3));

        //
        var stubUser = new User();
        stubUser.id = 1L;
        var stubArtist = new Artist();
        // The artist that we want returned first (since already followed)
        stubArtist.id = stubArtist2.getId();
        when(userRepository.findByEmail(any())).thenReturn(stubUser);
        var stubUserArtist = new UserArtist();
        stubUserArtist.setUser(stubUser);
        stubUserArtist.setArtist(stubArtist);
        when(userArtistRepository.findByUserIdAndArtistIds(stubUser.id, List.of(stubArtist1.getId(), stubArtist2.getId())))
                .thenReturn(List.of(
                        stubUserArtist
                ));

        ArtistSearchResponse response = service.searchArtists(email, pattern, size);

        assertEquals(3, response.getArtists().size());

        for (var dto : response.getArtists()) {
            if ("hibernate2 followed by user should appear first".equals(dto.getName())) {
                assertTrue(dto.isFollowedByUser());
            } else {
                assertFalse(dto.isFollowedByUser());
            }
        }
        var returnedNames = response.getArtists().stream().map(ArtistDto::getName).toList();
        assertTrue(returnedNames.contains(stubArtist1.getName()));
        assertTrue(returnedNames.contains(stubArtist2.getName()));
        assertTrue(returnedNames.contains(stubArtist3.getName()));
    }

}

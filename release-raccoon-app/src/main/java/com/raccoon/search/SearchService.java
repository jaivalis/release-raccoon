package com.raccoon.search;

import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.mapping.ArtistSearchResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import static com.raccoon.Constants.HIBERNATE_SEARCHER_ID;

@Slf4j
@ApplicationScoped
public class SearchService {

    final List<ArtistSearcher> searchers;
    final UserRepository userRepository;
    final UserArtistRepository userArtistRepository;

    @Inject
    public SearchService(final Instance<ArtistSearcher> searchers,
                         final UserRepository userRepository,
                         final UserArtistRepository userArtistRepository) {
        this.searchers = searchers.stream().toList();
        log.info("Found {} artist searchers in classpath", this.searchers.size());
        this.userRepository = userRepository;
        this.userArtistRepository = userArtistRepository;
    }

    /**
     * Search for an artist against available Searchers
     * @param userEmail user who searches, used to set followedByUser flag of ArtistDto
     * @param pattern pattern to match artist name against
     * @param size search limit per resource (database and lastfm)
     * @return ArtistSearchResponse
     */
    public ArtistSearchResponse searchArtists(final String userEmail,
                                              final String pattern,
                                              final Optional<Integer> size) {
        log.info("Searching for artist {}", pattern);

        Map<String, Collection<ArtistDto>> artistsPerResource = new HashMap<>();

        searchers.parallelStream().forEach(
                searcher -> {
                    var searcherId = searcher.getSearcherId();
                    var results = searcher.searchArtist(pattern, size);
                    log.info("Search hits, source `{}`: {} ", searcherId, results);

                    artistsPerResource.put(searcherId, results);
                }
        );

        return ArtistSearchResponse.builder()
                .artists(postProcessSearchResults(userEmail, artistsPerResource))
                .build();
    }

    List<ArtistDto> postProcessSearchResults(String userEmail, Map<String, Collection<ArtistDto>> perSource) {
        List<ArtistDto> sortedResultList = new ArrayList<>();

        Collection<ArtistDto> hibernateResults = perSource.get(HIBERNATE_SEARCHER_ID);
        if (!hibernateResults.isEmpty()) {
            // Add the artists from the database first
            var followedFlagSet = setAlreadyFollowed(userEmail, hibernateResults);

            sortedResultList.addAll(followedFlagSet);
        }
        perSource.remove(HIBERNATE_SEARCHER_ID);

        for (Map.Entry<String, Collection<ArtistDto>> searcherHits : perSource.entrySet()) {
            sortedResultList.addAll(searcherHits.getValue());
        }

        return sortedResultList;
    }

    /**
     * Updates the followedByUser flag where necessary
     * @param userEmail
     * @param hibernateHits
     * @return
     */
    private List<ArtistDto> setAlreadyFollowed(final String userEmail,
                                               final Collection<ArtistDto> hibernateHits) {
        var searchingUser = userRepository.findByEmail(userEmail);
        var distinctArtistIds = hibernateHits.stream()
                .map(ArtistDto::getId)
                .toList();

        var idsOfArtistsFollowedByUser = idsOfArtistsFollowedByUser(searchingUser.id, distinctArtistIds);

        return hibernateHits.stream()
                .peek(artistDto -> {
                    if (idsOfArtistsFollowedByUser.contains(artistDto.getId())) {
                        artistDto.setFollowedByUser(Boolean.TRUE);
                    }
                }).toList();
    }

    private List<Long> idsOfArtistsFollowedByUser(final Long userId, List<Long> allArtistResults) {
        List<UserArtist> alreadyFollowed = userArtistRepository.findByUserIdAndArtistIds(userId, allArtistResults);
        return alreadyFollowed.stream()
                .map(userArtist -> userArtist.getArtist().id)
                .toList();
    }

}

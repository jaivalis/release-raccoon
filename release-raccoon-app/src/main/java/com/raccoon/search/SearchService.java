package com.raccoon.search;

import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.mapping.ArtistSearchResponse;
import com.raccoon.search.ranking.ResultsRanker;

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
    final ResultsRanker ranker;
    final UserRepository userRepository;
    final UserArtistRepository userArtistRepository;

    @Inject
    public SearchService(final Instance<ArtistSearcher> searchers,
                         final ResultsRanker ranker,
                         final UserRepository userRepository,
                         final UserArtistRepository userArtistRepository) {
        this.searchers = searchers.stream().toList();
        log.info("Found {} artist searchers in classpath", this.searchers.size());
        this.ranker = ranker;
        this.userRepository = userRepository;
        this.userArtistRepository = userArtistRepository;
    }

    /**
     * Search for an artist against available Searchers
     * @param userEmail raccoonUser who searches, used to set followedByUser flag of ArtistDto
     * @param pattern pattern to match artist name against
     * @param size search limit per resource (database and lastfm)
     * @return ArtistSearchResponse
     */
    public ArtistSearchResponse searchArtists(final String userEmail,
                                              final String pattern,
                                              final Optional<Integer> size) {
        Map<ArtistSearcher, Collection<ArtistDto>> searchResultsPerSource = new HashMap<>();
        log.info("Searching for artist {}", pattern);

        searchers.parallelStream().forEach(
                searcher -> {
                    var results = searcher.searchArtist(pattern, size);
                    log.info("Search hits, source `{}`: {}", searcher.id(), results);
                    searchResultsPerSource.put(searcher, results);
                }
        );

        return ArtistSearchResponse.builder()
                .artists(postProcessSearchResults(userEmail, searchResultsPerSource))
                .build();
    }

    List<ArtistDto> postProcessSearchResults(String userEmail, Map<ArtistSearcher, Collection<ArtistDto>> perSource) {
        List<ArtistDto> rankedResultList = new ArrayList<>();

        // Mark followed artists
        var hibernateSearcher = perSource.keySet().stream()
                .filter(artistSearcher -> HIBERNATE_SEARCHER_ID.equals(artistSearcher.id()))
                .findFirst();
        if (hibernateSearcher.isPresent()) {
            Collection<ArtistDto> hibernateResults = perSource.get(hibernateSearcher.get());
            if (!hibernateResults.isEmpty()) {
                var followedFlagSet = setAlreadyFollowed(userEmail, hibernateResults);
                // Artists from the database are top ranked
                rankedResultList.addAll(followedFlagSet);
            }
        }

        // rank and merge the rest
        return ranker.rankSearchResults(perSource, rankedResultList);
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

        List<ArtistDto> list = new ArrayList<>();
        for (ArtistDto artistDto : hibernateHits) {
            if (idsOfArtistsFollowedByUser.contains(artistDto.getId())) {
                artistDto.setFollowedByUser(Boolean.TRUE);
            }
            list.add(artistDto);
        }
        return list;
    }

    private List<Long> idsOfArtistsFollowedByUser(final Long userId, List<Long> allArtistResults) {
        List<UserArtist> alreadyFollowed = userArtistRepository.findByUserIdAndArtistIds(userId, allArtistResults);
        return alreadyFollowed.stream()
                .map(userArtist -> userArtist.getArtist().id)
                .toList();
    }

}

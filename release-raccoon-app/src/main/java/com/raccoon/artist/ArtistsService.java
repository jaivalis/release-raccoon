package com.raccoon.artist;

import com.raccoon.dto.ArtistDto;
import com.raccoon.dto.PaginationParams;
import com.raccoon.dto.mapping.ArtistMapper;
import com.raccoon.entity.Artist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.user.dto.FollowedArtistsResponse;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.panache.common.Page;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ArtistsService {

    final ArtistRepository artistRepository;
    final UserRepository userRepository;
    final ArtistMapper artistMapper;

    @Inject
    public ArtistsService(ArtistRepository artistRepository,
                          UserRepository userRepository,
                          ArtistMapper artistMapper) {
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.artistMapper = artistMapper;
    }

    public List<Artist> getArtists(PaginationParams pageRequest) {
        return artistRepository.listArtistsPaginated(Page.of(pageRequest.getPage() * pageRequest.getSize(), pageRequest.getSize()));
    }

    @Transactional
    public FollowedArtistsResponse getOtherUsersFollowedArtists(PaginationParams pageRequest, String email) {
        var user = userRepository.findByEmail(email);

        List<Artist> followedByOthers = artistRepository.listDistinctArtistsNotFollowedByUser(
                Page.of(pageRequest.getPage() * pageRequest.getSize(), pageRequest.getSize()), user.getId()
        );

        List<ArtistDto> rows = followedByOthers
                .stream()
                .map(artistMapper::toArtistDto)
                .toList();

        log.info("Artists followed by other users: {}", rows.size());
        return FollowedArtistsResponse.builder()
                .rows(rows)
                .total(rows.size())
                .build();
    }

}

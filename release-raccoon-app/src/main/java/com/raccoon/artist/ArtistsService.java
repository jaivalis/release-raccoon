package com.raccoon.artist;

import com.raccoon.dto.ArtistDto;
import com.raccoon.dto.PaginationParams;
import com.raccoon.dto.mapping.ArtistMapper;
import com.raccoon.entity.Artist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.user.dto.FollowedArtistsResponse;

import java.util.List;

import io.quarkus.panache.common.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

        jakarta.data.page.Page<Artist> followedByOthers = artistRepository.distinctArtistsNotFollowedByUser(
                PageRequest.ofPage(pageRequest.getPage(), pageRequest.getSize(), true),
                user.getId()
        );

        List<ArtistDto> rows = followedByOthers
                .stream()
                .map(artistMapper::toArtistDto)
                .toList();

        log.info("Artists followed by other users: {}", rows.size());
        return FollowedArtistsResponse.builder()
                .rows(rows)
                .total(followedByOthers.totalElements())
                .build();
    }

}

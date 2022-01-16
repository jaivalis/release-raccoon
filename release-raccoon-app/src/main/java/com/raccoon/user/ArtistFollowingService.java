package com.raccoon.user;

import com.raccoon.entity.Artist;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;
import com.raccoon.search.dto.ArtistDto;

import java.time.LocalDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ArtistFollowingService {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final UserArtistRepository userArtistRepository;
    private final NotifyService notifyService;

    @Inject
    public ArtistFollowingService(final UserRepository userRepository,
                                  final ArtistRepository artistRepository,
                                  final UserArtistRepository userArtistRepository,
                                  final NotifyService notifyService) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.userArtistRepository = userArtistRepository;
        this.notifyService = notifyService;
    }

    public void unfollowArtist(final String userEmail, final Long artistId) {
        var user = userRepository.findByEmail(userEmail);
        userArtistRepository.deleteAssociation(user.id, artistId);
    }

    /**
     * Create a new UserArtist association
     * @param userEmail user requesting the follow
     * @param artistDto artistDto as it originates from an Artist search.
     */
    public void followArtist(final String userEmail, final ArtistDto artistDto) {
        var user = userRepository.findByEmail(userEmail);

        var artistDtoId = artistDto.validId();
        Artist artist = artistDtoId
                .map(id -> getArtistFromDb(artistDto, id))
                .orElseGet(() -> createNewArtist(artistDto));

        var userArtist = userArtistRepository
                .findByUserIdArtistIdOptional(user.id, artist.id)
                .orElseGet(UserArtist::new);
        userArtist.setArtist(artist);
        userArtist.setUser(user);
        userArtist.setWeight(1.0F);
        userArtistRepository.persist(userArtist);

        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        if (twoMinutesAgo.isAfter(artist.getCreateDate())) {
            // artist existed in the database prior, might have a release
            notifyService
                    .notifySingleUser(user, List.of(userArtist))
                    .await().indefinitely();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Artist createNewArtist(ArtistDto artistDto) {
        Artist artist;
        log.info("Following artist from web search {}", artistDto.getName());
        artist = artistRepository
                .findByNameOptional(artistDto.getName())
                .orElseGet(Artist::new);
        artist.setName(artistDto.getName());
        artist.setLastfmUri(artistDto.getLastfmUri());
        artist.setSpotifyUri(artistDto.getSpotifyUri());
        artist.setCreateDate(LocalDateTime.now());

        artistRepository.persist(artist);
        return artist;
    }

    private Artist getArtistFromDb(ArtistDto artistDto, Long artistDtoId) {
        Artist artist;
        log.info("Following artist with `id` provided {}", artistDto.getName());
        var artistOpt = artistRepository
                .findByIdAndNameOptional(artistDtoId, artistDto.getName());
        if (artistOpt.isPresent()) {
            artist = artistOpt.get();
        } else {
            // bad name/id pair provided, need to create new artist
            artist = new Artist();
            artist.setName(artistDto.getName());
            artist.setLastfmUri(artistDto.getLastfmUri());
            artist.setSpotifyUri(artistDto.getSpotifyUri());
            artist.setCreateDate(LocalDateTime.now());

            artistRepository.persist(artist);
        }
        return artist;
    }

}

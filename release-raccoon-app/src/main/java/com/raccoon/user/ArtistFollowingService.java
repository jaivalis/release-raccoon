package com.raccoon.user;

import com.raccoon.entity.Artist;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
        log.debug("Unfollow artist {} by user {}", artistId, user.getId());
        userArtistRepository.deleteAssociation(user.id, artistId);
        log.debug("Unfollowed");
    }

    /**
     * Create a new UserArtist association
     * @param userEmail raccoonUser requesting the follow
     * @param artist artist mapped from dto, might need to be persisted
     */
    public void followArtist(final String userEmail, final Artist artist) {
        var user = userRepository.findByEmail(userEmail);
        log.info("Follow artist {} by user {}", artist, user.getId());
        Artist persisted;
        Optional<Artist> byNameOptional = artistRepository.findByNameOptional(artist.getName());
        if (byNameOptional.isEmpty()) {
            log.info("Persisting new artist");
            artistRepository.persistAndFlush(artist);
            persisted = artist;
        } else {
            persisted = byNameOptional.get();
        }

        log.info("Follow artist {} by user {}", persisted, user.getId());

        var userArtist = userArtistRepository
                .findByUserIdArtistIdOptional(user.getId(), persisted.getId())
                .orElseGet(UserArtist::new);
        userArtist.setArtist(persisted);
        userArtist.setUser(user);
        userArtist.setWeight(1.0F);
        userArtistRepository.persist(userArtist);

        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        if (twoMinutesAgo.isAfter(persisted.getCreateDate())) {
            // artist existed in the database prior, might have a release
            notifyService
                    .notifySingleUser(user, List.of(userArtist))
                    .await()
                    .indefinitely();
        }
    }

}

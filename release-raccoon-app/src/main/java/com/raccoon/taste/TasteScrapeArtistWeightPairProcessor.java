package com.raccoon.taste;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.UserArtistRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TasteScrapeArtistWeightPairProcessor {

    ArtistRepository artistRepository;
    UserArtistFactory userArtistFactory;
    UserArtistRepository userArtistRepository;

    @Inject
    TasteScrapeArtistWeightPairProcessor(final ArtistRepository artistRepository,
                                         final UserArtistFactory userArtistFactory,
                                         final UserArtistRepository userArtistRepository) {
        this.artistRepository = artistRepository;
        this.userArtistFactory = userArtistFactory;
        this.userArtistRepository = userArtistRepository;
    }

    /**
     * Processes the new UserArtist pair that was returned by some taste scraper.
     *
     * Creates and persists the new RaccoonUser/Artist/UserArtist objects.
     * Adds the created UserArtist to the existingUserArtists if the Artist was in the database such that
     * an email might be sent in case that artist has an association in the Releases Table.
     * @param raccoonUser The raccoonUser scraped
     * @param artist The artist found as part of the raccoonUser's taste
     * @param weight The weight associated with the UserArtist
     * @param existingUserArtists Collection containing the UserArtist pairs for which the Artist
     *                            was present in the database before.
     * @return new UserArtist created
     */
    public UserArtist delegateProcessArtistWeightPair(final RaccoonUser raccoonUser,
                                                      final Artist artist,
                                                      final Float weight,
                                                      final Collection<UserArtist> existingUserArtists) {
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);

        var artistOpt = artistRepository.findByNameOptional(artist.getName());
        if (artistOpt.isEmpty()) {
            artistRepository.persist(artist);
        }

        var userArtistOpt = userArtistRepository.findByUserIdArtistIdOptional(raccoonUser.id, artist.id);
        var userArtist = userArtistOpt.orElseGet(() -> userArtistFactory.createUserArtist(raccoonUser, artist, weight));
        userArtist.setWeight(weight);
        userArtistRepository.persist(userArtist);

        if (artist.getCreateDate() == null || twoMinutesAgo.isAfter(artist.getCreateDate())) {
            // artist existed in the database prior, might have a release
            existingUserArtists.add(userArtist);
        }

        return userArtist;
    }
}

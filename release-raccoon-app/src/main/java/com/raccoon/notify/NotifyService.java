package com.raccoon.notify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.entity.repository.UserArtistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class NotifyService {

    MailingService sender;
    ReleaseRepository releaseRepository;
    UserArtistRepository userArtistRepository;

    public NotifyService(final ReleaseRepository releaseRepository,
                         final UserArtistRepository userArtistRepository,
                         final MailingService sender) {
        this.userArtistRepository = userArtistRepository;
        this.releaseRepository = releaseRepository;
        this.sender = sender;
    }

    public List<User> notifyUsers() {
        log.info("Notifying users");
        final List<User> usersNotified = new ArrayList<>();

        final List<UserArtist> userArtists = userArtistRepository.getUserArtistsWithNewRelease();
        userArtists.stream()
                .collect(Collectors.groupingBy(UserArtist::getUser))
                .forEach((user, userArtistList) -> {
                    if (notifyUser(user, getLatestReleases(userArtistList))) {
                        // mark processed
                        userArtistList.forEach(userArtist -> userArtist.setHasNewRelease(false));
                        usersNotified.add(user);
                    }
                });
        if (!usersNotified.isEmpty()) {
            userArtistRepository.persist(userArtists);
        }

        log.info("Notified {} users", usersNotified.size());
        return usersNotified;
    }

    /**
     * Returns a list of releases for which the user should be notified.
     * @param userArtistAssociations The <user, artist> pairs
     * @return a list of releases
     */
    private List<Release> getLatestReleases(Collection<UserArtist> userArtistAssociations) {
        Set<Artist> artists = userArtistAssociations.stream()
                .map(UserArtist::getArtist)
                .collect(Collectors.toSet());
        final List<Release> relevantReleases = releaseRepository.findByArtistsSinceDays(artists, 40);
        log.info("Found {} releases from {} to report on: {}", relevantReleases.size(), artists, relevantReleases);

        return relevantReleases;
    }

    /**
     * Call an external lib that will send emails.
     * @param user who needs to be notified.
     * @param releases what should be in the notification.
     */
    private boolean notifyUser(User user, List<Release> releases) {
        log.info("Notifying user {} for releases {}", user, releases);
        return this.sender.send(user.getEmail(), user, releases);
    }

}

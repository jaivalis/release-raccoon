package com.raccoon.notify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.persist;
import static java.util.Collections.EMPTY_LIST;

@Slf4j
@ApplicationScoped
public class NotifyService {

    MailSender sender;

    public NotifyService(final MailSender sender) {
        this.sender = sender;
    }

    public List<User> notifyUsers() {
        final List<UserArtist> userArtists = UserArtist.getUserArtistsWithNewRelease();

        userArtists.stream()
                .collect(Collectors.groupingBy(UserArtist::getUser))
                .forEach((user, userArtistList) -> {
                    if (notifyUser(user, getLatestReleases(userArtistList))) {
                        userArtistList.forEach(userArtist -> userArtist.setHasNewRelease(false));
                    }
                });
        // mark processed
//        userArtists
//                .forEach(userArtist -> userArtist.setHasNewRelease(false));
        persist(userArtists);

        return EMPTY_LIST;
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
        final List<Release> relevantReleases = Release.findByArtistsSinceDays(artists, 40);
        log.info("Found {} releases from {} to report on: {}", relevantReleases.size(), artists, relevantReleases);

        return relevantReleases;
    }

    /**
     * Call an external lib that will notify.
     * @param user who needs to be notified.
     * @param releases what should be in the notification.
     */
    private boolean notifyUser(User user, List<Release> releases) {
        log.info("No-op notifier for user {} for releases {}", user, releases);
        return this.sender.send(user.getEmail());
    }
}

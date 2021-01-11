package com.raccoon.notify;

import com.raccoon.entity.Release;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_LIST;

@Slf4j
@ApplicationScoped
public class NotifyService {

    public List<User> notifyUsers() {
        final List<UserArtist> userArtists = UserArtist.getUserArtistsWithNewRelease();

        userArtists.stream()
                .collect(Collectors.groupingBy(UserArtist::getUser))
                .forEach((user, userArtistList) -> notifyUser(user, getLatestReleases(userArtistList)));

        // mark UserArtists hasNewRelease as false

        return EMPTY_LIST;
    }

    /**
     * Returns a list of releases for which the user should be notified.
     * @param userArtistAssociations The <user, artist> pairs
     * @return a list of releases
     */
    private List<Release> getLatestReleases(Collection<UserArtist> userArtistAssociations) {
        // Need to join tables here I guess
//    def get_artist_latest_releases_since(self, artist_id: int, day_frequency: int) -> list:
//        """Joins ArtistRelease, Artist & Release tables to return all relevant info to be used to update users
//        :param artist_id: artist to look for
//        :param day_frequency: retrieve releases after `day_frequency` days ago.
//        :return: tuple of [ArtistRelease, Artist, Release]
//        """
//        current_time = datetime.utcnow()
//        x_days_ago = current_time - timedelta(days=day_frequency)
//
//        return self.session.query(ArtistRelease, Artist, Release).join(Artist)\
//            .filter(ArtistRelease.artist_id == artist_id)\
//            .filter(Release.id == ArtistRelease.release_id)\
//            .filter(Artist.id == ArtistRelease.artist_id)\
//            .filter(Release.date > x_days_ago)\
//            .all()
        return EMPTY_LIST;
    }

    /**
     * Call an external lib that will notify.
     * @param user who needs to be notified.
     * @param releases what should be in the notification.
     */
    private void notifyUser(User user, List<Release> releases) {
        log.info("No-op notifier for user {} for releases {}", user, releases);
    }
}

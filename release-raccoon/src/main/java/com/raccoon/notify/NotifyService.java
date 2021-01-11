package com.raccoon.notify;

import com.raccoon.entity.Release;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

@Slf4j
@ApplicationScoped
public class NotifyService {

    public List<User> notifyUsers() {
        // get_all_userartists_with_new_releases_grouped_by_artist
        List<UserArtist> userArtists = UserArtist.getUserArtistsWithNewReleaseGroupedByArtist();
        return EMPTY_LIST;
    }

    private void notifyUser(User user, List<Release> releases) {
        log.info("No-op notifier for user {} over releases {}", user, releases);
    }
}

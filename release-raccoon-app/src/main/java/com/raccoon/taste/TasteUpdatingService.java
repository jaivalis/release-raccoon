package com.raccoon.taste;

import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;

import java.util.Collection;

public interface TasteUpdatingService {

    /**
     * Update the taste of a given user
     * @param userId user to update
     * @return
     */
    User updateTaste(final Long userId);

    /**
     * Should be called after updateTaste.
     * That way the user will receive notifications in case a newly followed artist has made a
     * release in the near past.
     * @param user user to potentially notify
     * @param userArtists associations with Artists that were found in the db (therefore might be
     *                    relevant to get notified for)
     */
    void notifyForRecentReleases(final User user, Collection<UserArtist> userArtists);

}

package com.raccoon.taste;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;

import java.util.Collection;

public interface TasteUpdatingService {

    /**
     * Update the taste of a given raccoonUser
     * @param userId raccoonUser to update
     * @return
     */
    RaccoonUser updateTaste(final Long userId);

    /**
     * Should be called after updateTaste.
     * That way the raccoonUser will receive notifications in case a newly followed artist has made a
     * release in the near past.
     * @param raccoonUser raccoonUser to potentially notify
     * @param userArtists associations with Artists that were found in the db (therefore might be
     *                    relevant to get notified for)
     */
    void notifyForRecentReleases(final RaccoonUser raccoonUser, Collection<UserArtist> userArtists);

}

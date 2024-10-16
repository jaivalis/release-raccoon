package com.raccoon.notify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.Release;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.UserSettings;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserSettingsRepository;
import com.raccoon.mail.RaccoonMailer;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.quarkus.qute.TemplateException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Slf4j
@ApplicationScoped
public class NotifyService {

    private final RaccoonMailer raccoonMailer;
    private final ReleaseRepository releaseRepository;
    private final UserArtistRepository userArtistRepository;
    private final UserSettingsRepository userSettingsRepository;

    @Inject
    public NotifyService(final ReleaseRepository releaseRepository,
                         final UserArtistRepository userArtistRepository,
                         final UserSettingsRepository userSettingsRepository,
                         final RaccoonMailer raccoonMailer) {
        this.userArtistRepository = userArtistRepository;
        this.releaseRepository = releaseRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.raccoonMailer = raccoonMailer;
    }

    /**
     * Blocks until the Reactive mailer has responses for all sent mails.
     * @return
     */
    @Transactional
    public Uni<Boolean> notifyUsers() {
        log.info("Notifying users...");

        final List<UserArtist> userArtistsWithNewRelease = userArtistRepository.getUserArtistsWithNewRelease();
        List<Uni<Void>> unis = userArtistsWithNewRelease.stream()
                .collect(groupingBy(UserArtist::getUser))
                .entrySet()
                .stream()
                .map(entry -> {
                    var user = entry.getKey();
                    var userArtistList = entry.getValue();

                    if (shouldNotify(user)) {
                        return notifyUser(user, getLatestReleases(userArtistList), userArtistList);
                    } else {
                        log.info("Skipping over user {} because of user settings", user.id);
                        return Uni.createFrom().voidItem();
                    }
                }).toList();

        if (unis.isEmpty()) {
            log.info("Nobody to notify");
            return Uni.createFrom().item(true);
        }

        return Uni.combine().all()
                .unis(unis)
                .with(results -> true)
                .onFailure()
                .recoverWithUni(failure -> Uni.createFrom().item(false));
    }

    private boolean shouldNotify(RaccoonUser user) {
        Optional<UserSettings> settings = userSettingsRepository.findByUserId(user.id);
        if (settings.isPresent()) {
            LocalDate lastNotified = user.getLastNotified();
            return settings.get().shouldNotify(lastNotified);
        } else {
            return true;
        }
    }

    /**
     * Can be broken into `boolean canNotifyUser` & `Uni<Void> notifyUser`
     * @param raccoonUser the raccoonUser to notify
     * @param mightHaveNewReleases UserArtist associations that potentially have a release,
     *                             hasNewRelease will be marked `false` after the digest is sent.
     * @return
     */
    public Uni<Void> notifySingleUser(RaccoonUser raccoonUser, Collection<UserArtist> mightHaveNewReleases) {
        log.info("Checking for potential digests for {} newly subscribed to artists that were present in the database for raccoonUser {}",
                mightHaveNewReleases.size(), raccoonUser.id);
        Set<Artist> artists = mightHaveNewReleases.stream()
                .map(UserArtist::getArtist)
                .collect(toSet());
        var relevantReleases = releaseRepository.findByArtistsSinceDays(artists, 10);

        if (relevantReleases.isEmpty()) {
            log.info("Nothing found for raccoonUser {}", raccoonUser.id);
            return Uni.createFrom().voidItem();
        }

        return notifyUser(raccoonUser, relevantReleases, mightHaveNewReleases);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a list of releases for which the raccoonUser should be notified.
     * @param userArtistAssociations The <raccoonUser, artist> pairs
     * @return a list of releases
     */
    private List<Release> getLatestReleases(Collection<UserArtist> userArtistAssociations) {
        Set<Artist> artists = userArtistAssociations.stream()
                .map(UserArtist::getArtist)
                .collect(toSet());
        final List<Release> relevantReleases = releaseRepository.findByArtistsSinceDays(artists, 30);
        log.info("Found {} releases from {} to report on: {}", relevantReleases.size(), artists, relevantReleases);

        return relevantReleases;
    }

    /**
     * Generates the Digest email and sends it asynchronously.
     * @param raccoonUser who needs to be notified.
     * @param releases what should be in the notification.
     * @param userArtistList
     */
    private Uni<Void> notifyUser(final RaccoonUser raccoonUser,
                                 final List<Release> releases,
                                 final Collection<UserArtist> userArtistList) {
        try {
            return raccoonMailer.sendDigest(raccoonUser, releases,
                    () -> mailSuccessCallback(raccoonUser, userArtistList),
                    () -> mailFailureCallback(raccoonUser)
            );
        } catch (TemplateException e) {
            return Uni.createFrom().voidItem();
        }
    }

    /**
     * Mark userArtist.hasNewRelease as false
     * @param raccoonUser
     * @param userArtistList
     */
    void mailSuccessCallback(RaccoonUser raccoonUser, Collection<UserArtist> userArtistList) {
        log.info("Notified raccoonUser {}", raccoonUser.getId());
        raccoonUser.setLastNotified(LocalDate.now());
        userArtistList.forEach(userArtist -> userArtist.setHasNewRelease(false));

        userArtistRepository.persist(userArtistList);
    }

    void mailFailureCallback(RaccoonUser raccoonUser) {
        log.warn("Failed to deliver mail to raccoonUser {}", raccoonUser.id);
    }

}

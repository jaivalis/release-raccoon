package com.raccoon.notify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.Release;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.ReleaseRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.mail.RaccoonMailer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.qute.TemplateException;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Slf4j
@ApplicationScoped
public class NotifyService {

    RaccoonMailer raccoonMailer;
    ReleaseRepository releaseRepository;
    UserArtistRepository userArtistRepository;

    @Inject
    public NotifyService(final ReleaseRepository releaseRepository,
                         final UserArtistRepository userArtistRepository,
                         final RaccoonMailer raccoonMailer) {
        this.userArtistRepository = userArtistRepository;
        this.releaseRepository = releaseRepository;
        this.raccoonMailer = raccoonMailer;
    }

    @Scheduled(cron="{notify.cron.expr}")
    public void notifyCronJob() {
        log.info("Notifying cronjob triggered");
        notifyUsers();
    }

    /**
     * Blocks until the Reactive mailer has responses for all sent mails.
     * @return
     */
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
                    return notifyUser(user, getLatestReleases(userArtistList), userArtistList);
                })
                .toList();

        if (unis.isEmpty()) {
            log.info("Nobody to notify");
            return Uni.createFrom().item(true);
        }

        return Uni.combine().all()
                .unis(unis)
                .combinedWith(results -> true)
                .onFailure()
                .recoverWithUni(failure -> Uni.createFrom().item(false));
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a list of releases for which the user should be notified.
     * @param userArtistAssociations The <user, artist> pairs
     * @return a list of releases
     */
    private List<Release> getLatestReleases(Collection<UserArtist> userArtistAssociations) {
        Set<Artist> artists = userArtistAssociations.stream()
                .map(UserArtist::getArtist)
                .collect(toSet());
        final List<Release> relevantReleases = releaseRepository.findByArtistsSinceDays(artists, 40);
        log.info("Found {} releases from {} to report on: {}", relevantReleases.size(), artists, relevantReleases);

        return relevantReleases;
    }

    /**
     * Generates the Digest email and sends it asynchronously.
     * @param user who needs to be notified.
     * @param releases what should be in the notification.
     * @param userArtistList
     */
    private Uni<Void> notifyUser(final User user,
                                 final List<Release> releases,
                                 final List<UserArtist> userArtistList) {
        try {
            log.info("Notifying user {} for releases {}", user.id, releases);

            return raccoonMailer.sendDigest(user, releases,
                    () -> mailSuccessCallback(user, userArtistList),
                    () -> mailFailureCallback(user));
        } catch (TemplateException e) {
            return Uni.createFrom().voidItem();
        }
    }

    /**
     * Mark userArtist.hasNewRelease as false
     * @param user
     * @param userArtistList
     */
    private void mailSuccessCallback(User user,
                                     List<UserArtist> userArtistList) {
        log.info("Notified user {}", user.id);
        userArtistList.forEach(userArtist -> userArtist.setHasNewRelease(false));

        userArtistRepository.persist(userArtistList);
    }

    private void mailFailureCallback(User user) {
        log.warn("Failed to notify user {}", user.id);
    }

}

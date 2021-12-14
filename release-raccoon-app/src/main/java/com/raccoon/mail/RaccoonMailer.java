package com.raccoon.mail;

import com.raccoon.entity.Release;
import com.raccoon.entity.User;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.TemplateException;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper around ReactiveMailer. Sends email.
 */
@Slf4j
@ApplicationScoped
public class RaccoonMailer {

    ReactiveMailer mailer;
    MailTemplateRenderer renderer;

    @Inject
    RaccoonMailer(final ReactiveMailer mailer,
                  final MailTemplateRenderer renderer) {
        this.mailer = mailer;
        this.renderer = renderer;
    }

    public Uni<Void> sendDigest(final User user,
                                final List<Release> releases,
                                final Runnable successCallback,
                                final Runnable failureCallback) {
        log.info("Notifying user {} for releases {}", user.id, releases);
        try {
            Mail mail = renderer.renderDigestMail(user, releases);

            return mailer.send(mail).onItem()
                    .invoke(successCallback)
                    .onFailure().invoke(failureCallback);
        } catch (TemplateException e) {
            return Uni.createFrom().failure(e);
        }
    }

    public Uni<Void> sendWelcome(User user,
                                 final Runnable successCallback,
                                 final Runnable failureCallback) {
        log.info("Welcoming new user {}", user.id);
        try {
            Mail mail = renderer.renderWelcomeMail(user);

            return mailer.send(mail).onItem()
                    .invoke(successCallback)
                    .onFailure().invoke(failureCallback);
        } catch (TemplateException e) {
            return Uni.createFrom().failure(e);
        }
    }
}

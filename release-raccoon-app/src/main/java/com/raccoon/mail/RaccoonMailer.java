package com.raccoon.mail;

import com.raccoon.entity.Release;
import com.raccoon.entity.User;

import java.util.List;
import java.util.function.Consumer;

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

    /**
     * Sends emails and registers a failure callback
     * @param mail
     * @param onFailureCallback
     * @return
     */
    public Uni<Void> sendMail(Mail mail, Consumer<Mail> onFailureCallback) {
        return mailer.send(mail)
                .onItem().invoke(() -> log.debug("Successfully sent email"))
                .onFailure().invoke(() -> onFailureCallback.accept(mail));
    }

    /**
     * Fire and forget
     * @param withHtml
     */
    public Uni<Void> send(Mail withHtml) {
        return mailer.send(withHtml);
    }

    public Uni<Void> sendDigest(User user, List<Release> releases) {
        try {
            Mail mail = renderer.renderDigestMail(user.getEmail(), user, releases);
            log.info("Notifying user {} for releases {}", user, releases);

            return mailer.send(mail);
        } catch (TemplateException e) {
            return Uni.createFrom().voidItem();
        }
    }

    public Uni<Void> sendWelcome(User user) {
        try {
            Mail mail = renderer.renderWelcomeMail(user);
            log.info("Welcoming new user {}", user);

            return mailer.send(mail);
        } catch (TemplateException e) {
            return Uni.createFrom().voidItem();
        }
    }
}

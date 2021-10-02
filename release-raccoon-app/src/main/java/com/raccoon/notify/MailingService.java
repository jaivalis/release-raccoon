package com.raccoon.notify;

import com.raccoon.entity.Release;
import com.raccoon.entity.User;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class MailingService {

    static final String MAIL_SUBJECT_FORMAT_SINGULAR = "%d New Release for you";
    static final String MAIL_SUBJECT_FORMAT_PLURAL = "%d New Releases for you";

    Mailer mailer;

    @Inject
    Template digest;

    @Inject
    public MailingService(final Mailer mailer,
                          final Template digest) {
        this.mailer = mailer;
        this.digest = digest;
    }

    public boolean send(final String to, final User user, List<Release> releases) {
        try {
            final String subject = formatMailSubject(releases);
            final String htmlBody = digest.data("user", user, "releases", releases)
                    .render();
            mailer.send(Mail.withHtml(to, subject, htmlBody));
            return true;
        } catch (TemplateException e) {
            log.error("Error occurred when sending mail. Cause: {}", e.getCause(), e);
            return false;
        }
    }

    private String formatMailSubject(List<Release> releases) {
        String template = releases.size() == 1 ?
                MAIL_SUBJECT_FORMAT_SINGULAR : MAIL_SUBJECT_FORMAT_PLURAL;
        return String.format(template, releases.size());
    }

}

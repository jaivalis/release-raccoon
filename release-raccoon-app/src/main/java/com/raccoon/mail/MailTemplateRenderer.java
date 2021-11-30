package com.raccoon.mail;

import com.raccoon.entity.Release;
import com.raccoon.entity.User;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.mailer.Mail;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateException;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.templatedata.Constants.DIGEST_MAIL_SUBJECT_FORMAT_PLURAL;
import static com.raccoon.templatedata.Constants.DIGEST_MAIL_SUBJECT_FORMAT_SINGULAR;
import static com.raccoon.templatedata.Constants.WELCOME_EMAIL_SUBJECT;
import static com.raccoon.templatedata.QuteTemplateLoader.DIGEST_EMAIL_TEMPLATE_ID;
import static com.raccoon.templatedata.QuteTemplateLoader.WELCOME_EMAIL_TEMPLATE_ID;

/**
 * Renders emails to be sent by Raccoon Mailer
 */
@Slf4j
@ApplicationScoped
class MailTemplateRenderer {

    Template digestTemplate;
    Template welcomeTemplate;

    @Inject
    public MailTemplateRenderer(final Engine engine) {
        this.digestTemplate = engine.getTemplate(DIGEST_EMAIL_TEMPLATE_ID);
        this.welcomeTemplate = engine.getTemplate(WELCOME_EMAIL_TEMPLATE_ID);
    }

    Mail renderDigestMail(final String to, final User user, List<Release> releases) throws TemplateException {
        try {
            final String subject = getDigestSubject(releases);
            final String htmlBody = digestTemplate
                    .data(
                            "user", user,
                            "releases", releases
                    ).render();
            return Mail.withHtml(to, subject, htmlBody);
        } catch (TemplateException e) {
            log.error("Error occurred when rendering digest mail to {}. Cause: {}", to, e.getCause(), e);
            throw e;
        }
    }

    Mail renderWelcomeMail(final User user) throws TemplateException {
        var to = user.getEmail();
        try {
            final String htmlBody = welcomeTemplate
                    .data("user", user)
                    .render();
            return Mail.withHtml(to, WELCOME_EMAIL_SUBJECT, htmlBody);
        } catch (TemplateException e) {
            log.error("Error occurred when rendering welcome mail to {}. Cause: {}", to, e.getCause(), e);
            throw e;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String getDigestSubject(List<Release> releases) {
        String template = releases.size() == 1 ?
                DIGEST_MAIL_SUBJECT_FORMAT_SINGULAR : DIGEST_MAIL_SUBJECT_FORMAT_PLURAL;
        return String.format(template, releases.size());
    }

}

package com.raccoon.mail;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.Release;
import com.raccoon.templatedata.pojo.DigestMailContents;

import java.util.List;

import io.quarkus.mailer.Mail;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    Mail renderDigestMail(final RaccoonUser raccoonUser, List<Release> releases) throws TemplateException {
        var to = raccoonUser.getEmail();
        var subject = getDigestSubject(releases.size());
        try {
            final var contents = DigestMailContents.builder()
                    .mailTitle(getDigestSubject(releases.size()))
                    .raccoonUser(raccoonUser)
                    .releases(releases)
                    .build();

            final var htmlBody = digestTemplate
                    .data("contents", contents)
                    .render();

            return Mail.withHtml(to, subject, htmlBody);
        } catch (TemplateException e) {
            log.error("Error occurred when rendering digest mail to {}. Cause: {}", raccoonUser.id, e.getCause(), e);
            throw e;
        }
    }

    Mail renderWelcomeMail(final RaccoonUser raccoonUser) throws TemplateException {
        var to = raccoonUser.getEmail();
        try {
            final String htmlBody = welcomeTemplate.render();
            return Mail.withHtml(to, WELCOME_EMAIL_SUBJECT, htmlBody);
        } catch (TemplateException e) {
            log.error("Error occurred when rendering welcome mail to {}. Cause: {}", raccoonUser.id, e.getCause(), e);
            throw e;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String getDigestSubject(int releases) {
        String template = releases == 1 ?
                DIGEST_MAIL_SUBJECT_FORMAT_SINGULAR : DIGEST_MAIL_SUBJECT_FORMAT_PLURAL;
        return String.format(template, releases);
    }

}

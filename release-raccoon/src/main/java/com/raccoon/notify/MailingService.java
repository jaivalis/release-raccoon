package com.raccoon.notify;

import com.raccoon.entity.Release;
import com.raccoon.entity.User;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@Slf4j
@ApplicationScoped
public class MailingService {

    static final String MAIL_SUBJECT_FORMAT = "%d New Releases from your favorite artists!";

    @Inject
    Mailer mailer;

    @Inject
    Template digest;

    public MailingService() {}

    public boolean send(final String to, final User user, List<Release> releases) {
        final String subject = formatMailSubject(releases);
        final String htmlBody = digest.data("user", user, "releases", releases)
                .render();
        mailer.send(Mail.withHtml(to, subject, htmlBody));
        return true;
    }

    private String formatMailSubject(List<Release> releases) {
        return String.format(MAIL_SUBJECT_FORMAT, releases.size());
    }
}

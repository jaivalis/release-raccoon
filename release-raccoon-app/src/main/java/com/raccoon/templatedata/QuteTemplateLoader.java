package com.raccoon.templatedata;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Objects;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.qute.Engine;
import io.quarkus.runtime.StartupEvent;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Workaround for Template loading. I seem to be getting some errors when injecting the templates:
 *      [ERROR] [1] No template found for com.raccoon.xxx.ClassName#templatename
 * This class loads the templates in advance per a suggestion found here:
 *      https://groups.google.com/g/quarkus-dev/c/q5kaomkjMWA
 * As stated in the thread, with this solution the templates are not validated during the build.
 *
 * Look into @Location as replacement for this.
 */
public class QuteTemplateLoader {

    Engine engine;

    @Inject
    public QuteTemplateLoader(Engine engine) throws IOException {
        this.engine = engine;
    }

    final String indexContents = IOUtils.toString(Objects.requireNonNull(this.getClass().getResource("/templates/index.html")), UTF_8);
    final String profileContents = IOUtils.toString(Objects.requireNonNull(this.getClass().getResource("/templates/profile.html")), UTF_8);
    final String digestEmailContents = IOUtils.toString(Objects.requireNonNull(this.getClass().getResource("/templates/mail-digest.html")), UTF_8);
    final String welcomeEmailContents = IOUtils.toString(Objects.requireNonNull(this.getClass().getResource("/templates/mail-welcome.html")), UTF_8);

    public static final String DIGEST_EMAIL_TEMPLATE_ID = "digest";
    public static final String INDEX_TEMPLATE_ID = "index";
    public static final String PROFILE_TEMPLATE_ID = "profile";
    public static final String WELCOME_EMAIL_TEMPLATE_ID = "welcome";

    void onStart(@Observes StartupEvent event) {
        engine.putTemplate(DIGEST_EMAIL_TEMPLATE_ID, engine.parse(digestEmailContents));
        engine.putTemplate(INDEX_TEMPLATE_ID, engine.parse(indexContents));
        engine.putTemplate(PROFILE_TEMPLATE_ID, engine.parse(profileContents));
        engine.putTemplate(WELCOME_EMAIL_TEMPLATE_ID, engine.parse(welcomeEmailContents));
    }

}
package com.raccoon.templatedata;

import java.util.Scanner;

import io.quarkus.qute.Engine;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

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

    static final String EOF_DELIMITER = "\\A";

    final Engine engine;

    @Inject
    public QuteTemplateLoader(Engine engine) {
        this.engine = engine;
    }


    final String indexContents =
            new Scanner(requireNonNull(this.getClass().getResourceAsStream("/templates/index.html")), UTF_8)
                    .useDelimiter(EOF_DELIMITER).next();
    final String profileContents =
            new Scanner(requireNonNull(this.getClass().getResourceAsStream("/templates/profile.html")), UTF_8)
                    .useDelimiter(EOF_DELIMITER).next();
    final String userSettingsContents =
            new Scanner(requireNonNull(this.getClass().getResourceAsStream("/templates/profile-settings.html")), UTF_8)
                    .useDelimiter(EOF_DELIMITER).next();
    final String digestEmailContents =
            new Scanner(requireNonNull(this.getClass().getResourceAsStream("/templates/mail-digest.html")), UTF_8)
                    .useDelimiter(EOF_DELIMITER).next();
    final String welcomeEmailContents =
            new Scanner(requireNonNull(this.getClass().getResourceAsStream("/templates/mail-welcome.html")), UTF_8)
                    .useDelimiter(EOF_DELIMITER).next();

    public static final String DIGEST_EMAIL_TEMPLATE_ID = "digest";
    public static final String INDEX_TEMPLATE_ID = "index";
    public static final String PROFILE_TEMPLATE_ID = "profile";
    public static final String USER_SETTINGS_TEMPLATE_ID = "profile-settings";
    public static final String WELCOME_EMAIL_TEMPLATE_ID = "welcome";

    @Startup
    void onStart() {
        engine.putTemplate(DIGEST_EMAIL_TEMPLATE_ID, engine.parse(digestEmailContents));
        engine.putTemplate(INDEX_TEMPLATE_ID, engine.parse(indexContents));
        engine.putTemplate(PROFILE_TEMPLATE_ID, engine.parse(profileContents));
        engine.putTemplate(USER_SETTINGS_TEMPLATE_ID, engine.parse(userSettingsContents));
        engine.putTemplate(WELCOME_EMAIL_TEMPLATE_ID, engine.parse(welcomeEmailContents));
    }

}

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
 */
public class TemplateLoader {

    @Inject
    Engine engine;

    final String DIGEST_HTML_STR = IOUtils.toString(Objects.requireNonNull(this.getClass().getResource("/templates/digest.html")), UTF_8);
    final String INDEX_HTML_STR = IOUtils.toString(Objects.requireNonNull(this.getClass().getResource("/templates/index.html")), UTF_8);
    final String PROFILE_HTML_STR = IOUtils.toString(Objects.requireNonNull(this.getClass().getResource("/templates/profile.html")), UTF_8);

    public static final String DIGEST_TEMPLATE_ID = "digest";
    public static final String INDEX_TEMPLATE_ID = "index";
    public static final String PROFILE_TEMPLATE_ID = "profile";

    public TemplateLoader() throws IOException {
        // no-op
    }

    void onStart(@Observes StartupEvent event) {
        engine.putTemplate(DIGEST_TEMPLATE_ID, engine.parse(DIGEST_HTML_STR));
        engine.putTemplate(INDEX_TEMPLATE_ID, engine.parse(INDEX_HTML_STR));
        engine.putTemplate(PROFILE_TEMPLATE_ID, engine.parse(PROFILE_HTML_STR));
    }

}

package com.raccoon.mail;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.Release;
import com.raccoon.templatedata.pojo.DigestMailContents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.mailer.Mail;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateException;
import io.quarkus.qute.TemplateInstance;

import static com.raccoon.templatedata.Constants.DIGEST_MAIL_SUBJECT_FORMAT_PLURAL;
import static com.raccoon.templatedata.Constants.DIGEST_MAIL_SUBJECT_FORMAT_SINGULAR;
import static com.raccoon.templatedata.Constants.WELCOME_EMAIL_SUBJECT;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class MailTemplateRendererTest {

    MailTemplateRenderer renderer;

    @Mock
    Engine mockEngine;

    @Mock
    RaccoonUser mockRaccoonUser;
    @Mock
    Template mockTemplate;
    @Mock
    TemplateInstance mockTemplateInstance;

    @BeforeEach
    void setUp() {
        openMocks(this);

        when(mockEngine.getTemplate(any())).thenReturn(mockTemplate);

        renderer = new MailTemplateRenderer(mockEngine);
    }

    @Test
    void renderDigestMailSuccess() {
        var email = "email";
        when(mockTemplate.data(
                anyString(), any(DigestMailContents.class)
        )).thenReturn(mockTemplateInstance);
        when(mockRaccoonUser.getEmail()).thenReturn(email);

        Mail mail = renderer.renderDigestMail(mockRaccoonUser, emptyList());

        assertEquals(1, mail.getTo().size());
        assertEquals(email, mail.getTo().get(0));
    }

    @Test
    void renderDigestMailSuccessSingularSubject() {
        var email = "email";
        when(mockTemplate.data(
                anyString(), any(DigestMailContents.class)
        )).thenReturn(mockTemplateInstance);
        when(mockRaccoonUser.getEmail()).thenReturn(email);

        Mail mail = renderer.renderDigestMail(mockRaccoonUser, List.of(new Release()));

        assertEquals(String.format(DIGEST_MAIL_SUBJECT_FORMAT_SINGULAR, 1), mail.getSubject());
    }

    @Test
    void renderDigestMailSuccessPluralSubject() {
        var email = "email";
        when(mockTemplate.data(
                anyString(), any(DigestMailContents.class)
        )).thenReturn(mockTemplateInstance);
        when(mockRaccoonUser.getEmail()).thenReturn(email);

        Mail mail = renderer.renderDigestMail(mockRaccoonUser, List.of(new Release(), new Release()));

        assertEquals(String.format(DIGEST_MAIL_SUBJECT_FORMAT_PLURAL, 2), mail.getSubject());
    }

    @Test
    void renderDigestMailFails() {
        when(mockTemplate.data(
                anyString(), any(DigestMailContents.class)
        )).thenReturn(mockTemplateInstance);
        when(mockTemplateInstance.render()).thenThrow(TemplateException.class);
        ArrayList<Release> stub = new ArrayList<>();

        assertThrows(TemplateException.class, () -> renderer.renderDigestMail(mockRaccoonUser, stub));
    }

    @Test
    void renderWelcomeMailSuccess() {
        var email = "email";
        when(mockRaccoonUser.getEmail()).thenReturn(email);

        Mail mail = renderer.renderWelcomeMail(mockRaccoonUser);

        assertEquals(1, mail.getTo().size());
        assertEquals(email, mail.getTo().get(0));
        assertEquals(WELCOME_EMAIL_SUBJECT, mail.getSubject());
    }

    @Test
    void renderWelcomeMailFails() {
        when(mockTemplate.render()).thenThrow(TemplateException.class);

        assertThrows(TemplateException.class, () -> renderer.renderWelcomeMail(mockRaccoonUser));
    }

}
package com.raccoon.mail;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.Release;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.TemplateException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import lombok.extern.slf4j.Slf4j;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class RaccoonMailerTest {

    RaccoonMailer mailer;

    @Mock
    ReactiveMailer mockMailer;
    @Mock
    MailTemplateRenderer mockRenderer;
    @Mock
    Mail mockMail;
    @Mock
    Runnable mockRunnable;

    @BeforeEach
    void setUp() {
        mailer = new RaccoonMailer(mockMailer, mockRenderer);
    }

    @Test
    void testSendDigest() {
        var user = new RaccoonUser();
        user.setEmail("email");
        var releases = List.of(new Release());
        when(mockRenderer.renderDigestMail(user, releases)).thenReturn(mockMail);

        mailer.sendDigest(user, releases, mockRunnable, mockRunnable);

        verify(mockMailer, times(1)).send(mockMail);
    }

    @Test
    void testSendDigestTemplateException() {
        var user = new RaccoonUser();
        user.setEmail("email");
        var releases = List.of(new Release());
        when(mockRenderer.renderDigestMail(user, releases)).thenThrow(TemplateException.class);

        Uni<Void> uni = mailer.sendDigest(user, releases, mockRunnable, mockRunnable);

        uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertFailed();
        verify(mockMailer, times(0)).send(mockMail);
    }

    @Test
    void testSendWelcome() {
        var user = new RaccoonUser();
        user.setEmail("email");
        when(mockRenderer.renderWelcomeMail(user)).thenReturn(mockMail);

        mailer.sendWelcome(user, mockRunnable, mockRunnable);

        verify(mockMailer, times(1)).send(mockMail);
    }

    @Test
    void testSendWelcomeTemplateException() {
        var user = new RaccoonUser();
        user.setEmail("email");
        when(mockRenderer.renderWelcomeMail(user)).thenThrow(TemplateException.class);

        Uni<Void> uni = mailer.sendWelcome(user, mockRunnable, mockRunnable);

        uni.subscribe().withSubscriber(UniAssertSubscriber.create()).assertFailed();
        verify(mockMailer, times(0)).send(mockMail);
    }
}
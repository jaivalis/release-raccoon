package com.raccoon.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void setUp() {
        mailer = new RaccoonMailer(mockMailer, mockRenderer);
    }

    @Test
    void testQuarkusMailerInvoked() {
        Consumer<Mail> failureConsumer = mail -> fail("This is happy flow");
        mailer.sendMail(Mail.withHtml("someone", "subject", "<html></html>"), failureConsumer).await();

        verify(mockMailer, times(1)).send(any(Mail.class));
    }

    @Test
    void testErrorHandlingInvoked() {
        Consumer<Mail> failureConsumer = mail -> log.info("failed successfully");
        when(mockMailer.send(any())).thenReturn(Uni.createFrom().failure(IllegalArgumentException::new));
        Uni<Void> uni = mailer.sendMail(Mail.withHtml("someone", "subject", "<html></html>"), failureConsumer);

        var subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.assertFailed();
    }
}
package com.raccoon.notify;

import com.raccoon.entity.Release;
import com.raccoon.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateException;
import io.quarkus.qute.TemplateInstance;

import static com.raccoon.templatedata.TemplateLoader.DIGEST_TEMPLATE_ID;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailingServiceTest {

    MailingService service;

    @Mock
    Mailer mailerMock;
    @Mock
    Engine engineMock;
    @Mock
    Template templateMock;
    @Mock
    TemplateInstance templateInstanceMock;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(engineMock.getTemplate(DIGEST_TEMPLATE_ID)).thenReturn(templateMock);

        service = new MailingService(mailerMock, engineMock);
    }

    @Test
    void send() {
        when(templateMock.data(any(), any(), any(), any())).thenReturn(templateInstanceMock);

        assertTrue(service.send("someone@mail.com", new User(), List.of(new Release())));
    }

    @Test
    void sendException() {
        when(templateMock.data(any(), any(), any(), any())).thenReturn(templateInstanceMock);
        doThrow(TemplateException.class)
                .when(mailerMock).send(any(Mail.class));

        assertFalse(service.send("someone@mail.com", new User(), List.of(new Release(), new Release())));
    }
}
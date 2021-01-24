package com.raccoon.notify;

import com.raccoon.config.MailConfig;
import com.raccoon.scraper.config.SpotifyConfig;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class MailSender {

    final String username;
    final String password;

    Session session;

    public MailSender(MailConfig config) {
        username = config.getUsername();
        password = config.getPassword();
    }

    @PostConstruct
    private void init() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.mailtrap.io");
        prop.put("mail.smtp.port", "25");
        prop.put("mail.smtp.ssl.trust", "smtp.mailtrap.io");

        session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public boolean send(final String to) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("releaseraccoon@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Mail Subject");

            String msg = "Hey buddy you've got new releases.";

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);

            return true;
        } catch (MessagingException e) {
            log.error("Exception occurred when sending email to {}", to, e);
            return false;
        }

    }
}

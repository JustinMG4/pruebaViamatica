package com.justindev.prueba_telconet.application.utils;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailManager {

    private final MailSender mailSender;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendEmail(String to, String subject, String body ) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setSubject(subject);
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setText(body);
            helper.setFrom(sender);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new  RuntimeException("Error sending email", e);
        }
    }

}

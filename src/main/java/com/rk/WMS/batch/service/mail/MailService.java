package com.rk.WMS.batch.service.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j(topic = "MAIL-SERVICE")
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void send(String to, String subject, String content) {

        log.info("[MAIL][SEND] Sending mail | to={}, subject={}", to, subject);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);

        log.info("[MAIL][SUCCESS] Mail sent successfully | to={}", to);
    }
}



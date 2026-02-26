package com.rk.WMS.batch.service.mail;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Mục đích: Kiểm tra service gửi mail
 */
@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    /**
     * Test case: Gửi mail thành công
     */
    @Test
    @DisplayName("send - Gửi mail thành công")
    void send_Success() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "<h1>Test Content</h1>";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        assertDoesNotThrow(() -> mailService.send(to, subject, content));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    /**
     * Test case: Gửi mail với nội dung text thường
     */
    @Test
    @DisplayName("send - Gửi mail với nội dung text")
    void send_PlainText() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String content = "Plain text content";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        assertDoesNotThrow(() -> mailService.send(to, subject, content));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }


}

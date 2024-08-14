package com.memetrader.webserver;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
    private JavaMailSender sender;

    public MailService() {
        sender = new JavaMailSenderImpl();
        ((JavaMailSenderImpl)sender).setHost("accounts@memetrader.net");
    }

    public void send(MimeMessage msg) {
        sender.send(msg);
    }

    public MimeMessage createMimeMessage() {
        return sender.createMimeMessage();
    }

}

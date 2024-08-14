package com.memetrader.webserver;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Profile("MockMailService")
@Configuration
public class MockMailService {
    @Bean
    @Primary
    public MailService nameService() {
        var r = Mockito.mock(MailService.class);
        Mockito.when(r.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());

        return r;
    }
}

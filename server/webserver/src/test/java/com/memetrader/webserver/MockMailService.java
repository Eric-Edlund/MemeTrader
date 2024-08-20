package com.memetrader.webserver;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("MockMailService")
@Configuration
public class MockMailService {
    @Bean
    @Primary
    public KafkaAdapter nameService() {
        var r = Mockito.mock(KafkaAdapter.class);
        Mockito.when(r.enqueueVerifictionEmail(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        return r;
    }
}

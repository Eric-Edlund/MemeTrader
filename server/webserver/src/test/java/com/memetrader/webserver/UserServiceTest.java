package com.memetrader.webserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("MockMailService")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    KafkaAdapter mailService;

    @Autowired
    UserService userService;

    private static final String EMAIL = "joe@gmail.com";

    @Test
    public void testSendVerificationEmail() throws Error {
        userService.createAccount(EMAIL, "password");

        verify(mailService).enqueueVerifictionEmail(eq(EMAIL), Mockito.matches(
            "\\d\\d\\d\\d"
        ));
    }
}

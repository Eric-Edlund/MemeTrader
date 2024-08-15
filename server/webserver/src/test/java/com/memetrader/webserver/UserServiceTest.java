package com.memetrader.webserver;

import org.junit.Test;
import org.junit.runner.RunWith;
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
    MailService mailService;

    @Autowired
    UserService userService;

    private static final String EMAIL = "joe@gmail.com";
    private static final String CODE = "1234";

    @Test
    public void testSendVerificationEmail() throws Error {
        userService.createAccount(EMAIL, CODE);

        verify(mailService).send(any());
    }
}

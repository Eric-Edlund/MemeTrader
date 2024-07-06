package com.memetrader.webserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private record CreateUserResponse(boolean success, String msg) {};

    /**
     *
     * @param userName
     * @param email
     * @param password
     * @return True if successfully registered.
     */
    public boolean beginVerifyingUser(String userName, String email, String password, SseEmitter emitter) throws IOException {

// Send an initial event to the client to let them know we're verifying the email
        emitter.send(SseEmitter.event().data(new CreateUserResponse(false, "Verifying email...")));

        // Start the email verification process...
        // ...

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // When the email verification process completes, send another event to the client
        emitter.send(SseEmitter.event().data(new CreateUserResponse(true, "Email verified!")));

//        String hashedPassword = passwordEncoder.encode(password);
//        StockUser user = userRepository.saveVerified(userName, email, password);

        return true;
    }
}

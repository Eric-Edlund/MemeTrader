package com.memetrader.webserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class UserService {

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public String createHash(@NonNull String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Creates an unverified account in the verification queue.
     * 
     * @param email Any string
     * @param password Raw password
     * @return The account creation attempt id or a creation error.
     */
    public Result<String, AccountCreationError> createAccount(String email, String password) {
        var hash = passwordEncoder.encode(password);
        var code = Integer.toString((int) (Math.random() * 10000));

        var result = userRepository.saveUnverifiedAccount(email, hash, code);
        if (result.isOk()) {
            sendVerificationEmail(email, code);
        }

        return result;
    }

    /**
     * Tries to verify the account. If successful, then initializes the account.
     *
     * @param attemptId A number generated when the account was attempted to 
     * be created.
     * @returns true if the account is ready to use.
     */
    public boolean verifyAccount(String attemptId, String code) {
        var email = userRepository.verifyAccount(attemptId, code);
        if (email != null) {
            return false;
        }

        var user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return false;
        }

        var status = initializeAccount(user.get().getUserId());
        if (!status) {
            return false;
        }

        return true;
    }

    private boolean sendVerificationEmail(@NonNull String email, @NonNull String code) {
        MimeMessage msg = mailService.createMimeMessage();
        var helper = new MimeMessageHelper(msg);
        try {
            helper.setTo(email);
            helper.setText("Your verification code is " + code);
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }

        try {
            mailService.send(msg);
        } catch (MailException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * @param userId A valid user id.
     */
    private boolean initializeAccount(long userId) {
        return userRepository.addFundsToAccount(userId, 500);
    }

}

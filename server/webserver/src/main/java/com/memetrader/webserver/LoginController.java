package com.memetrader.webserver;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
public class LoginController {

    //TODO: Remove?
    @RequestMapping("/user")
    public Principal user(Principal user) {
        return user;
    }
}

package org.example;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class LoginController {

    @GetMapping("/authenticated")
    public boolean authenticated() {
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
        return SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }


//    @GetMapping("/login")
//    public String login(@RequestParam(value = "redirect", required = false) String redirect, Model model) {
//        model.addAttribute("redirect", redirect);
//        return "login";
//    }

//    @GetMapping("/login-success")
//    public String loginSuccess(HttpServletRequest request) {
//        String redirectUrl = request.getParameter("redirect");
//        if (redirectUrl != null) {
//            return "redirect:" + redirectUrl;
//        } else {
//            return "redirect:/"; // default redirect URL
//        }
//    }

//    @PostMapping("/login")
//    public String handleLogin(@RequestParam(value = "redirect", required = false) String redirect,
//                              @RequestParam("username") String username,
//                              @RequestParam("password") String password) {
//        // handle login logic here
//        if (false) {
//            if (redirect != null) {
//                return "redirect:" + redirect;
//            } else {
//                return "redirect:/"; // default redirect URL
//            }
//        } else {
//            return "login"; // return the login page view if login fails
//        }
//    }
}
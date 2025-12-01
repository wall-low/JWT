package com.web_site.JWT.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "Войти");
        return "login";
    }

    @GetMapping("/protected")
    public String protectedPage(Model model) {
        model.addAttribute("title", "Защищенная страница");
        return "protected_page";
    }

    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("title", "Профиль");
        return "profile";
    }

}
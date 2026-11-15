package com.example.szinhaz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/fooldal"})
    public String home() {
        return "fooldal";
    }

    @GetMapping("/kosar")
    public String cart() {
        return "kosar";
    }

    @GetMapping("/bejelentkezes")
    public String login() {
        return "bejelentkezes"; // A javított, ékezet nélküli fájlnév!
    }

    @GetMapping("/regisztracio")
    public String register() {
        return "regisztracio"; // A javított, ékezet nélküli fájlnév!
    }

    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin_felulet";
    }
}
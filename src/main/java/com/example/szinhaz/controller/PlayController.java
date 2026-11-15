package com.example.szinhaz.controller;

import com.example.szinhaz.model.Play;
import com.example.szinhaz.service.PlayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PlayController {

    private final PlayService playService;

    public PlayController(PlayService playService) {
        this.playService = playService;
    }

    // Felhasználói lista -> szindarabok_user.html
    @GetMapping("/darabok")
    public String listPlaysUser(Model model) {
        model.addAttribute("plays", playService.getAllPlays());
        return "szindarabok_user";
    }

    // Egy darab részletes oldala -> szindarab.html
    @GetMapping("/darabok/{id}")
    public String viewPlayDetails(@PathVariable Long id, Model model) {
        model.addAttribute("play", playService.getPlayById(id));
        return "szindarab";
    }

    // Admin lista -> szindarabok_admin.html
    @GetMapping("/admin/darabok")
    public String listPlaysAdmin(Model model) {
        model.addAttribute("plays", playService.getAllPlays());
        model.addAttribute("newPlay", new Play());
        return "szindarabok_admin";
    }

    // Új darab mentése adminból
    @PostMapping("/admin/darabok/mentes")
    public String savePlay(@ModelAttribute Play play) {
        playService.savePlay(play);
        return "redirect:/admin/darabok";
    }

    // Darab törlése adminból
    @PostMapping("/admin/darabok/torles/{id}")
    public String deletePlay(@PathVariable Long id) {
        playService.deletePlay(id);
        return "redirect:/admin/darabok";
    }
}
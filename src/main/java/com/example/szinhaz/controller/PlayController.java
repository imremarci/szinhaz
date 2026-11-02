package com.example.szinhaz.controller;

import com.example.szinhaz.service.PlayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlayController {

    private final PlayService playService;

    public PlayController(PlayService playService) {
        this.playService = playService;
    }

    @GetMapping("/darabok")
    public String listPlays(Model model) {
        model.addAttribute("plays", playService.getAllPlays());
        return "darabok"; // Későbbi darabok.html
    }
}
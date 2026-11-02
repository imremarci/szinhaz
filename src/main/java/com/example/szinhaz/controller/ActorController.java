package com.example.szinhaz.controller;

import com.example.szinhaz.service.ActorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActorController {

    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping("/szineszek")
    public String listActors(Model model) {
        model.addAttribute("actors", actorService.getAllActors());
        return "szineszek"; // A jövőbeli szineszek.html fájl neve lesz
    }
}
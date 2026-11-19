package com.example.szinhaz.controller;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.User;
import com.example.szinhaz.service.ActorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ActorController {

    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping("/szineszek")
    public String listActorsUser(Model model) {
        model.addAttribute("actors", actorService.getAllActors());
        return "szineszek_user";
    }

    @GetMapping("/admin/szineszek")
    public String listActorsAdmin(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/bejelentkezes";
        if (!"ADMIN".equals(currentUser.getRole())) return "redirect:/fooldal";

        model.addAttribute("actors", actorService.getAllActors());
        model.addAttribute("newActor", new Actor());
        return "szineszek_admin";
    }

    @PostMapping("/admin/szineszek/mentes")
    public String saveActor(@ModelAttribute Actor actor, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/fooldal";
        }

        actorService.saveActor(actor);
        return "redirect:/admin/szineszek";
    }
}
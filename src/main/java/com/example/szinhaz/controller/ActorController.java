package com.example.szinhaz.controller;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.User;
import com.example.szinhaz.service.ActorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
public class ActorController {

    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping("/szineszek")
    public String listActorsUser(Model model) {
        Collator huCollator = Collator.getInstance(Locale.forLanguageTag("hu-HU"));

        List<Actor> sortedActors = actorService.getAllActors().stream()
                .sorted(Comparator.comparing(Actor::getName, huCollator))
                .toList();

        model.addAttribute("actors", sortedActors);
        return "szineszek_user";
    }

    @GetMapping("/admin/szineszek")
    public String listActorsAdmin(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/bejelentkezes";
        if (!"ADMIN".equals(currentUser.getRole())) return "redirect:/fooldal";

        Collator huCollator = Collator.getInstance(Locale.forLanguageTag("hu-HU"));

        List<Actor> sortedActors = actorService.getAllActors().stream()
                .sorted(Comparator.comparing(Actor::getName, huCollator))
                .toList();

        model.addAttribute("actors", sortedActors);
        model.addAttribute("newActor", new Actor());
        return "szineszek_admin";
    }

    @PostMapping("/admin/szineszek/mentes")
    public String saveActor(@ModelAttribute Actor actor,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/fooldal";
        }

        try {
            actorService.saveActor(actor);
            redirectAttributes.addFlashAttribute("success", "Színész sikeresen mentve!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/szineszek";
    }
}
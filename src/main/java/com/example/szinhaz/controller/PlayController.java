package com.example.szinhaz.controller;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.*;
import com.example.szinhaz.service.PlayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class PlayController {

    private final PlayService playService;
    private final ShowRepository showRepository;
    private final TicketRepository ticketRepository;
    private final PlayRepository playRepository;
    private final ActorRepository actorRepository;

    public PlayController(PlayService playService, ShowRepository showRepository,
                          TicketRepository ticketRepository, PlayRepository playRepository,
                          ActorRepository actorRepository) {
        this.playService = playService;
        this.showRepository = showRepository;
        this.ticketRepository = ticketRepository;
        this.playRepository = playRepository;
        this.actorRepository = actorRepository;
    }

    @GetMapping("/darabok")
    public String listPlaysUser(Model model) {
        model.addAttribute("plays", playService.getAllPlays());
        return "szindarabok_user";
    }

    @GetMapping("/darabok/{id}")
    public String viewPlayDetails(@PathVariable Long id, Model model) {
        Play play = playService.getPlayById(id);
        if (play == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "A színdarab nem található.");
        }
        model.addAttribute("play", play);
        return "szindarab";
    }

    @GetMapping("/admin/darabok")
    public String listPlaysAdmin(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/bejelentkezes";
        if (!"ADMIN".equals(currentUser.getRole())) return "redirect:/fooldal";

        model.addAttribute("plays", playService.getAllPlays());
        model.addAttribute("newPlay", new Play());
        model.addAttribute("actors", actorRepository.findAll());
        return "szindarabok_admin";
    }

    @PostMapping("/admin/darabok/mentes")
    public String savePlay(@ModelAttribute Play play,
                           @RequestParam(required = false) List<Long> actorIds,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/fooldal";
        }

        if (play.getId() != null || (actorIds != null && !actorIds.isEmpty())) {
            // Szerkesztésnél akkor is felülírjuk a szereplőket, ha senkit sem jelöltek be.
            play.setActors(actorIds == null
                    ? new ArrayList<>()
                    : actorRepository.findAllById(actorIds));
        }

        try {
            playService.savePlay(play);
            redirectAttributes.addFlashAttribute("success", "Színdarab sikeresen mentve!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/darabok";
    }

    @PostMapping("/admin/darabok/torles/{id}")
    public String deletePlay(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/fooldal";
        }

        List<Show> relatedShows = showRepository.findAll().stream()
                .filter(s -> s.getPlay() != null && s.getPlay().getId().equals(id))
                .toList();

        for (Show show : relatedShows) {
            ticketRepository.deleteAll(ticketRepository.findByShowId(show.getId()));
        }
        showRepository.deleteAll(relatedShows);
        playRepository.deleteById(id);

        return "redirect:/admin/darabok";
    }
}
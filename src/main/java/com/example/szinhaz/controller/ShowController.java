package com.example.szinhaz.controller;

import com.example.szinhaz.model.Ticket;
import com.example.szinhaz.repository.TicketRepository;
import com.example.szinhaz.service.ShowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ShowController {

    private final ShowService showService;
    private final TicketRepository ticketRepository;

    public ShowController(ShowService showService, TicketRepository ticketRepository) {
        this.showService = showService;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/eloadasok")
    public String listShows(Model model) {
        model.addAttribute("shows", showService.getAllShows());
        return "eloadasok";
    }

    @GetMapping("/eloadas/{id}")
    public String bookShow(@PathVariable Long id, Model model) {
        model.addAttribute("show", showService.getShowById(id));

        List<Ticket> tickets = ticketRepository.findByShowId(id);
        List<String> bookedSeats = tickets.stream()
                .map(t -> t.getRowNumber() + "-" + t.getSeatNumber())
                .collect(Collectors.toList());

        model.addAttribute("bookedSeats", bookedSeats);
        return "eloadas";
    }
}
package com.example.szinhaz.controller;

import com.example.szinhaz.service.RoomService;
import com.example.szinhaz.service.ShowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class ShowController {

    private final ShowService showService;
    private final RoomService roomService; // Repository helyett Service!

    public ShowController(ShowService showService, RoomService roomService) {
        this.showService = showService;
        this.roomService = roomService;
    }

    @GetMapping("/napi-musor")
    public String getDailyShows(@RequestParam(value = "datum", required = false) LocalDate date, Model model) {

        // 1. Átadjuk a HTML-nek a Service-ből lekérdezett adatokat
        model.addAttribute("shows", showService.getDailyShows(date));
        model.addAttribute("selectedDate", date != null ? date : LocalDate.now());

        // 2. Visszaadjuk a HTML fájl nevét.
        // Mivel még nem dőlt el a végleges név, maradhat a jelenlegi "alap", amit később egy kattintással átírhattok.
        return "alap";
    }
}
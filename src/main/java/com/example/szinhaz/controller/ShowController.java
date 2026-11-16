package com.example.szinhaz.controller;

import com.example.szinhaz.service.ShowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    // Műsorfüzet -> eloadasok.html
    @GetMapping("/eloadasok")
    public String listShows(Model model) {
        model.addAttribute("shows", showService.getAllShows());
        return "eloadasok";
    }

    // Jegyvásárlás / Nézőtér egy adott előadáshoz -> eloadas.html
//    @GetMapping("/eloadas/{id}")
//    public String bookShow(@PathVariable Long id, Model model) {
//        model.addAttribute("show", showService.getShowById(id));
//        return "eloadas";
//    }

    @GetMapping("/eloadas/{id}")
    public String bookShow(@PathVariable Long id, Model model) {
        model.addAttribute("show", showService.getShowById(id));

        // Tesztként beállítunk pár széket foglaltra, amiket pirosra fest a Thymeleaf
        List<String> bookedSeats = List.of(
                "1-1", "1-2",         // Első sor bal széle
                "5-7", "5-8",         // Középen
                "10-14", "10-15",     // Nagyterem legutolsó sora
                "8-5", "8-6"          // Kamaraterem utolsó sora
        );
        model.addAttribute("bookedSeats", bookedSeats);

        return "eloadas";
    }
}
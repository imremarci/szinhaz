package com.example.szinhaz.controller;

import com.example.szinhaz.model.CartItem;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.service.PlayService;
import com.example.szinhaz.service.RoomService;
import com.example.szinhaz.service.ShowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@SessionAttributes("cart")
public class PageController {

    private final ShowService showService;
    private final PlayService playService;
    private final RoomService roomService;

    // Mindhárom Service-t injektáljuk
    public PageController(ShowService showService, PlayService playService, RoomService roomService) {
        this.showService = showService;
        this.playService = playService;
        this.roomService = roomService;
    }

    @ModelAttribute("cart")
    public List<CartItem> initializeCart() {
        return new ArrayList<>();
    }

    @GetMapping({"/", "/fooldal"})
    public String home(Model model) {
        model.addAttribute("plays", playService.getAllPlays());

        // Termek lekérése az adatbázisból
        List<Room> rooms = roomService.getAllRooms();

        // Sorbarendezés: A "Nagyterem" kerüljön a lista legkéjére!
        rooms.sort((r1, r2) -> {
            if (r1.getName().contains("Nagyterem")) return -1; // r1 előre
            if (r2.getName().contains("Nagyterem")) return 1;  // r2 előre
            return r1.getName().compareTo(r2.getName());       // A többi ABC sorrendben
        });

        model.addAttribute("rooms", rooms);
        model.addAttribute("shows", showService.getAllShows());

        return "fooldal";
    }

    @GetMapping("/kosar")
    public String cart(@ModelAttribute("cart") List<CartItem> cart, Model model) {
        int totalTickets = cart.stream().mapToInt(CartItem::getQuantity).sum();
        int ticketsPrice = cart.stream().mapToInt(CartItem::getPrice).sum();
        int convenienceFee = totalTickets > 0 ? 390 : 0;

        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("ticketsPrice", ticketsPrice);
        model.addAttribute("convenienceFee", convenienceFee);
        model.addAttribute("totalPrice", ticketsPrice + convenienceFee);

        return "kosar";
    }

    @PostMapping("/kosar/hozzaad")
    public String addToCart(@RequestParam Long showId,
                            @RequestParam String selectedSeats,
                            @ModelAttribute("cart") List<CartItem> cart) {
        String[] seats = selectedSeats.split(",");
        int quantity = seats.length;
        int price = quantity * 4500;

        StringBuilder seatInfo = new StringBuilder();
        for (String seat : seats) {
            String[] parts = seat.split("-");
            seatInfo.append(parts[0]).append(". sor, ").append(parts[1]).append(". szék; ");
        }

        cart.add(new CartItem(showService.getShowById(showId), seatInfo.toString(), quantity, price));
        return "redirect:/kosar";
    }

    @PostMapping("/kosar/torol/{index}")
    public String removeFromCart(@PathVariable int index, @ModelAttribute("cart") List<CartItem> cart) {
        if (index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
        return "redirect:/kosar";
    }

    @GetMapping("/bejelentkezes")
    public String login() { return "bejelentkezes"; }

    @GetMapping("/regisztracio")
    public String register() { return "regisztracio"; }

    @GetMapping("/admin")
    public String adminDashboard() { return "admin_felulet"; }
}
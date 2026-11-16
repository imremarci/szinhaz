package com.example.szinhaz.controller;

import com.example.szinhaz.model.CartItem;
import com.example.szinhaz.service.ShowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@SessionAttributes("cart") // Ezzel mondjuk meg a Springnek, hogy a "cart" változót tartsa meg a memóriában!
public class PageController {

    private final ShowService showService;

    public PageController(ShowService showService) {
        this.showService = showService;
    }

    // Ha még nincs kosár a sessionben, létrehozunk egy üreset
    @ModelAttribute("cart")
    public List<CartItem> initializeCart() {
        return new ArrayList<>();
    }

    @GetMapping({"/", "/fooldal"})
    public String home() {
        return "fooldal";
    }

    @GetMapping("/kosar")
    public String cart(@ModelAttribute("cart") List<CartItem> cart, Model model) {
        int totalTickets = cart.stream().mapToInt(CartItem::getQuantity).sum();
        int ticketsPrice = cart.stream().mapToInt(CartItem::getPrice).sum();
        int convenienceFee = totalTickets > 0 ? 390 : 0; // Fix kényelmi díj, ha van valami a kosárban

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

        // A selectedSeats egy string, pl: "1-4,1-5"
        String[] seats = selectedSeats.split(",");
        int quantity = seats.length;
        int price = quantity * 4500; // 4500 Ft / jegy

        // Visszafejtjük a szép szöveget (pl. "1. sor 4. szék, 1. sor 5. szék")
        StringBuilder seatInfo = new StringBuilder();
        for (String seat : seats) {
            String[] parts = seat.split("-");
            seatInfo.append(parts[0]).append(". sor, ").append(parts[1]).append(". szék; ");
        }

        // Hozzáadjuk a kosárhoz
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
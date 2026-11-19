package com.example.szinhaz.controller;

import com.example.szinhaz.model.CartItem;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Ticket;
import com.example.szinhaz.model.User;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.repository.TicketRepository;
import com.example.szinhaz.repository.UserRepository;
import com.example.szinhaz.repository.PlayRepository;
import com.example.szinhaz.repository.RoomRepository;
import com.example.szinhaz.repository.ShowRepository;
import com.example.szinhaz.service.PlayService;
import com.example.szinhaz.service.RoomService;
import com.example.szinhaz.service.ShowService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.szinhaz.model.Show;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Controller
@SessionAttributes("cart")
public class PageController {

    private final ShowService showService;
    private final PlayService playService;
    private final RoomService roomService;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final RoomRepository roomRepository;
    private final PlayRepository playRepository;

    public PageController(ShowService showService, PlayService playService, RoomService roomService,
                          TicketRepository ticketRepository, UserRepository userRepository, ShowRepository showRepository, RoomRepository roomRepository, PlayRepository playRepository) {
        this.showService = showService;
        this.playService = playService;
        this.roomService = roomService;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.roomRepository = roomRepository;
        this.playRepository = playRepository;
    }

    @ModelAttribute("cart")
    public List<CartItem> initializeCart() {
        return new ArrayList<>();
    }

    @GetMapping({"/", "/fooldal"})
    public String home(Model model) {
        model.addAttribute("plays", playService.getAllPlays());

        List<Room> rooms = roomService.getAllRooms();
        rooms.sort((r1, r2) -> {
            if (r1.getName().contains("Nagyterem")) return -1;
            if (r2.getName().contains("Nagyterem")) return 1;
            return r1.getName().compareTo(r2.getName());
        });
        model.addAttribute("rooms", rooms);

        // Csak a mai napra eső előadások szűrése
        LocalDate today = LocalDate.now();
        List<Show> todayShows = showService.getAllShows().stream()
                .filter(s -> s.getStartTime() != null && s.getStartTime().toLocalDate().equals(today))
                .toList();

        model.addAttribute("shows", todayShows);

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

        // Átadjuk a nyers selectedSeats stringet is a CartItem-nek
        cart.add(new CartItem(showService.getShowById(showId), seatInfo.toString(), selectedSeats, quantity, price));
        return "redirect:/kosar";
    }

    @PostMapping("/kosar/torol/{index}")
    public String removeFromCart(@PathVariable int index, @ModelAttribute("cart") List<CartItem> cart) {
        if (index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
        return "redirect:/kosar";
    }

    @PostMapping("/kosar/fizetes")
    public String checkout(@ModelAttribute("cart") List<CartItem> cart, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");

        // Ha nincs bejelentkezve, átirányítjuk a bejelentkezésre
        if (currentUser == null) {
            return "redirect:/bejelentkezes";
        }

        for (CartItem item : cart) {
            String[] seats = item.getRawSeats().split(",");
            for (String seat : seats) {
                String[] parts = seat.split("-");
                int row = Integer.parseInt(parts[0]);
                int seatNum = Integer.parseInt(parts[1]);

                ticketRepository.save(new Ticket(row, seatNum,currentUser, item.getShow()));
            }
        }

        cart.clear();
        return "redirect:/eloadasok";
    }

    @GetMapping("/bejelentkezes")
    public String login() {
        return "bejelentkezes";
    }

    @PostMapping("/bejelentkezes")
    public String processLogin(@RequestParam String name,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        var userOpt = userRepository.findByName(name);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("loggedInUser", userOpt.get());

            if ("ADMIN".equals(userOpt.get().getRole())) {
                return "redirect:/admin";
            }
            return "redirect:/fooldal";
        }

        model.addAttribute("error", "Hibás név vagy jelszó!");
        return "bejelentkezes";
    }

    @GetMapping("/regisztracio")
    public String register() {
        return "regisztracio";
    }

    @PostMapping("/regisztracio")
    public String processRegister(@RequestParam String name,
                                  @RequestParam String password,
                                  Model model) {
        if (userRepository.findByName(name).isPresent()) {
            model.addAttribute("error", "Ezzel a névvel már regisztráltak!");
            return "regisztracio";
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setPassword(password);

        userRepository.save(newUser);
        return "redirect:/bejelentkezes?sikeresReg";
    }

    @GetMapping("/kijelentkezes")
    public String logout(HttpSession session) {
        session.removeAttribute("loggedInUser");
        return "redirect:/fooldal";
    }
    @GetMapping("/profil")
    public String userProfile(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/bejelentkezes";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("tickets", ticketRepository.findByUserId(currentUser.getId()));
        return "profil";
    }

    // ADMIN FELÜLET MEGJELENÍTÉSE (adatbázisból)
    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/bejelentkezes";
        if (!"ADMIN".equals(currentUser.getRole())) return "redirect:/fooldal";

        List<Room> rooms = roomService.getAllRooms();
        rooms.sort((r1, r2) -> {
            if (r1.getName().contains("Nagyterem")) return -1;
            if (r2.getName().contains("Nagyterem")) return 1;
            return r1.getName().compareTo(r2.getName());
        });

        LocalDate today = LocalDate.now();
        List<Show> todayShows = showService.getAllShows().stream()
                .filter(s -> s.getStartTime() != null && s.getStartTime().toLocalDate().equals(today))
                .toList();

        model.addAttribute("rooms", rooms);
        model.addAttribute("plays", playService.getAllPlays());
        model.addAttribute("shows", todayShows);

        return "admin_felulet";
    }

    // ÚJ ELŐADÁS MENTÉSE AZ ADATBÁZISBA
    @PostMapping("/admin/eloadas/hozzaad")
    public String addShow(@RequestParam Long roomId,
                          @RequestParam Long playId,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                          HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/fooldal";
        }

        Room room = roomRepository.findById(roomId).orElse(null);
        Play play = playRepository.findById(playId).orElse(null);

        if (room != null && play != null) {
            showRepository.save(new Show(startTime, room, play));
        }

        return "redirect:/admin";
    }

    // ELŐADÁS TÖRLÉSE AZ ADATBÁZISBÓL
    @PostMapping("/admin/eloadas/torol/{id}")
    public String deleteShow(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/fooldal";
        }

        // Ha már vettek rá jegyet, előbb a jegyeket töröljük, hogy ne legyen adatbázis hiba
        ticketRepository.deleteAll(ticketRepository.findByShowId(id));
        showRepository.deleteById(id);

        return "redirect:/admin";
    }
}
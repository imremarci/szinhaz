package com.example.szinhaz.service;

import com.example.szinhaz.model.CartItem;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.Ticket;
import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> getTicketsByShow(Long showId) {
        return ticketRepository.findByShowId(showId);
    }

    public List<Ticket> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId);
    }

    public Ticket buyTicket(Show show, User user, int rowNumber, int seatNumber, int price) {
        if (show == null || user == null) {
            throw new IllegalArgumentException("Hiányzó előadás vagy felhasználó!");
        }
        if (show.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Már elkezdődött vagy lejárt előadásra nem váltható jegy!");
        }
        if (rowNumber <= 0 || seatNumber <= 0) {
            throw new IllegalArgumentException("Érvénytelen sor- vagy székszám!");
        }
        if (show.getRoom() == null) {
            throw new IllegalStateException("Az előadáshoz nem tartozik terem!");
        }
        if (rowNumber > show.getRoom().getNumberOfRows() || seatNumber > show.getRoom().getNumberOfSeats()) {
            throw new IllegalArgumentException(
                    "Nem létező szék! A terem mérete: " + show.getRoom().getNumberOfRows() + " sor, soronként " + show.getRoom().getNumberOfSeats() + " szék."
            );
        }

        boolean alreadyTaken = ticketRepository.findByShowId(show.getId()).stream()
                .anyMatch(t -> t.getRowNumber() == rowNumber && t.getSeatNumber() == seatNumber);

        if (alreadyTaken) {
            throw new IllegalStateException("Ez a szék (" + rowNumber + ". sor, " + seatNumber + ". szék) már foglalt!");
        }

        Ticket ticket = new Ticket(rowNumber, seatNumber, price, user, show);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public void checkoutCart(List<CartItem> cart, User user) {
        for (CartItem item : cart) {
            if (item.getRawSeats() == null || item.getRawSeats().isBlank()) {
                throw new IllegalArgumentException("Érvénytelen ülőhelyadat a kosárban!");
            }
            String[] seats = item.getRawSeats().split(",");
            for (String seat : seats) {
                String[] parts = seat.trim().split("-");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Hibás székformátum: " + seat);
                }
                int row = Integer.parseInt(parts[0]);
                int seatNum = Integer.parseInt(parts[1]);
                buyTicket(item.getShow(), user, row, seatNum, 4500);
            }
        }
    }
}
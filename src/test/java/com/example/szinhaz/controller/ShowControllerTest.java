package com.example.szinhaz.controller;

import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.Ticket;
import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.TicketRepository;
import com.example.szinhaz.service.ShowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShowControllerTest {

    private ShowService showService;
    private TicketRepository ticketRepository;
    private ShowController showController;
    private Model model;

    @BeforeEach
    void setUp() {
        showService = Mockito.mock(ShowService.class);
        ticketRepository = Mockito.mock(TicketRepository.class);
        showController = new ShowController(showService, ticketRepository);
        model = new ExtendedModelMap();
    }

    private Show show(Long id, LocalDateTime startTime) {
        Show show = new Show(startTime, new Room("Nagyterem", 10, 15),
                new Play("Hamlet", "Shakespeare", 120, 2024, "Leiras", "kep.png"));
        show.setId(id);
        return show;
    }

    @Test
    void eloadasokShouldExposeOnlyFutureShowsInChronologicalOrder() {
        Show withoutStart = show(1L, null);
        Show past = show(2L, LocalDateTime.now().minusDays(1));
        Show later = show(3L, LocalDateTime.now().plusDays(2));
        Show sooner = show(4L, LocalDateTime.now().plusDays(1));
        when(showService.getAllShows()).thenReturn(List.of(withoutStart, past, later, sooner));

        String view = showController.eloadasok(model);

        assertThat(view).isEqualTo("eloadasok");
        assertThat((List<Show>) model.getAttribute("shows"))
                .extracting(Show::getId)
                .containsExactly(4L, 3L);
    }

    @Test
    void eloadasokShouldExposeEmptyListWhenThereAreNoShows() {
        when(showService.getAllShows()).thenReturn(List.of());

        showController.eloadasok(model);

        assertThat((List<Show>) model.getAttribute("shows")).isEmpty();
    }

    @Test
    void bookShowShouldExposeShowAndBookedSeats() {
        Show show = show(5L, LocalDateTime.now().plusDays(1));
        User user = new User("anna", "jelszo", "Anna", "anna@example.com");
        List<Ticket> tickets = List.of(
                new Ticket(1, 2, 4500, user, show),
                new Ticket(3, 4, 4500, user, show)
        );
        when(showService.getShowById(5L)).thenReturn(show);
        when(ticketRepository.findByShowId(5L)).thenReturn(tickets);

        String view = showController.bookShow(5L, model);

        assertThat(view).isEqualTo("eloadas");
        assertThat(model.getAttribute("show")).isSameAs(show);
        assertThat((List<String>) model.getAttribute("bookedSeats")).containsExactly("1-2", "3-4");
    }

    @Test
    void bookShowShouldReturnNotFoundWhenShowIsMissing() {
        when(showService.getShowById(6L)).thenReturn(null);

        assertThatThrownBy(() -> showController.bookShow(6L, model))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Az előadás nem található.");
    }

    @Test
    void bookShowShouldExposeEmptyBookedSeatsWhenNoTicketsExist() {
        Show show = show(6L, LocalDateTime.now().plusDays(1));
        when(showService.getShowById(6L)).thenReturn(show);
        when(ticketRepository.findByShowId(6L)).thenReturn(List.of());

        showController.bookShow(6L, model);

        assertThat(model.getAttribute("show")).isSameAs(show);
        assertThat((List<String>) model.getAttribute("bookedSeats")).isEmpty();
    }
}

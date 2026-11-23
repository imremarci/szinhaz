package com.example.szinhaz.controller;

import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.Ticket;
import com.example.szinhaz.repository.TicketRepository;
import com.example.szinhaz.service.ShowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowControllerTest {

    @Mock
    private ShowService showService;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ShowController showController;

    @Test
    void eloadasok_FiltersPastAndNullAndSortsFutureShows() {
        Model model = new ExtendedModelMap();

        Show pastShow = new Show();
        pastShow.setStartTime(LocalDateTime.now().minusDays(1));

        Show nullTimeShow = new Show();
        nullTimeShow.setStartTime(null);

        Show laterShow = new Show();
        laterShow.setStartTime(LocalDateTime.now().plusDays(3));

        Show soonerShow = new Show();
        soonerShow.setStartTime(LocalDateTime.now().plusDays(1));

        when(showService.getAllShows()).thenReturn(List.of(pastShow, nullTimeShow, laterShow, soonerShow));

        String view = showController.eloadasok(model);

        assertEquals("eloadasok", view);
        @SuppressWarnings("unchecked")
        List<Show> result = (List<Show>) model.getAttribute("shows");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(soonerShow, result.get(0));
        assertEquals(laterShow, result.get(1));
    }

    @Test
    void bookShow_PopulatesShowAndFormattedBookedSeats() {
        Model model = new ExtendedModelMap();
        Show show = new Show();
        show.setId(5L);

        Ticket t1 = new Ticket(1, 2, 4500, null, show);
        Ticket t2 = new Ticket(3, 4, 4500, null, show);

        when(showService.getShowById(5L)).thenReturn(show);
        when(ticketRepository.findByShowId(5L)).thenReturn(List.of(t1, t2));

        String view = showController.bookShow(5L, model);

        assertEquals("eloadas", view);
        assertEquals(show, model.getAttribute("show"));
        @SuppressWarnings("unchecked")
        List<String> bookedSeats = (List<String>) model.getAttribute("bookedSeats");
        assertEquals(List.of("1-2", "3-4"), bookedSeats);
    }
}
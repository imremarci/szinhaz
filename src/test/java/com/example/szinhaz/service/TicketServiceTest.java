package com.example.szinhaz.service;

import com.example.szinhaz.model.CartItem;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.Ticket;
import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    private Show futureShow;
    private User user;
    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("Nagyterem", 5, 10);

        futureShow = new Show();
        futureShow.setId(1L);
        futureShow.setStartTime(LocalDateTime.now().plusDays(1));
        futureShow.setRoom(room);

        user = new User();
        user.setId(1L);
        user.setName("tesztuser");
    }

    @Test
    void getTicketsByShow_ReturnsList() {
        Ticket ticket = new Ticket(1, 2, 4500, user, futureShow);
        when(ticketRepository.findByShowId(1L)).thenReturn(List.of(ticket));

        List<Ticket> result = ticketService.getTicketsByShow(1L);

        assertEquals(1, result.size());
        verify(ticketRepository).findByShowId(1L);
    }

    @Test
    void getTicketsByUser_ReturnsList() {
        Ticket ticket = new Ticket(1, 2, 4500, user, futureShow);
        when(ticketRepository.findByUserId(1L)).thenReturn(List.of(ticket));

        List<Ticket> result = ticketService.getTicketsByUser(1L);

        assertEquals(1, result.size());
        verify(ticketRepository).findByUserId(1L);
    }

    @Test
    void buyTicket_ValidData_SavesAndReturnsTicket() {
        when(ticketRepository.findByShowId(1L)).thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        Ticket saved = ticketService.buyTicket(futureShow, user, 2, 5, 4500);

        assertNotNull(saved);
        assertEquals(2, saved.getRowNumber());
        assertEquals(5, saved.getSeatNumber());
        assertEquals(4500, saved.getPrice());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void buyTicket_NullShowOrUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ticketService.buyTicket(null, user, 1, 1, 4500));
        assertThrows(IllegalArgumentException.class, () -> ticketService.buyTicket(futureShow, null, 1, 1, 4500));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void buyTicket_PastShow_ThrowsException() {
        futureShow.setStartTime(LocalDateTime.now().minusHours(1));

        assertThrows(IllegalStateException.class, () -> ticketService.buyTicket(futureShow, user, 1, 1, 4500));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void buyTicket_InvalidRowOrSeat_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ticketService.buyTicket(futureShow, user, 0, 1, 4500));
        assertThrows(IllegalArgumentException.class, () -> ticketService.buyTicket(futureShow, user, 1, -2, 4500));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void buyTicket_NullRoom_ThrowsException() {
        futureShow.setRoom(null);

        assertThrows(IllegalStateException.class, () -> ticketService.buyTicket(futureShow, user, 1, 1, 4500));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void buyTicket_ExceedsRoomCapacity_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ticketService.buyTicket(futureShow, user, 6, 1, 4500));
        assertThrows(IllegalArgumentException.class, () -> ticketService.buyTicket(futureShow, user, 1, 11, 4500));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void buyTicket_SeatAlreadyTaken_ThrowsException() {
        Ticket existing = new Ticket(2, 5, 4500, user, futureShow);
        when(ticketRepository.findByShowId(1L)).thenReturn(List.of(existing));

        assertThrows(IllegalStateException.class, () -> ticketService.buyTicket(futureShow, user, 2, 5, 4500));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void checkoutCart_ValidMultipleSeats_SavesAllTickets() {
        CartItem item = new CartItem(futureShow, "1. sor, 2. szék; 1. sor, 3. szék;", "1-2, 1-3", 2, 9000);
        when(ticketRepository.findByShowId(1L)).thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        ticketService.checkoutCart(List.of(item), user);

        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }

    @Test
    void checkoutCart_BlankOrNullRawSeats_ThrowsException() {
        CartItem nullSeats = new CartItem(futureShow, "", null, 1, 4500);
        CartItem blankSeats = new CartItem(futureShow, "", "   ", 1, 4500);

        assertThrows(IllegalArgumentException.class, () -> ticketService.checkoutCart(List.of(nullSeats), user));
        assertThrows(IllegalArgumentException.class, () -> ticketService.checkoutCart(List.of(blankSeats), user));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void checkoutCart_InvalidSeatFormat_ThrowsException() {
        CartItem badFormat = new CartItem(futureShow, "", "1_2", 1, 4500);

        assertThrows(IllegalArgumentException.class, () -> ticketService.checkoutCart(List.of(badFormat), user));
        verify(ticketRepository, never()).save(any());
    }
}
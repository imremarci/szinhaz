package com.example.szinhaz.service;

import com.example.szinhaz.model.CartItem;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.Ticket;
import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketServiceTest {

    private TicketRepository ticketRepository;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = Mockito.mock(TicketRepository.class);
        ticketService = new TicketService(ticketRepository);
    }

    private User user() {
        User user = new User("anna", "jelszo", "Anna", "anna@example.com");
        user.setId(1L);
        return user;
    }

    private Room room() {
        Room room = new Room("Nagyterem", 10, 15);
        room.setId(1L);
        return room;
    }

    private Play play() {
        Play play = new Play("Hamlet", "Shakespeare", 120, 2024, "Leiras", "kep.png");
        play.setId(1L);
        return play;
    }

    private Show futureShow() {
        Show show = new Show(LocalDateTime.now().plusDays(1), room(), play());
        show.setId(5L);
        return show;
    }

    @Test
    void getTicketsByShowShouldDelegateToRepository() {
        List<Ticket> tickets = List.of(new Ticket());
        when(ticketRepository.findByShowId(5L)).thenReturn(tickets);

        assertThat(ticketService.getTicketsByShow(5L)).isSameAs(tickets);
    }

    @Test
    void getTicketsByUserShouldDelegateToRepository() {
        List<Ticket> tickets = List.of(new Ticket());
        when(ticketRepository.findByUserId(1L)).thenReturn(tickets);

        assertThat(ticketService.getTicketsByUser(1L)).isSameAs(tickets);
    }

    @Test
    void buyTicketShouldRejectNullShow() {
        assertThatThrownBy(() -> ticketService.buyTicket(null, user(), 1, 1, 4500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hiányzó előadás vagy felhasználó!");
    }

    @Test
    void buyTicketShouldRejectNullUser() {
        assertThatThrownBy(() -> ticketService.buyTicket(futureShow(), null, 1, 1, 4500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hiányzó előadás vagy felhasználó!");
    }

    @Test
    void buyTicketShouldRejectPastShow() {
        Show show = futureShow();
        show.setStartTime(LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> ticketService.buyTicket(show, user(), 1, 1, 4500))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Már elkezdődött vagy lejárt előadásra nem váltható jegy!");
    }

    @Test
    void buyTicketShouldRejectNonPositiveRow() {
        assertThatThrownBy(() -> ticketService.buyTicket(futureShow(), user(), 0, 1, 4500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvénytelen sor- vagy székszám!");
    }

    @Test
    void buyTicketShouldRejectNonPositiveSeat() {
        assertThatThrownBy(() -> ticketService.buyTicket(futureShow(), user(), 1, 0, 4500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvénytelen sor- vagy székszám!");
    }

    @Test
    void buyTicketShouldRejectShowWithoutRoom() {
        Show show = futureShow();
        show.setRoom(null);

        assertThatThrownBy(() -> ticketService.buyTicket(show, user(), 1, 1, 4500))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Az előadáshoz nem tartozik terem!");
    }

    @Test
    void buyTicketShouldRejectRowOutsideRoom() {
        assertThatThrownBy(() -> ticketService.buyTicket(futureShow(), user(), 11, 1, 4500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nem létező szék! A terem mérete: 10 sor, soronként 15 szék.");
    }

    @Test
    void buyTicketShouldRejectSeatOutsideRoom() {
        assertThatThrownBy(() -> ticketService.buyTicket(futureShow(), user(), 1, 16, 4500))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nem létező szék! A terem mérete: 10 sor, soronként 15 szék.");
    }

    @Test
    void buyTicketShouldRejectAlreadyTakenSeat() {
        Show show = futureShow();
        Ticket existing = new Ticket(1, 2, 4500, user(), show);
        when(ticketRepository.findByShowId(5L)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> ticketService.buyTicket(show, user(), 1, 2, 4500))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Ez a szék (1. sor, 2. szék) már foglalt!");
    }

    @Test
    void buyTicketShouldIgnoreTicketsInOtherRows() {
        Show show = futureShow();
        when(ticketRepository.findByShowId(5L))
                .thenReturn(List.of(new Ticket(2, 2, 4500, user(), show)));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket ticket = ticketService.buyTicket(show, user(), 1, 2, 4500);

        assertThat(ticket.getRowNumber()).isEqualTo(1);
        assertThat(ticket.getSeatNumber()).isEqualTo(2);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void buyTicketShouldSaveTicketWhenSeatIsFree() {        Show show = futureShow();
        when(ticketRepository.findByShowId(5L)).thenReturn(List.of(new Ticket(1, 3, 4500, user(), show)));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket ticket = ticketService.buyTicket(show, user(), 1, 2, 5000);

        assertThat(ticket.getRowNumber()).isEqualTo(1);
        assertThat(ticket.getSeatNumber()).isEqualTo(2);
        assertThat(ticket.getPrice()).isEqualTo(5000);
        assertThat(ticket.getUser()).isNotNull();
        assertThat(ticket.getShow()).isSameAs(show);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void buyTicketShouldAcceptBoundarySeat() {
        Show show = futureShow();
        when(ticketRepository.findByShowId(5L)).thenReturn(List.of());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket ticket = ticketService.buyTicket(show, user(), 10, 15, 4500);

        assertThat(ticket.getRowNumber()).isEqualTo(10);
        assertThat(ticket.getSeatNumber()).isEqualTo(15);
    }

    @Test
    void checkoutCartShouldDoNothingForEmptyCart() {
        ticketService.checkoutCart(List.of(), user());

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void checkoutCartShouldRejectNullSeats() {
        CartItem item = new CartItem(futureShow(), "info", null, 1, 4500);

        assertThatThrownBy(() -> ticketService.checkoutCart(List.of(item), user()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvénytelen ülőhelyadat a kosárban!");
    }

    @Test
    void checkoutCartShouldRejectBlankSeats() {
        CartItem item = new CartItem(futureShow(), "info", "   ", 1, 4500);

        assertThatThrownBy(() -> ticketService.checkoutCart(List.of(item), user()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvénytelen ülőhelyadat a kosárban!");
    }

    @Test
    void checkoutCartShouldRejectSeatWithoutSeparator() {
        CartItem item = new CartItem(futureShow(), "info", "12", 1, 4500);

        assertThatThrownBy(() -> ticketService.checkoutCart(List.of(item), user()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hibás székformátum: 12");
    }

    @Test
    void checkoutCartShouldRejectSeatWithTooManyParts() {
        CartItem item = new CartItem(futureShow(), "info", "1-2-3", 1, 4500);

        assertThatThrownBy(() -> ticketService.checkoutCart(List.of(item), user()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hibás székformátum: 1-2-3");
    }

    @Test
    void checkoutCartShouldBuyEverySeat() {
        Show show = futureShow();
        when(ticketRepository.findByShowId(5L)).thenReturn(List.of());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CartItem item = new CartItem(show, "info", " 1-2 , 1-3 ", 2, 9000);

        ticketService.checkoutCart(List.of(item), user());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Ticket::getRowNumber, Ticket::getSeatNumber, Ticket::getPrice)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, 2, 4500),
                        org.assertj.core.groups.Tuple.tuple(1, 3, 4500)
                );
    }
}

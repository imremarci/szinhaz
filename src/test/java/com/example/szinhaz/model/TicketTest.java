package com.example.szinhaz.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TicketTest {

    @Test
    void defaultConstructorShouldCreateEmptyTicket() {
        Ticket ticket = new Ticket();

        assertThat(ticket.getId()).isNull();
        assertThat(ticket.getRowNumber()).isZero();
        assertThat(ticket.getSeatNumber()).isZero();
        assertThat(ticket.getPrice()).isZero();
        assertThat(ticket.getUser()).isNull();
        assertThat(ticket.getShow()).isNull();
    }

    @Test
    void parameterizedConstructorShouldSetFields() {
        User user = new User("anna", "jelszo", "Anna", "anna@example.com");
        Show show = new Show(LocalDateTime.of(2030, 1, 1, 19, 0), null, null);

        Ticket ticket = new Ticket(2, 5, 4500, user, show);

        assertThat(ticket.getRowNumber()).isEqualTo(2);
        assertThat(ticket.getSeatNumber()).isEqualTo(5);
        assertThat(ticket.getPrice()).isEqualTo(4500);
        assertThat(ticket.getUser()).isSameAs(user);
        assertThat(ticket.getShow()).isSameAs(show);
    }

    @Test
    void settersAndGettersShouldRoundTripEveryField() {
        Ticket ticket = new Ticket();
        User user = new User("bela", "jelszo", "Bela", "bela@example.com");
        Show show = new Show();

        ticket.setId(9L);
        ticket.setRowNumber(4);
        ticket.setSeatNumber(7);
        ticket.setPrice(5000);
        ticket.setUser(user);
        ticket.setShow(show);

        assertThat(ticket.getId()).isEqualTo(9L);
        assertThat(ticket.getRowNumber()).isEqualTo(4);
        assertThat(ticket.getSeatNumber()).isEqualTo(7);
        assertThat(ticket.getPrice()).isEqualTo(5000);
        assertThat(ticket.getUser()).isSameAs(user);
        assertThat(ticket.getShow()).isSameAs(show);
    }
}

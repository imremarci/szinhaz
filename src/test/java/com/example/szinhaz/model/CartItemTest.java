package com.example.szinhaz.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemTest {

    @Test
    void constructorShouldExposeEveryField() {
        Room room = new Room("Nagyterem", 10, 15);
        Play play = new Play("Hamlet", "Shakespeare", 160, 2024, "Leiras", "kep.png");
        Show show = new Show(LocalDateTime.of(2030, 1, 1, 19, 0), room, play);

        CartItem item = new CartItem(show, "1. sor, 2. szek; ", "1-2", 1, 4500);

        assertThat(item.getShow()).isSameAs(show);
        assertThat(item.getSeatInfo()).isEqualTo("1. sor, 2. szek; ");
        assertThat(item.getRawSeats()).isEqualTo("1-2");
        assertThat(item.getQuantity()).isEqualTo(1);
        assertThat(item.getPrice()).isEqualTo(4500);
    }

    @Test
    void multipleSeatsShouldKeepQuantityAndPrice() {
        CartItem item = new CartItem(null, "info", "1-2,1-3", 2, 9000);

        assertThat(item.getShow()).isNull();
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getPrice()).isEqualTo(9000);
    }
}

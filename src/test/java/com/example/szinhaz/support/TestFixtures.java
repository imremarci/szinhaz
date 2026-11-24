package com.example.szinhaz.support;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.Ticket;
import com.example.szinhaz.model.User;

import java.time.LocalDateTime;

/**
 * Kozos teszt-fixture-ok a tesztekhez. Nem tartalmaz uzleti logikat,
 * csak egyszeru, azonosithato entitasokat gyart.
 */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static Room room(Long id, String name, int rows, int seats) {
        Room room = new Room(name, rows, seats);
        room.setId(id);
        return room;
    }

    public static Play play(Long id, String title, int durationMinutes) {
        Play play = new Play(title, "Rendezo", durationMinutes, 2024, "Leiras", "kep.png");
        play.setId(id);
        return play;
    }

    public static Show show(Long id, LocalDateTime startTime, Room room, Play play) {
        Show show = new Show(startTime, room, play);
        show.setId(id);
        return show;
    }

    public static User user(Long id, String name, String role) {
        User user = new User(name, "titkos123", "Teljes Nev", name + "@example.com");
        user.setId(id);
        user.setRole(role);
        return user;
    }

    public static Actor actor(Long id, String name, double rating) {
        Actor actor = new Actor(name, (int) rating, "kep.png");
        actor.setId(id);
        actor.setRating(rating);
        return actor;
    }

    public static Ticket ticket(Long id, int row, int seat, User user, Show show) {
        Ticket ticket = new Ticket(row, seat, 4500, user, show);
        ticket.setId(id);
        return ticket;
    }
}

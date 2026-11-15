package com.example.szinhaz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shows")
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDateTime startTime;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "play_id")
    private Play play;

    public Show() {
    }

    public Show(LocalDateTime startTime, Room room, Play play) {
        this.startTime = startTime;
        this.room = room;
        this.play = play;
    }

    // Kötelező Getterek és Setterek a Spring és a Thymeleaf számára:
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Play getPlay() {
        return play;
    }

    public void setPlay(Play play) {
        this.play = play;
    }
}
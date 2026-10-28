package com.example.szinhaz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
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

    @OneToMany(mappedBy = "show")
    private Set<Performance> performances;

    public Show() {
    }

    public Show(LocalDateTime startTime, Room room, Play play) {
        this.startTime = startTime;
        this.room = room;
        this.play = play;
    }


}
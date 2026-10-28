package com.example.szinhaz.model;

import jakarta.persistence.*;

@Entity
public class Play {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String director;
    private int durationInMinutes;

    public Play() {
    }

    public Play(String title, String director, int durationInMinutes) {
        this.title = title;
        this.director = director;
        this.durationInMinutes = durationInMinutes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    @Override
    public String toString() {
        return title + " (Director: " + director + ")";
    }
}
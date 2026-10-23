package com.example.szinhaz.model;

public class Play {
    private String title;
    private String director;
    private int durationInMinutes;

    public Play(String title, String director, int durationInMinutes) {
        this.title = title;
        this.director = director;
        this.durationInMinutes = durationInMinutes;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public int getDurationInMinutes() { return durationInMinutes; }
    public void setDurationInMinutes(int durationInMinutes) { this.durationInMinutes = durationInMinutes; }

    @Override
    public String toString() {
        return title + " (Director: " + director + ")";
    }
}
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

    private int releaseYear;

    @Column(length = 2000)
    private String description;
    private String coverImageUrl;

    public Play() {
    }

    public Play(String title, String director, int durationInMinutes, int releaseYear, String description, String coverImageUrl) {
        this.title = title;
        this.director = director;
        this.durationInMinutes = durationInMinutes;
        this.releaseYear = releaseYear;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
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

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    @Override
    public String toString() {
        return title + " (Director: " + director + ")";
    }
}
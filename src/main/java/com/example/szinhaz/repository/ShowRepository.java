package com.example.szinhaz.repository;

import com.example.szinhaz.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    // A Spring ebből a névből automatikusan legenerálja a két időpont közötti SQL lekérdezést
    List<Show> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
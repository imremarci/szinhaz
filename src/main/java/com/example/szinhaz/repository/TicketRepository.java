package com.example.szinhaz.repository;

import com.example.szinhaz.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByShowId(Long showId);
}
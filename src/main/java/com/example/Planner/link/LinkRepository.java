package com.example.Planner.link;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface LinkRepository extends JpaRepository<Link, UUID> {
    List<Link> findByTripId(UUID id);
}

package com.example.Planner.link;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Planner.trip.Trip;

@Service
public class LinkService {

    @Autowired
    private LinkRepository linkRepository;

    public LinkResponseDTO registerLink(LinkRequestDTO data, Trip trip) {
        Link newLink = new Link(data.title(), data.url(), trip);
        
        this.linkRepository.save(newLink);
        return new LinkResponseDTO(newLink.getId());
    }

    public List<LinkData> getAllLinksFromTrip(UUID tripId) {
        return this.linkRepository.findByTripId(tripId).stream().map(link -> new LinkData(link.getId(), link.getTitle(), link.getUrl())).toList();
    }

}

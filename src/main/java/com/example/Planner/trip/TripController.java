package com.example.Planner.trip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Planner.activity.ActivityData;
import com.example.Planner.activity.ActivityRequestDTO;
import com.example.Planner.activity.ActivityResponseDTO;
import com.example.Planner.activity.ActivityService;
import com.example.Planner.link.LinkData;
import com.example.Planner.link.LinkRequestDTO;
import com.example.Planner.link.LinkResponseDTO;
import com.example.Planner.link.LinkService;
import com.example.Planner.participant.ParticipantData;
import com.example.Planner.participant.ParticipantRequestDTO;
import com.example.Planner.participant.ParticipantResponseDTO;
import com.example.Planner.participant.ParticipantService;

@RestController
@RequestMapping("trips")
public class TripController {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LinkService linkService;

    @PostMapping
    public ResponseEntity<TripResponseDTO> createTrip(@RequestBody TripRequestDTO data) {
        Trip newTrip = new Trip(data);

        tripRepository.save(newTrip);
        this.participantService.registerParticipantsToTrip(data.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripResponseDTO(newTrip.getId()));
    }

    @GetMapping("{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestDTO data) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setEndsAt(LocalDateTime.parse(data.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(data.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(data.destination());

            this.tripRepository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.tripRepository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("{id}/invite")
    public ResponseEntity<ParticipantResponseDTO> invateParticipant(@PathVariable UUID id,
            @RequestBody ParticipantRequestDTO data) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ParticipantResponseDTO participantResponse = this.participantService.registerParticipantToTrip(data.email(),
                    rawTrip);

            if (rawTrip.getIsConfirmed())
                this.participantService.triggerConfirmationEmailToParticipant(data.email());

            return ResponseEntity.ok(participantResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id) {
        List<ParticipantData> participantsList = this.participantService.getAllParticipantsTrip(id);

        return ResponseEntity.ok(participantsList);
    }

    @PostMapping("{id}/activities")
    public ResponseEntity<ActivityResponseDTO> registerActivity(@PathVariable UUID id,
            @RequestBody ActivityRequestDTO data) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ActivityResponseDTO activityResponseDTO = this.activityService.registerActivity(data, rawTrip);

            return ResponseEntity.ok(activityResponseDTO);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id) {
        List<ActivityData> activityList = this.activityService.getAllActivitiesFromId(id);

        return ResponseEntity.ok(activityList);
    }

    @PostMapping("{id}/links")
    public ResponseEntity<LinkResponseDTO> registerLink(@PathVariable UUID id, @RequestBody LinkRequestDTO data) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            LinkResponseDTO linkResponse = this.linkService.registerLink(data, rawTrip);

            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("{id}/links")
    public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id) {
        List<LinkData> linkList = this.linkService.getAllLinksFromTrip(id);

        return ResponseEntity.ok(linkList);
    }

}

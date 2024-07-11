package com.example.Planner.activity;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Planner.trip.Trip;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    public ActivityResponseDTO registerActivity(ActivityRequestDTO data, Trip trip) {
        Activity newActivity = new Activity(data.title(), data.occurs_at(), trip);
        
        this.activityRepository.save(newActivity);
        return new ActivityResponseDTO(newActivity.getId());
    }

    public List<ActivityData> getAllActivitiesFromId(UUID tripId) {
        return this.activityRepository.findByTripId(tripId).stream().map(activity -> new ActivityData(activity.getId(), activity.getTitle(), activity.getOccursAt())).toList();
    }

}

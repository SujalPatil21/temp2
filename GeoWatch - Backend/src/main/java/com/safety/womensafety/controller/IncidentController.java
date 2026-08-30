package com.safety.womensafety.controller;

import com.safety.womensafety.dto.CreateIncidentRequest;
import com.safety.womensafety.dto.IncidentResponse;
import com.safety.womensafety.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    // Create incident and return incidentId
    @PostMapping
    public ResponseEntity<Long> submitIncident(@Valid @RequestBody CreateIncidentRequest request) {

        Long incidentId = incidentService.submitIncident(request);

        return ResponseEntity.ok(incidentId);
    }

    // Resolve incident
    @PostMapping("/{id}/resolve")
    public ResponseEntity<String> resolveIncident(@PathVariable Long id) {

        String response = incidentService.resolveIncident(id);

        return ResponseEntity.ok(response);
    }

    // Get incidents by event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(incidentService.getIncidentsByEvent(eventId));
    }
}
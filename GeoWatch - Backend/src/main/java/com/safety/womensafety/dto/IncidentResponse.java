package com.safety.womensafety.dto;

import com.safety.womensafety.model.Incident;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private Long id;
    private String description;
    private LocalDateTime timestamp;
    private Double latitude;
    private Double longitude;
    private boolean resolved;
    private String semanticRisk;
    private String incidentType;

    public IncidentResponse(Incident incident) {
        this.id = incident.getId();
        this.description = incident.getDescription();
        this.timestamp = incident.getTimestamp();
        this.latitude = incident.getLatitude();
        this.longitude = incident.getLongitude();
        this.resolved = incident.isResolved();
        this.semanticRisk = incident.getSemanticRisk();
        this.incidentType = incident.getIncidentType();
    }
}

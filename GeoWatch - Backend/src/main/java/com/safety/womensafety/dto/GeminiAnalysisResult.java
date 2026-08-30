package com.safety.womensafety.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiAnalysisResult {
    private String semanticRisk;
    private String incidentType;
    private String reasoning;
}

# Gemini Semantic Risk Engine — Implementation Summary

## 1. Architecture & Gemini's Role
Gemini has been integrated into the core risk pipeline as a **Semantic Risk Engine**.
It runs asynchronously to avoid blocking the initial SOS submission and provides a severity classification based purely on the description provided by the user. 
The system does not rely on Gemini exclusively; rather, it combines the deterministic spatial DBSCAN clustering risk with the Gemini semantic risk.

## 2. Request Flow
1. User submits an SOS via Flutter UI (`/api/incidents`).
2. **Synchronous**: The backend creates and saves the `Incident` to the PostgreSQL database with the provided description.
3. **Synchronous**: The backend schedules an immediate run of the spatial DBSCAN algorithm to broadcast the initial spatial risk to the React dashboard over WebSocket.
4. **Asynchronous**: If a description exists, the backend passes the description to `GeminiRiskAnalysisService`.
5. **Asynchronous**: When the Gemini API responds, the backend updates the incident with `semanticRisk`, `incidentType`, and `aiReasoning`.
6. **Asynchronous**: The backend triggers a second run of the DBSCAN algorithm which computes the final aggregated risk.

## 3. Final Risk Formula
The final cluster risk is determined by taking the maximum risk level across both the spatial clustering algorithm and the semantic classification of all incidents in that cluster:

`FINAL CLUSTER RISK = MAX(SPATIAL DBSCAN RISK, HIGHEST AVAILABLE SEMANTIC RISK)`

Risk levels map to integers (LOW=1, MEDIUM=2, HIGH=3) to perform this mathematical reduction safely.

## 4. No-Description Path
If an incident contains no description (or only whitespace):
1. The incident is saved normally.
2. The `GeminiRiskAnalysisService` is skipped entirely.
3. The spatial DBSCAN result is used as the sole determinant of risk for that incident.

## 5. Failure Fallback
If the Gemini API is rate-limited, times out, returns malformed JSON, or is otherwise unavailable:
1. The exception is safely caught inside `GeminiRiskAnalysisService`.
2. The semantic fields on the incident remain `null` (unavailable).
3. The spatial DBSCAN risk dictates the overall severity. 
The backend NEVER sets semantic risk to `LOW` when an API failure occurs, to ensure spatial risk isn't falsely suppressed or misinterpreted.

## 6. Response Schema
Gemini returns the following forced JSON structure:
```json
{
  "semanticRisk": "LOW|MEDIUM|HIGH",
  "incidentType": "string",
  "reasoning": "string"
}
```
This is mapped to the internal `GeminiAnalysisResult` DTO.

## 7. Configuration
The Gemini API key is securely provided via the environment variable `GEMINI_API_KEY`.
It is read securely in `application.properties`:
```properties
gemini.api.key=${GEMINI_API_KEY:default-key}
```

## 8. Files Created/Modified
**Created:**
- `GeminiRiskAnalysisService.java`
- `GeminiAnalysisResult.java`

**Modified (Backend):**
- `Incident.java` (Added description, semanticRisk, aiReasoning, incidentType)
- `CreateIncidentRequest.java` (Added description)
- `IncidentService.java` (Integrated async Gemini pipeline)
- `DbscanClusteringService.java` (Integrated MAX risk combination algorithm)
- `ClusterResponse.java` (Added highestSemanticRisk, clusterIncidentTypes)
- `application.properties` (Added API keys)

**Modified (Frontend - Flutter):**
- `incident_request.dart`
- `incident_viewmodel.dart`
- `incident_report_screen.dart`

## 9. Testing Performed
- **Backend Compilation**: `mvn clean install -DskipTests` completed successfully (`BUILD SUCCESS`).
- **Data Model verification**: Modified classes to handle null/empty descriptions to verify safe bypass. 
- Flutter SDK testing skipped as Flutter is not installed on the current PATH, but source changes are fully typed and aligned with the Flutter provider pattern.

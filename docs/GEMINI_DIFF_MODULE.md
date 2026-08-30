# Gemini Semantic Risk Engine — Integration Diff & Design Specification

## 1. CURRENT SYSTEM BASELINE
**Incident Reporting Flow:**
1. **Flutter UI (`incident_report_screen.dart`)**: User fills out Name, Phone Number, Incident Type (dropdown), and an optional Description. The submit button triggers `_submit(vm, event)`.
2. **Flutter ViewModel (`incident_viewmodel.dart`)**: The `submitIncident` method accepts only `eventId`, `name`, and `phoneNumber`. It builds an `IncidentRequest` model with these fields plus the current GPS `latitude`/`longitude`.
3. **Flutter Repository/Service (`api_service.dart`)**: Sends the payload via `POST /api/incidents`.
4. **Backend Controller (`IncidentController.java`)**: Receives the payload into a `CreateIncidentRequest` DTO (which only contains `eventId`, `name`, `phoneNumber`, `latitude`, `longitude`).
5. **Backend Service (`IncidentService.java`)**: Validates rate limits and geofencing. Saves the incident to the database via `IncidentRepository`. 
6. **Backend Trigger**: `IncidentService` calls `triggerClusteringAndBroadcast(eventId)` which schedules a debounced execution of the clustering engine.
7. **Backend Clustering (`DbscanClusteringService.java`)**: Processes all unresolved incidents for the event. Calculates clusters and determines the risk based purely on size (LOW < 3, MEDIUM 3-5, HIGH >= 6).
8. **Backend Broadcast**: Returns a list of `ClusterResponse` (containing `centerLat`, `centerLng`, `incidentCount`, `riskLevel`) and broadcasts to `/topic/risk-updates/{eventId}`.
9. **React Client (`websocket.ts` & `AdminEvents.tsx`)**: Receives the STOMP message and plots it on the Leaflet map, coloring the zone based on the `riskLevel`.

## 2. CURRENT DESCRIPTION SUPPORT
**STATUS: PARTIALLY IMPLEMENTED IN UI ONLY, MISSING IN API/BACKEND**
- **Flutter UI**: Yes. `incident_report_screen.dart` has an `InputField` for `Description (Optional)` mapped to `_descriptionController`.
- **Flutter ViewModel**: NO. `_submit()` ignores the description. `IncidentViewModel.submitIncident` does not accept a description parameter.
- **Flutter API Request (`incident_request.dart`)**: NO. Does not contain a description field.
- **Backend DTO (`CreateIncidentRequest.java`)**: NO. Does not contain a description field.
- **Backend Entity (`Incident.java`)**: NO. Does not contain a description column in the PostgreSQL database.

**Conclusion:** The description input exists on the screen, but is completely dropped when the user hits "Submit Report". We must wire this all the way down to the database.

## 3. GEMINI INTEGRATION ARCHITECTURE
**Chosen Architecture: D (Gemini asynchronously after the incident is accepted)**

**Flow:**
Flutter → Spring Boot (`IncidentService`) → Persistence → Schedule Async Gemini Task → Gemini → Update Incident in DB with Semantic Risk → Trigger DBSCAN/Broadcast → React Dashboard.

**Why this is the safest approach:**
The primary goal of SOS reporting is reliability. If Gemini is down, rate-limited, or slow, the initial `POST /api/incidents` must still return `200 OK` instantly so the user knows help was requested.
By executing Gemini asynchronously *after* the incident is safely in the database, a Gemini failure only delays the *semantic* risk classification; the spatial DBSCAN system will still function normally.

## 4. GEMINI INPUT CONTRACT
**Data sent to Gemini:**
- `description` (The user-provided string).

*Note: Context like `latitude`/`longitude` or event details are irrelevant for determining the semantic severity of "someone is bleeding" vs "I lost my keys". Keeping the prompt focused on the description alone saves tokens, reduces latency, and prevents Gemini from being confused by raw spatial data.*

## 5. GEMINI OUTPUT CONTRACT
We will force Gemini to return structured JSON containing exactly:
- `semanticRisk` (Enum: `LOW`, `MEDIUM`, `HIGH`)
- `incidentType` (String: e.g., "Medical Emergency", "Assault", "General Inquiry")
- `reasoning` (String: 1-2 sentence explanation of why this risk was chosen)

**Rules:**
- **Required fields**: All three.
- **Invalid Output Handling**: If Gemini fails to return valid JSON, or returns an invalid risk enum, the system will catch the exception and default the semantic risk to `LOW` (relying on DBSCAN for escalation).

## 6. SEMANTIC RISK RULES
The system prompt will instruct Gemini to classify based on immediate threat to life/safety:
- **HIGH**: Weapons, active violence, severe medical emergencies, fires, crowd crush, or situations where immediate intervention is critical.
- **MEDIUM**: Harassment, aggressive behavior, suspicious activity, minor medical assistance, severe overcrowding without immediate injury.
- **LOW**: Lost items, directions, noise complaints, general assistance, vague or unintelligible descriptions ("help", "idk").

*Instruction to Gemini: "If the description is vague, lacks context, or does not explicitly describe a dangerous situation (e.g. 'Help', 'Come here'), classify the risk as LOW to avoid hallucinating danger. Provide a concise reason."*

## 7. FINAL RISK ENGINE
The combination logic will live in `DbscanClusteringService.java` when computing the final `ClusterResponse`.

Risk Mapping: `LOW = 1`, `MEDIUM = 2`, `HIGH = 3`.
`Final Cluster Risk = MAX(DBSCAN Spatial Risk, MAX(All Incident Semantic Risks in Cluster))`

**Examples:**
- DBSCAN LOW (2 incidents) + Gemini HIGH (1 of the incidents is "gun") = **HIGH**
- DBSCAN HIGH (10 incidents) + Gemini LOW (all incidents are "lost item") = **HIGH**
- DBSCAN MEDIUM (4 incidents) + Gemini LOW = **MEDIUM**

## 8. NO-DESCRIPTION PATH
If `request.getDescription()` is null or empty (whitespace), the backend will:
1. Save the incident with semanticRisk = `LOW`.
2. Bypass the `GeminiRiskAnalysisService` entirely.
3. Call `triggerClusteringAndBroadcast()` immediately.
*Result: Zero Gemini API calls, zero latency penalty, relies 100% on spatial density.*

## 9. VAGUE / INSUFFICIENT DESCRIPTION
Descriptions like "Help" will be passed to Gemini. The system prompt will explicitly instruct the model: *If the text is vague or insufficient to determine an actual emergency, default to LOW risk and state "Insufficient context".*
The spatial DBSCAN engine will automatically escalate the risk if multiple people in the same area send vague "Help" messages.

## 10. DATABASE CHANGES
**Entity:** `Incident.java` (Table: `incidents`)
- `description` (String, nullable) - Stores the user's input.
- `semanticRisk` (String, nullable) - Stores "LOW", "MEDIUM", "HIGH".
- `aiReasoning` (String, nullable) - Stores Gemini's explanation.
- `incidentType` (String, nullable) - Stores Gemini's categorized type.

*Since `spring.jpa.hibernate.ddl-auto=update` is used, adding these fields to the entity will automatically create the columns.*

## 11. API CONTRACT CHANGES
**REST POST `/api/incidents`**
`CreateIncidentRequest.java`:
```java
// ADDED FIELD
private String description;
```

## 12. WEB SOCKET CHANGES
**Topic:** `/topic/risk-updates/{eventId}`
`ClusterResponse.java` payload additions:
```java
// ADDED FIELDS
private String highestSemanticRisk; // "LOW", "MEDIUM", "HIGH"
private List<String> clusterIncidentTypes; // E.g., ["Medical Emergency", "Assault"]
```
*Backward compatibility:* The React dashboard currently looks at the overall `riskLevel`. By simply updating the overall `riskLevel` field to the calculated MAX risk, the dashboard will immediately visualize the semantic escalation without requiring frontend logic changes. The new fields can be surfaced in UI side-panels later.

## 13. FRONTEND CHANGES (React)
**Minimal Changes Required:**
- None immediately necessary to achieve the core visual effect, since `riskLevel` on the websocket payload will be correctly elevated. 
- *Nice-to-have*: Update `AdminEvents.tsx` to display the new `clusterIncidentTypes` in a tooltip or side panel when hovering over a cluster.

## 14. MOBILE CHANGES (Flutter)
1. **`incident_report_screen.dart`**: Update `_submit` to read `_descriptionController.text.trim()` and pass it to `vm.submitIncident`.
2. **`incident_viewmodel.dart`**: Update `submitIncident` parameters to accept `String? description`. Pass it to `IncidentRequest`.
3. **`incident_request.dart`**: Add `final String? description;` to the model and `toJson()` method.

## 15. GEMINI SERVICE MODULE
**File:** `src/main/java/com/safety/womensafety/service/GeminiRiskAnalysisService.java`
**Responsibilities:**
- Encapsulate the HTTP/SDK call to the Gemini API.
- Maintain the System Prompt for classification.
- Parse the structured JSON response.
- Expose a method: `public GeminiAnalysisResult analyzeIncident(String description)`

## 16. GEMINI API CLIENT
**Integration Pattern:** Simple Spring `RestTemplate` or `WebClient` hitting the Gemini REST API directly. No need for a heavy SDK dependency for a simple prompt completion.
**Environment Variable:** `GEMINI_API_KEY`.
**Config:** Read via `@Value("${gemini.api.key}")`.

## 17. FAILURE & FALLBACK STRATEGY
If the Gemini API times out, returns a 429 quota error, or returns unparseable JSON:
1. Catch the exception inside `GeminiRiskAnalysisService`.
2. Log the error (without exposing the API key).
3. Return a default fallback object: `new GeminiAnalysisResult("LOW", "Unknown", "AI Analysis Failed")`.
4. The incident persists with `semanticRisk = LOW` and the spatial pipeline continues uninterrupted.

## 18. FREE-TIER / RATE-LIMIT AWARENESS
- By explicitly bypassing Gemini for empty descriptions, we conserve quota.
- The fallback strategy safely handles HTTP 429 Too Many Requests without failing the SOS report.

## 19. PERFORMANCE IMPACT
- **SOS Submission (`POST /api/incidents`)**: Remains lightning fast and synchronous.
- **Gemini Processing**: Asynchronous. If an incident has a description, it goes into an async thread to wait for Gemini (typically 1-3 seconds). 
- **Clustering/Broadcast**: Triggers once immediately on SOS submission (capturing the raw location instantly), and triggers *again* once the async Gemini task completes and saves the semantic risk. 

## 20. EXACT FILE-BY-FILE DIFF PLAN

### FILES TO CREATE
1. **`GeoWatch - Backend/src/main/java/com/safety/womensafety/service/GeminiRiskAnalysisService.java`**
   - Purpose: The core module handling HTTP requests to Gemini, prompting, and JSON parsing.
2. **`GeoWatch - Backend/src/main/java/com/safety/womensafety/dto/GeminiAnalysisResult.java`**
   - Purpose: DTO for mapping the structured output from Gemini.

### FILES TO MODIFY
1. **`GeoWatch - Backend/src/main/java/com/safety/womensafety/model/Incident.java`**
   - Changes: Add `description`, `semanticRisk`, `aiReasoning`, `incidentType`. Add Getters/Setters.
2. **`GeoWatch - Backend/src/main/java/com/safety/womensafety/dto/CreateIncidentRequest.java`**
   - Changes: Add `description`.
3. **`GeoWatch - Backend/src/main/java/com/safety/womensafety/service/IncidentService.java`**
   - Changes: Wire the `description` from DTO to Entity. Add logic: if description is present, submit async task to `GeminiRiskAnalysisService`, then update DB and trigger clustering.
4. **`GeoWatch - Backend/src/main/java/com/safety/womensafety/service/DbscanClusteringService.java`**
   - Changes: Implement the MAX(spatialRisk, semanticRisk) logic when building `ClusterResponse`.
5. **`GeoWatch - Backend/src/main/java/com/safety/womensafety/dto/ClusterResponse.java`**
   - Changes: Add `highestSemanticRisk` and `clusterIncidentTypes`.
6. **`GeoWatch - Backend/src/main/resources/application.properties`**
   - Changes: Add `gemini.api.key=${GEMINI_API_KEY}` and `gemini.api.url=...`.
7. **`GeoWatch - Application/lib/models/incident_request.dart`**
   - Changes: Add `description` field.
8. **`GeoWatch - Application/lib/viewmodels/incident_viewmodel.dart`**
   - Changes: Update `submitIncident` method signature.
9. **`GeoWatch - Application/lib/screens/incident_report_screen.dart`**
   - Changes: Pass `_descriptionController.text.trim()` to the view model.

### FILES TO LEAVE UNCHANGED
- `GeoWatch - Frontend/src/pages/AdminEvents.tsx` (Unless implementing the nice-to-have tooltip).
- `IncidentController.java`

## 21. BEFORE vs AFTER ARCHITECTURE
**Before:**
SOS → save Incident → DBSCAN computes risk based on count → WebSocket → Dashboard.

**After:**
SOS (No Description) → save Incident → DBSCAN computes risk based on count → WebSocket → Dashboard.

SOS (With Description) → save Incident (Risk = LOW) → trigger immediate DBSCAN/WebSocket → Async Gemini Call → Update Incident Semantic Risk → trigger secondary DBSCAN/WebSocket with elevated final risk → Dashboard.

## 22. SECURITY
- The Flutter and React apps remain completely ignorant of Gemini.
- `GEMINI_API_KEY` is injected strictly via environment variables.

## 23. TEST PLAN
- **Empty Description**: Submit SOS with no description. Verify 0 Gemini calls, instant WebSocket update, and risk determined by density.
- **Vague Description**: Submit "Help". Verify Gemini returns LOW risk with "Insufficient context".
- **Severe Description**: Submit "Man with a gun". Verify Gemini returns HIGH risk. Map cluster should immediately turn RED, even if it's just 1 person.
- **Failover**: Disconnect network/invalidate API key. Submit SOS with description. Verify incident still saves and cluster appears on the map without crashing.

## 24. HACKATHON SCOPE CONTROL
- **REQUIRED**: Modifying Flutter API request, updating Spring Boot Incident entity, adding `GeminiRiskAnalysisService`, modifying `DbscanClusteringService` risk math.
- **NICE TO HAVE**: Showing the AI explanation tooltip on the React dashboard.
- **DO NOT TOUCH**: React map rendering logic (it already dynamically colors based on `riskLevel`), admin authentication, geofencing logic.

## 25. FINAL IMPLEMENTATION CHECKLIST
- [ ] Flutter `incident_report_screen` wired to API
- [ ] Flutter `incident_request.dart` updated
- [ ] Spring Boot `CreateIncidentRequest` updated
- [ ] Spring Boot `Incident` entity updated
- [ ] Spring Boot `GeminiRiskAnalysisService` implemented
- [ ] Spring Boot `IncidentService` updated for async Gemini trigger
- [ ] Spring Boot `DbscanClusteringService` updated for final risk logic
- [ ] Railway environment variable `GEMINI_API_KEY` configured

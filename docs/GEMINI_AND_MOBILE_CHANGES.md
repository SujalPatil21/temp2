# GEMINI & MOBILE APP CHANGES

This document outlines the end-to-end integration of Google Gemini into the GeoWatch platform, specifically for incident reporting, risk classification, and voice-to-text UX. This reflects the actual implemented source code and architecture as of the current state.

---

## 1. GEMINI INTEGRATION

The GeoWatch backend now uses Google Gemini to perform semantic risk analysis on incident reports.

*   **Gemini semantic risk engine**: Evaluates natural language incident descriptions for severity.
*   **Gemini model currently used**: `gemini-1.5-flash`
*   **Gemini API integration**: Implemented via Spring's `RestTemplate` directly calling the Google Generative AI REST API endpoint (`https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent`).
*   **`GeminiRiskAnalysisService`**: A new dedicated Spring service that builds prompts, parses responses, and handles HTTP execution.
*   **`GeminiAnalysisResult`**: A structured Java DTO mapped to the model's JSON response.
*   **`GEMINI_API_KEY`**: Sourced from environment variables and securely passed as a request header `x-goog-api-key`.
*   **Gemini structured JSON response**: The model is instructed to strictly return a `response.text` containing:
    ```json
    {
      "semanticRisk": "HIGH",
      "incidentType": "ASSAULT",
      "reasoning": "A person was physically attacked."
    }
    ```
*   **semanticRisk**: Mapped to three valid states: `LOW`, `MEDIUM`, or `HIGH`.
*   **incidentType**: Categorized classification of the event.
*   **reasoning / AI explanation**: Context explaining why Gemini chose the risk level.
*   **Asynchronous Gemini processing**: The Gemini API call executes asynchronously in a separate thread. This prevents the initial `POST /api/incidents` request from blocking while awaiting the AI response.
*   **Gemini called only when description exists**: The `IncidentService` only invokes Gemini if the user provided a non-empty `description`.
*   **Gemini skipped when description is empty/null**: If the description is blank, the AI call is entirely bypassed.
*   **Gemini failure behavior**: If the Gemini call times out, encounters a 4xx/5xx error, or fails to parse, it logs the error, leaves `semanticRisk` as null, and falls back gracefully to DBSCAN-only clustering.
*   **Final risk calculation**: `MAX(DBSCAN risk, Gemini semantic risk)`.
*   **Raising risk limits**: Gemini can elevate the risk (e.g., a size-1 cluster goes from LOW to HIGH because of a severe description). It *cannot* lower the risk if DBSCAN independently calculates a high risk due to volume (e.g., 10 incidents naturally calculate to HIGH, and a Gemini LOW will not suppress the spatial HIGH).

---

## 2. INCIDENT DESCRIPTION INTEGRATION

The Flutter SOS screen previously discarded user descriptions, sending only GPS coordinates. The incident pipeline is now fully wired:

**Trace:**
1. Flutter `_descriptionController` (Mobile)
2. `IncidentRequest` JSON body (Mobile API Client)
3. `POST /api/incidents` endpoint (Spring Boot Controller)
4. `CreateIncidentRequest` DTO (Backend)
5. `IncidentService.createIncident()` (Backend)
6. `Incident` entity updated with `.setDescription()`
7. Saved to PostgreSQL database.
8. If description is present, passed asynchronously to `GeminiRiskAnalysisService`.

*Files modified to enable this wireup:*
*   `IncidentService.java`
*   `CreateIncidentRequest.java`
*   `Incident.java`
*   `IncidentController.java`
*   `incident_report_screen.dart`
*   `incident_viewmodel.dart`
*   `incident_repository.dart`

---

## 3. DATABASE CHANGES

The `Incident` entity and corresponding PostgreSQL `incident` table were extended to capture the new reporting fields. 

**Fields Added:**
*   `description` (String)
*   `semanticRisk` (String)
*   `incidentType` (String)
*   `aiReasoning` (String)

**Hibernate Behavior:**
The database uses `spring.jpa.hibernate.ddl-auto=update`, which automatically applied the schema changes by appending the new columns to the `incident` table without destroying existing data.

---

## 4. RISK ENGINE CHANGES

Previously, risk was determined strictly by DBSCAN spatial volume (`DbscanClusteringService`).

**New Architecture:**
`Final Risk = MAX(DBSCAN spatial risk, Gemini semantic risk)`

The exact `MAX` rule maps `LOW=1`, `MEDIUM=2`, and `HIGH=3`.

**Examples:**
*   **1 isolated incident + severe description**: 
    *   DBSCAN evaluates isolated incident as size 1 = `LOW`.
    *   Gemini evaluates description as = `HIGH`.
    *   `MAX(LOW, HIGH)` → final cluster broadcasted as **HIGH**.
*   **No description**:
    *   Gemini is skipped. Semantic risk is `NULL` (0).
    *   DBSCAN decides based purely on cluster size.
*   **Gemini failure**:
    *   Semantic result unavailable (0).
    *   DBSCAN decides based purely on cluster size.

*Bug Fix:* We modified the DBSCAN post-processing loop so that isolated incidents (noise) are correctly retained as size-1 clusters. Previously they were silently discarded if any other cluster existed on the map.

---

## 5. GEMINI TESTING

The following tests were actively executed to verify the Gemini integration:
*   **Standalone Gemini API test**: Tested `GeminiRiskAnalysisService` isolated from clustering.
*   **Model verification**: Verified `gemini-1.5-flash` accepts our system prompt.
*   **Live API request**: Sent raw cURL commands to test serialization.
*   **Semantic HIGH test**: Submitted "Someone is carrying a weapon and attacking people" to ensure the risk elevates correctly.
*   **E2E `/api/incidents` test**: Followed the pipeline from database creation through async processing.
*   **No-description test**: Submitted empty payloads to verify Gemini is bypassed.
*   **Low-risk description test**: Submitted "Lost my wallet" to verify LOW baseline.
*   **Risk combination test**: Verified DBSCAN and Gemini merge states correctly.
*   **WebSocket verification**: Confirmed the dashboard receives the final combined risk dynamically.

---

## 6. LOCAL DEVELOPMENT CHANGES

A dedicated `local` Spring Boot profile was established to allow isolated execution.

*   **`application-local.properties`**: Overrides specific database and server parameters for local execution.
*   **Local PostgreSQL**: Pointed at `localhost:5432` with database `womenSafety`.
*   **Localhost Backend**: Spring Boot executes locally on port 8080.
*   **Local React Frontend**: Connects directly to the Spring Boot REST/WebSocket APIs on 8080.
*   **`adb reverse tcp:8080 tcp:8080`**: Employed to pipe the physical Android device's localhost requests directly to the host machine's Spring Boot server.

The Gemini API key is intentionally kept out of committed property files and is supplied strictly via the `GEMINI_API_KEY` environment variable.

---

## 7. FLUTTER DESCRIPTION UX

The mobile app's SOS workflow was redesigned to make description capture efficient.

*   **Optional Description**: Users can skip the step completely.
*   **Existing Description Field**: A streamlined multi-line text input field.
*   **Predefined Local Suggestions**: A hardcoded list of common incident phrases ("I saw a gun", "People are fighting").
*   **Autocomplete/Filtering**: Typing filters the suggestion list locally.
*   **User Selection**: Tapping a suggestion auto-fills the field.
*   **User Customization**: Users can type arbitrary text, select a suggestion, edit the suggestion, or clear it.
*   **Skip Description**: A dedicated text button allows instantaneous submission without typing.
*   **Submission**: The final text inside the input controller is what binds to the `POST` request.

*Relevant Files:*
*   `lib/screens/incident_report_screen.dart`

---

## 8. VOICE-TO-TEXT

To accommodate users in distress, voice input was seamlessly integrated alongside the typing flow.

*   **Package used**: `speech_to_text` (version ^7.4.0).
*   **Microphone permission**: Handled dynamically using `permission_handler`. If denied, the app does not break; users can still type.
*   **Speech-to-text flow**: A `[ 🎤 Speak ]` button initializes the native Android SpeechRecognizer.
*   **UX**: The recognized speech immediately populates the autocomplete and description fields in real-time.
*   **Editing**: The user maintains total control and can manually type over the transcription.
*   **Architecture**: No audio is sent to the backend. No audio is sent to Gemini. No new API endpoints were required. The voice input acts strictly as a local keyboard alternative.

---

## 9. APIs

*   `New APIs added: NONE`
*   The existing `POST /api/incidents` was extended. It now accepts the `description` string inside the JSON payload.
*   The Gemini REST interaction occurs entirely server-to-server. There is no Gemini endpoint exposed to the Flutter or React clients.

---

## 10. FILE-BY-FILE CHANGE LIST

| File | Change | Purpose | Type |
|---|---|---|---|
| `GeminiRiskAnalysisService.java` | Created | Handles API interactions with Google Generative AI | Backend |
| `IncidentService.java` | Modified | Async invocation of Gemini; saves description | Backend |
| `Incident.java` | Modified | Added description, semanticRisk, incidentType fields | Backend |
| `CreateIncidentRequest.java` | Modified | Added description field to DTO | Backend |
| `DbscanClusteringService.java` | Modified | Applies MAX risk logic; fixes isolated incident drops | Backend |
| `application-local.properties` | Created | Configures local DB/ports | Configuration |
| `Dashboard.tsx` | Modified | Removed hardcoded `#ff0000` marker color, respects dynamic risk | Frontend |
| `incident_report_screen.dart` | Modified | Implemented Autocomplete suggestions and Voice-to-Text | Flutter |
| `pubspec.yaml` | Modified | Added `speech_to_text` dependency | Configuration |
| `AndroidManifest.xml` | Modified | Added `RECORD_AUDIO` and `RecognitionService` permissions | Configuration |

---

## 11. REMOVED / ABANDONED WORK

`NOT PART OF FINAL IMPLEMENTATION`:
*   Verification/Corroboration feature (Organizer states, manual corroboration flags).
*   CRITICAL risk level (reverted back to LOW/MEDIUM/HIGH).
*   Gemini directly from Flutter (rejected to protect API keys and reduce mobile payload).
*   Separate Gemini REST endpoint (rejected to keep the pipeline atomic).

---

## 12. CURRENT END-TO-END ARCHITECTURE

**WITH DESCRIPTION:**
Flutter SOS → typing/voice → `POST /api/incidents` → PostgreSQL → DBSCAN → async Gemini → semantic risk updated → MAX risk computed → WebSocket broadcast → React dashboard

**WITHOUT DESCRIPTION:**
Flutter SOS → `POST /api/incidents` → PostgreSQL → DBSCAN → WebSocket broadcast → React dashboard

**VOICE INPUT:**
Voice → `speech_to_text` (Local Device) → `_descriptionController` → normal incident flow

---

## 13. CURRENT STATUS

### IMPLEMENTED
- Gemini async semantic analysis.
- DBSCAN + Gemini MAX risk integration.
- Flutter manual description UX + Autocomplete.
- Flutter Speech-to-Text (`speech_to_text`) integration.
- Backend isolated incident (noise) retention fix.
- Frontend marker color dynamic mapping fix.

### PARTIALLY IMPLEMENTED
- (None currently)

### NOT IMPLEMENTED
- Manual corroboration workflows.
- Audio recording transmission.

### CURRENT BLOCKERS
- For local physical device testing on Android 9+, `android:usesCleartextTraffic="true"` is required in `AndroidManifest.xml` to allow `http://localhost:8080` connections. This is currently missing.

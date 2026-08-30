# MobAlert

> **Real-time crowd safety intelligence powered by geospatial clustering and Gemini AI.**

[![Live Demo](https://img.shields.io/badge/Live-Demo-success)](https://geo-watch.pages.dev/)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-green)](#-tech-stack)
[![Frontend](https://img.shields.io/badge/Frontend-React%20%2B%20TypeScript-blue)](#-tech-stack)
[![Mobile](https://img.shields.io/badge/Mobile-Flutter-02569B)](#-tech-stack)
[![Database](https://img.shields.io/badge/Database-PostgreSQL-336791)](#-tech-stack)
[![Realtime](https://img.shields.io/badge/Realtime-WebSocket%20%2B%20STOMP-orange)](#-websocket-api)
[![AI](https://img.shields.io/badge/AI-Gemini-purple)](#-gemini-ai-integration)
[![Algorithm](https://img.shields.io/badge/Algorithm-DBSCAN-red)](#-dbscan-clustering)

MobAlert is a real-time crowd safety monitoring platform for concerts, festivals, college events, sports events, public gatherings, and other high-density environments.

Participants use a Flutter mobile application to discover active events and report incidents with their GPS location and an optional description. The Spring Boot backend validates and stores incidents, performs geospatial clustering with DBSCAN, analyzes incident descriptions with Gemini when available, and publishes updated risk zones over WebSockets.

Organizers use a React/TypeScript dashboard to monitor live event conditions through maps, risk zones, realtime incident clusters, and a read-only Gemini Event Intelligence chatbot.

---

##  Table of Contents

- [Overview](#-overview)
- [Core Workflow](#-core-workflow)
- [Features](#-features)
- [Gemini AI Integration](#-gemini-ai-integration)
- [Risk Model](#-risk-model)
- [Event Geofencing](#-event-geofencing)
- [DBSCAN Clustering](#-dbscan-clustering)
- [Realtime WebSocket System](#-realtime-websocket-system)
- [Gemini Event Intelligence](#-gemini-event-intelligence)
- [Multilingual and Voice Input](#-multilingual-and-voice-input)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Environment Variables](#-environment-variables)
- [Running Locally](#-running-locally)
- [API Reference](#-api-reference)
- [Database](#-database)
- [Performance](#-performance)
- [Screenshots & Demo](#-screenshots--demo)
- [Development Notes](#-development-notes)
- [Limitations](#-limitations)
- [Contributing](#-contributing)
- [License](#-license)
- [Authors](#-authors)

---

##  Overview

Crowd-safety systems often receive many individual reports without enough context for an organizer to determine what deserves immediate attention.

MobAlert combines:

- **Location intelligence** through GPS and geofencing
- **Spatial intelligence** through DBSCAN clustering
- **Semantic intelligence** through Gemini
- **Realtime distribution** through WebSockets
- **Operational visibility** through a live organizer dashboard

Instead of treating every SOS as an isolated event, the platform turns incident reports into evolving geospatial risk intelligence.

---

##  Core Workflow

### Participant Flow

```text
Participant opens app
        ↓
Discovers nearby active events
        ↓
Selects an event
        ↓
Presses and holds SOS / Report
        ↓
Optional incident description
        ↓
POST /api/incidents
        ↓
Backend validates event + geofence + rate limit
        ↓
Incident persisted to PostgreSQL
        ↓
DBSCAN recalculates recent spatial clusters
        ↓
If description exists → Gemini semantic analysis
        ↓
Final risk = MAX(DBSCAN Risk, Gemini Risk)
        ↓
WebSocket broadcast
        ↓
React dashboard updates in realtime
```

### Admin Flow

```text
Admin opens event dashboard
        ↓
Live map + cluster state
        ↓
WebSocket receives updates
        ↓
Risk zones change in realtime

Admin can also ask:
"What are the highest-risk clusters?"
        ↓
POST /api/admin/chat
        ↓
Gemini uses read-only backend tools
        ↓
Live event data retrieved
        ↓
Gemini reasons over current data
        ↓
Answer shown in dashboard
```

---

#  Features

##  Participant Mobile Application

- Nearby event discovery using location
- Event selection and joining
- Press-and-hold SOS/report interaction
- Haptic feedback on emergency actions
- Optional incident description
- Free-form custom descriptions
- Predefined local description suggestions
- Local autocomplete/filtering
- Voice-to-text incident descriptions
- English, Hindi, and Marathi language selection for voice input
- Geofence validation
- Database-backed rate limiting
- Incident submission
- Incident resolution support
- User settings/profile information
- Consistent emergency-oriented mobile UX

### Emergency Interaction

The SOS action uses a deliberate press-and-hold interaction to reduce accidental reports.

```text
Press and hold
     ↓
One haptic feedback
     ↓
Progress animation
     ↓
Release early → Cancel
     ↓
Complete hold
     ↓
Continue to incident report
```

The final report action also provides a single haptic confirmation.

---

##  Organizer Admin Dashboard

- Admin registration/login
- Active event management
- Event creation
- Live event map
- Risk-zone visualization
- Heatmap visualization
- DBSCAN cluster visualization
- Realtime WebSocket/STOMP updates
- Incident monitoring
- Gemini Event Intelligence chatbot
- English/Hindi/Marathi chatbot language selection
- Voice input for the chatbot
- Read-only live event analysis

---

#  Gemini AI Integration

Gemini is integrated into the **core safety pipeline**, rather than being used only for a demonstration chatbot.

## Incident-Level Semantic Analysis

When a participant submits a meaningful description, the backend sends that description to Gemini.

Gemini interprets the incident and returns structured information such as:

```json
{
  "semanticRisk": "HIGH",
  "incidentType": "Active Violence",
  "reasoning": "The report describes an immediate threat to life and public safety."
}
```

### Supported Semantic Risk Levels

```text
LOW
MEDIUM
HIGH
```

Gemini can identify the meaning of reports involving situations such as:

- Active violence
- Weapons
- Medical emergencies
- Fire or smoke
- Crowd danger
- Harassment
- Aggressive behavior
- Suspicious activity
- General assistance
- Lost items

The classification is semantic rather than a simple keyword lookup.

---

## Conditional Gemini Invocation

Gemini is invoked only when a meaningful incident description exists.

### With Description

```text
Description
    ↓
Gemini
    ↓
Semantic Risk
```

### Without Description

```text
No description
    ↓
Gemini skipped
    ↓
DBSCAN-only risk
```

This avoids unnecessary API calls and keeps the emergency path responsive.

---

## Gemini Failure Fallback

Gemini is not allowed to become a single point of failure.

If Gemini:

- times out
- is unavailable
- returns an invalid result
- hits an API failure

then:

```text
Incident still succeeds
        ↓
semantic risk unavailable
        ↓
DBSCAN risk remains authoritative
```

A Gemini failure is **not** converted into a fake LOW result.

---

## Final Risk Calculation

The current final risk is:

```text
FINAL RISK = MAX(DBSCAN SPATIAL RISK, GEMINI SEMANTIC RISK)
```

This allows semantic analysis to escalate an incident even when only a small number of reports exist.

Example:

```text
One incident
     ↓
DBSCAN = LOW

Description:
"Someone is carrying a weapon and attacking people."

     ↓
Gemini = HIGH

     ↓
FINAL RISK = HIGH
```

Likewise:

```text
DBSCAN = HIGH
Gemini = LOW

FINAL RISK = HIGH
```

Gemini never lowers an already higher spatial risk.

---

#  Risk Model

MobAlert keeps **risk** separate from operational urgency/priority.

## Spatial Risk

Current DBSCAN thresholds:

| Incident Count | Spatial Risk |
|---:|---|
| `< 3` | LOW |
| `3–5` | MEDIUM |
| `>= 6` | HIGH |

## Semantic Risk

Gemini returns:

| Semantic Result | Meaning |
|---|---|
| LOW | Low-severity or non-urgent situation |
| MEDIUM | Concerning incident requiring attention |
| HIGH | Severe or potentially immediate safety threat |

## Final Risk

```text
MAX(Spatial Risk, Semantic Risk)
```

There is currently **no CRITICAL risk level**.

---

#  Event Geofencing

Every incident is validated against the event's configured geofence.

The backend calculates distance between the incident's coordinates and the event center using the Haversine formula.

Current behavior:

```text
distance <= event radius + 30 meters
```

If an incident is beyond the permitted boundary plus the 30-meter buffer, the report is rejected.

This prevents unrelated external locations from contaminating an event's safety intelligence.

---

#  DBSCAN Clustering

MobAlert uses a custom DBSCAN implementation to convert individual incidents into geographic clusters.

Current parameters:

```text
EPS     = 50 meters
MIN_PTS = 2
```

The implementation uses a spatial grid/index to reduce unnecessary pairwise comparisons.

Recent incident activity is clustered rather than treating all historical incidents as permanently active.

Conceptually:

```text
Incidents
   ↓
Spatial index
   ↓
Neighbor search
   ↓
DBSCAN
   ↓
Cluster centers + counts
   ↓
Spatial risk
```

---

#  Realtime WebSocket System

The backend broadcasts updated cluster information using STOMP over WebSockets.

### Endpoint

```text
/ws
```

SockJS fallback is supported where configured.

### Topic

```text
/topic/risk-updates/{eventId}
```

Example:

```text
/topic/risk-updates/1
```

### Typical Data Flow

```text
New incident
    ↓
Persist
    ↓
DBSCAN
    ↓
Risk update
    ↓
SimpMessagingTemplate
    ↓
STOMP topic
    ↓
React dashboard
```

The dashboard updates without requiring a page refresh.

---

#  Gemini Event Intelligence

The admin dashboard contains a **read-only Gemini Event Intelligence chatbot**.

It is designed to answer questions about the live event rather than behave as a generic assistant.

Examples:

```text
What is happening right now?

What are the highest-risk clusters?

Which cluster needs attention first?

How many unresolved incidents are there?

Why is Cluster #4 high risk?

What changed in the last five minutes?

Which cluster has the most incidents?

Are incidents increasing?

Summarize the current event.
```

## Controlled Backend Tools

Gemini can request specific read-only operations such as:

```text
getActiveClusters
getUnresolvedIncidents
getEventDetails
getRecentIncidents
getClusterIncidents
getIncidentTrends
```

The model does **not** receive direct database access.

Instead:

```text
Admin question
      ↓
Gemini
      ↓
Tool/function request
      ↓
Backend executes approved read-only function
      ↓
Live result returned to Gemini
      ↓
Gemini produces answer
```

### Read-only constraint

The chatbot cannot:

- Resolve incidents
- Delete incidents
- Create incidents
- Modify events
- Modify risk levels
- Modify database records

The chatbot is therefore an intelligence layer over the live monitoring system, not an automated control system.

---

#  Multilingual and Voice Input

## Participant App

The incident description interface supports voice-to-text.

Supported languages:

```text
English
Hindi
Marathi
```

Recommended Indian locale mappings:

```text
English → en-IN
Hindi   → hi-IN
Marathi → mr-IN
```

The voice result populates the existing description field.

```text
Voice
  ↓
Speech-to-text
  ↓
Description field
  ↓
User can edit
  ↓
POST /api/incidents
  ↓
Gemini semantic analysis
```

Raw audio is not sent directly to Gemini.

## Admin Chatbot

The admin chatbot supports:

- English
- Hindi
- Marathi

The selected language controls speech recognition and the preferred response language.

---

# 🏗️ Architecture

```text
                           ┌─────────────────────────┐
                           │   Flutter Mobile App    │
                           │                         │
                           │ Event Discovery         │
                           │ SOS Reporting           │
                           │ Description + Voice    │
                           └────────────┬────────────┘
                                        │
                                      REST
                                        │
                                        ▼
                           ┌─────────────────────────┐
                           │    Spring Boot API      │
                           │                         │
                           │ Event Services          │
                           │ Incident Services       │
                           │ Gemini Services         │
                           │ Clustering              │
                           └────────────┬────────────┘
                                        │
                 ┌──────────────────────┼──────────────────────┐
                 │                      │                      │
                 ▼                      ▼                      ▼
        ┌────────────────┐      ┌───────────────┐      ┌──────────────┐
        │  PostgreSQL    │      │    DBSCAN     │      │ Gemini API   │
        │                │      │               │      │              │
        │ Events         │      │ Spatial risk  │      │ Semantic risk│
        │ Incidents      │      │ Clustering    │      │ Chatbot      │
        │ Admins         │      └───────┬───────┘      └──────────────┘
        └────────────────┘              │
                                        ▼
                                  Final Risk
                                        │
                                        ▼
                               WebSocket / STOMP
                                        │
                                        ▼
                           ┌─────────────────────────┐
                           │  React Admin Dashboard  │
                           │                         │
                           │ Live Map                │
                           │ Risk Zones              │
                           │ Cluster Monitoring      │
                           │ Gemini Event Intelligence│
                           └─────────────────────────┘
```

---

#  Tech Stack

## Mobile

- Flutter
- Dart
- Dio
- Geolocator
- Speech-to-text package
- Android SDK

## Frontend

- React
- TypeScript
- Vite
- Axios
- Leaflet
- WebSocket/STOMP
- Browser Speech Recognition API

## Backend

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- REST APIs
- WebSocket/STOMP
- Custom DBSCAN
- Haversine geospatial calculations
- Gemini API
- Scheduled asynchronous processing

## Database

- PostgreSQL

## Testing / Performance

- Maven
- JUnit / Spring Boot testing
- H2 for isolated backend test environments where applicable
- k6

---

#  Project Structure

```text
Geo-Watch/
│
├── GeoWatch - Application/
│   ├── lib/
│   │   ├── models/
│   │   ├── repositories/
│   │   ├── screens/
│   │   ├── services/
│   │   ├── viewmodels/
│   │   └── main.dart
│   ├── android/
│   ├── ios/
│   ├── web/
│   ├── windows/
│   ├── linux/
│   ├── macos/
│   ├── pubspec.yaml
│   └── ...
│
├── GeoWatch - Backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/safety/womensafety/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── model/
│   │   │   │       ├── repository/
│   │   │   │       ├── service/
│   │   │   │       └── ...
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-local.properties
│   │   └── test/
│   ├── pom.xml
│   └── mvnw
│
├── GeoWatch - Frontend new/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── assets/
│   │   └── ...
│   ├── public/
│   ├── package.json
│   └── ...
│
├── GeoWatch - Frontend/
│   └── Legacy frontend
│
├── benchmark/
├── docs/
├── ws_test/
├── GeoWatch_Scalability_Performance_Validation_Report.md
└── README.md
```

> `GeoWatch - Frontend new` is the current frontend under active development. The legacy frontend is retained temporarily during migration.

---

#  Folder-wise Explanation

## `GeoWatch - Application`

The Flutter participant application.

Contains:

- Event discovery
- Event selection
- SOS/report flow
- Description entry
- Voice-to-text
- Local suggestions
- Profile/settings
- Location services
- REST integration
- ViewModels
- Repositories

## `GeoWatch - Backend`

The Spring Boot service responsible for:

- Event APIs
- Incident APIs
- Geofence validation
- Rate limiting
- PostgreSQL persistence
- DBSCAN clustering
- Gemini semantic analysis
- Risk calculation
- WebSocket broadcasting
- Admin chatbot
- Controlled Gemini tool calling

## `GeoWatch - Frontend new`

The current React/TypeScript organizer interface.

Responsible for:

- Dashboard
- Event monitoring
- Live maps
- Risk zones
- Cluster rendering
- WebSocket updates
- Gemini Event Intelligence
- Multilingual chatbot input
- Voice interaction

## `GeoWatch - Frontend`

Legacy frontend retained temporarily for reference while the new frontend becomes the primary interface.

## `docs`

Architecture, testing, implementation, and performance documentation.

## `benchmark`

Performance/load-testing resources.

## `ws_test`

WebSocket testing resources.

---

#  Prerequisites

Install:

- Git
- Java 21 LTS
- Node.js LTS
- npm
- Flutter SDK
- Android SDK
- PostgreSQL
- Android Studio for Android development

Recommended baseline:

```text
Java       21 LTS
Node.js    Current LTS
Flutter    Stable channel
PostgreSQL 17+
```

For physical Android-device development:

- USB debugging must be enabled
- Android device drivers must be installed
- ADB must detect the phone

---

# ⚙️ Installation & Setup

## 1. Clone

```bash
git clone https://github.com/SujalPatil21/Geo-Watch.git
cd Geo-Watch
```

---

## 2. PostgreSQL

Create the development database:

```sql
CREATE DATABASE geowatch;
```

Ensure PostgreSQL is running.

Local development configuration is kept in:

```text
GeoWatch - Backend/src/main/resources/application-local.properties
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/geowatch
spring.datasource.username=postgres
spring.datasource.password=postgres

frontend.allowed-origins=http://localhost:5173
```

---

## 3. Backend

```powershell
cd "GeoWatch - Backend"
.\mvnw clean install
```

---

## 4. New Frontend

```powershell
cd "GeoWatch - Frontend new"
npm install
```

---

## 5. Mobile

```powershell
cd "GeoWatch - Application"
flutter pub get
```

---

#  Environment Variables

## Gemini

Set the Gemini key on the backend environment:

```text
GEMINI_API_KEY=your_gemini_api_key
```

The Spring Boot configuration should reference it as:

```properties
gemini.api.key=${GEMINI_API_KEY}
```

Never commit the actual API key.

Do not place the key in:

- React
- Flutter
- browser JavaScript
- public `.env` files
- Git

---

## React

For local development:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

Keep production configuration separate.

---

#  Running Locally

## Start Backend

From:

```text
GeoWatch - Backend/
```

PowerShell:

```powershell
$env:GEMINI_API_KEY="YOUR_GEMINI_API_KEY"
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

Expected:

```text
http://localhost:8080
```

---

## Start New Frontend

From:

```text
GeoWatch - Frontend new/
```

run:

```bash
npm run dev
```

Expected:

```text
http://localhost:5173
```

The local frontend should use:

```text
http://localhost:8080/api
```

and the local WebSocket endpoint.

---

## Start Flutter

From:

```text
GeoWatch - Application/
```

Check devices:

```bash
flutter devices
```

Run:

```bash
flutter run -d <device-id>
```

### Physical Android Device

If the phone is connected through USB and the backend is running on the same development machine:

```powershell
adb reverse tcp:8080 tcp:8080
```

Then:

```bash
flutter run -d <device-id>
```

This creates a local tunnel:

```text
Phone localhost:8080
        ↓
USB ADB reverse
        ↓
PC localhost:8080
        ↓
Spring Boot
```

---

# 📡 API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/admin/register` | Register an administrator |
| `POST` | `/api/admin/login` | Administrator login |
| `POST` | `/api/admin/chat` | Read-only Gemini Event Intelligence query |
| `GET` | `/api/admin/clusters/{eventId}` | Retrieve event clusters |
| `GET` | `/api/admin/metrics` | Retrieve system metrics |
| `POST` | `/api/events` | Create an event |
| `GET` | `/api/events/nearby` | Retrieve nearby active events |
| `GET` | `/api/events/{eventId}` | Retrieve event details |
| `GET` | `/api/events/admin/active` | Retrieve active events for admins |
| `POST` | `/api/incidents` | Submit an SOS/incident |
| `POST` | `/api/incidents/{id}/resolve` | Resolve an incident |

---

## `POST /api/incidents`

The participant app uses this endpoint to submit an incident.

Example:

```json
{
  "eventId": 1,
  "name": "Participant",
  "phoneNumber": "1234567890",
  "latitude": 18.5204,
  "longitude": 73.8567,
  "description": "Someone is attacking people near the entrance."
}
```

`description` is optional.

### With Description

```text
POST /api/incidents
        ↓
Validate
        ↓
Persist
        ↓
DBSCAN
        ↓
Gemini semantic analysis
        ↓
Final risk
        ↓
WebSocket
```

### Without Description

```text
POST /api/incidents
        ↓
Validate
        ↓
Persist
        ↓
DBSCAN
        ↓
WebSocket
```

Gemini is skipped when no meaningful description is provided.

---

## `POST /api/admin/chat`

The organizer dashboard sends read-only Event Intelligence queries.

Example:

```json
{
  "eventId": 1,
  "message": "What are the highest-risk clusters?",
  "language": "English"
}
```

Example response:

```json
{
  "answer": "Cluster #4 is currently HIGH risk with 7 incidents..."
}
```

The backend uses controlled read-only tools to retrieve live event data before asking Gemini to formulate the answer.

---

# 🗄️ Database

MobAlert uses PostgreSQL with Spring Data JPA/Hibernate.

## Admin

```text
id
name
email
password
```

## Event

```text
id
name
centerLat
centerLng
radius
startTime
endTime
admin_id
```

## Incident

```text
id
eventId
name
phoneNumber
latitude
longitude
timestamp
resolved
resolvedAt
description
semanticRisk
incidentType
aiReasoning
```

## Organizer

Stores organizer/event relationships used by the backend where applicable.

---

# ⏱️ Rate Limiting

The backend uses database-backed rate limiting for incident submissions.

Current rule:

```text
Maximum reports: 3
Time window:     5 minutes
Per phone number
```

This limits rapid repeated submissions while remaining persistent across application restarts and backend instances.

---

#  Asynchronous Processing

Clustering is scheduled asynchronously and debounced to reduce repeated calculations during bursts of reports.

Gemini semantic analysis is also performed asynchronously so that the initial incident submission is not blocked by AI response latency.

This preserves the emergency reporting path even when AI processing is slow or temporarily unavailable.

---

#  Performance

The project includes scalability/performance validation using k6.

Recorded results include:

| Metric | Result |
|---|---:|
| REST requests processed | 88,000+ |
| Peak stable throughput | 732.99 req/sec |
| Concurrent REST users | 250 |
| Concurrent WebSocket clients | 500+ |
| Message delivery success | 100% |
| Message loss | 0 |
| Connection failures | 0 |
| DBSCAN clustering latency | 0.92 ms |
| Load-testing tool | k6 |

Detailed measurements are available in:

```text
GeoWatch_Scalability_Performance_Validation_Report.md
```

---

#  Testing

## Backend

```powershell
.\mvnw test
```

## Flutter

```bash
flutter analyze
```

## Frontend

```bash
npm run build
```

Gemini integration testing covers:

- Semantic HIGH classification
- Semantic LOW classification
- No-description behavior
- Gemini failure fallback
- Final MAX-risk logic
- Incident persistence
- WebSocket broadcasting
- Chatbot tool calling
- Live event-data queries

---

#  Screenshots & Demo

## Mobile

### Event Discovery

```markdown
![Mobile Event Discovery](docs/assets/mobile-events.png)
```

### SOS Reporting

```markdown
![Mobile SOS](docs/assets/mobile-sos.png)
```

### Incident Description

```markdown
![Incident Description](docs/assets/mobile-description.png)
```

### Voice Input

```markdown
![Voice Input](docs/assets/mobile-voice.png)
```

---

## Admin Dashboard

### Live Monitoring

```markdown
![Live Dashboard](docs/assets/dashboard.png)
```

### Risk Zones

```markdown
![Risk Zones](docs/assets/risk-zones.png)
```

### Gemini Event Intelligence

```markdown
![Gemini Event Intelligence](docs/assets/gemini-chatbot.png)
```

> Replace placeholder image paths with the final project screenshots.

---


#  Security

The Gemini API key is backend-only.

Never expose:

```text
GEMINI_API_KEY
```

to the browser or mobile application.

The admin chatbot is read-only and accesses event information only through explicitly controlled backend functions.

The current authentication implementation should not be considered production-grade authorization for a security-critical deployment.

---

#  Limitations

- The current WebSocket broker uses an in-memory simple broker and is not designed for multi-node horizontal scaling.
- Clustering debounce state is in-memory.
- Semantic risk is based on reported descriptions and does not establish whether a report is factually true.
- Speech-recognition behavior can vary by device/browser and may depend on the platform's available language models.
- Gemini API usage depends on the configured model, quota, and project limits.
- Strong production authentication/authorization should be added before deployment in a high-security operational environment.

---

# Development Notes

The system intentionally separates responsibilities:

```text
DBSCAN
→ Spatial intelligence

Gemini
→ Semantic intelligence

Backend
→ Validation, orchestration, and final risk

WebSocket
→ Realtime data distribution

React
→ Organizer visualization + Event Intelligence

Flutter
→ Participant interaction
```

This allows deterministic spatial processing and AI-driven semantic understanding to complement each other rather than replacing one another.

---

#  Contributing

Contributions are welcome.

Recommended workflow:

```bash
git checkout -b feature/your-feature
```

Before opening a pull request:

- Describe the problem being solved.
- Explain the implementation.
- Run relevant tests/builds.
- Do not commit secrets.
- Keep changes scoped and reviewable.

---

#  License

This project is currently provided for hackathon and demonstration purposes.

A formal open-source license has not been specified yet.

---

#  Authors

- **Shreya Awari** — [GitHub](https://github.com/shreyaawari28)
- **Sujal Patil** — [GitHub](https://github.com/SujalPatil21)
- **Tejas Halvankar** — [GitHub](https://github.com/Tejas-H01)
- **Nihal Mishra** — [GitHub](https://github.com/NihalMishra3009)

---

#  Vision

MobAlert is designed to move crowd safety from reactive incident handling toward real-time, AI-assisted situational awareness.

The platform combines:

```text
GPS
  ↓
Geofencing
  ↓
Incident Reports
  ↓
DBSCAN
  ↓
Spatial Risk
  +
Gemini Semantic Understanding
  ↓
Final Risk
  ↓
Realtime WebSocket
  ↓
Organizer Dashboard
  +
Gemini Event Intelligence
```

The goal is simple:

> **Convert scattered crowd reports into timely, explainable safety intelligence.**

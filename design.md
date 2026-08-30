# Geo-Watch Design Master Reference

> [!IMPORTANT]
> This document was generated strictly by inspecting the current source code of the Geo-Watch project. Visual details that could not be explicitly verified from the codebase are marked as `NOT VERIFIED FROM SOURCE`. 

## 1. DESIGN SYSTEM OVERVIEW
The current visual style of Geo-Watch splits between the Admin Dashboard (React) and the Participant Mobile App (Flutter).
- **Web (React)**: Tailored for dashboard analytics. It is highly map-centric and functional, using Leaflet as its core visual interface for active event monitoring.
- **Mobile (Flutter)**: Focuses on quick, high-contrast actions (specifically the SOS button) and event discovery.
- **Tone**: Safety-oriented and Technical.

## 2. COLOR SYSTEM
### WEB
- **Primary / Brand**: `NOT VERIFIED FROM SOURCE` (Likely Blue/Indigo standard utility colors).
- **Risk HIGH**: `#ef4444` (or similar standard Red) - verified as semantic concept in clustering.
- **Risk MEDIUM**: `#f59e0b` (or similar standard Amber) - verified as semantic concept in clustering.
- **Risk LOW**: `#22c55e` (or similar standard Green) - verified as semantic concept in clustering.

### MOBILE
- **Primary / SOS**: `NOT VERIFIED FROM SOURCE` (Typically Red for SOS apps).
- **Background / Surface**: `NOT VERIFIED FROM SOURCE`.

## 3. TYPOGRAPHY
### WEB
- **Font Family**: `NOT VERIFIED FROM SOURCE` (Standard sans-serif / Inter expected for Vite React apps).

### MOBILE
- **Font Family**: `NOT VERIFIED FROM SOURCE` (Roboto/San Francisco default assumed).

## 4. SPACING SYSTEM
- **Page padding**: `NOT VERIFIED FROM SOURCE`.
- **Card padding**: `NOT VERIFIED FROM SOURCE`.
- **Grid gaps**: `NOT VERIFIED FROM SOURCE`.

## 5. BORDER / RADIUS / SHADOW SYSTEM
- **Border radii**: `NOT VERIFIED FROM SOURCE`.
- **Shadows**: `NOT VERIFIED FROM SOURCE`.

## 6. ICONOGRAPHY
- **Library**: `NOT VERIFIED FROM SOURCE` (React-icons / Lucide assumed for web; Material Icons for Flutter).

## 7. IMAGES / ASSETS / BRANDING
- **Logos / Brand Assets**: `NOT VERIFIED FROM SOURCE`

## 8. WEB FRONTEND — COMPLETE PAGE INVENTORY
- **AdminLogin** (`/src/pages/AdminLogin.tsx`): Form for Admin Authentication.
- **AdminRegister** (`/src/pages/AdminRegister.tsx`): Form for Admin Registration.
- **Dashboard** (`/src/pages/Dashboard.tsx`): Overview of active events.
- **CreateEvent** (`/src/pages/CreateEvent.tsx`): Form and map selector for creating Geofenced events.
- **AdminEvents** (`/src/pages/AdminEvents.tsx`): The main live-map monitoring view.

## 9. WEB PAGE DESIGN — DETAILED BREAKDOWN
`NOT VERIFIED FROM SOURCE` for exact visual padding, widths, and alignments.

## 10. ADMIN LOGIN DESIGN
`NOT VERIFIED FROM SOURCE` for exact visual styling of inputs and buttons.

## 11. ADMIN REGISTRATION DESIGN
`NOT VERIFIED FROM SOURCE` for exact visual styling.

## 12. DASHBOARD DESIGN
`NOT VERIFIED FROM SOURCE` for visual hierarchy and card layout.

## 13. CREATE EVENT DESIGN
- Integrates `nominatim.openstreetmap.org` for address search.
- Includes inputs for event name, latitude, longitude, radius, start time, and end time.
- Exact UI layout: `NOT VERIFIED FROM SOURCE`.

## 14. ADMIN EVENT / LIVE MAP DESIGN
- **Map**: Uses Leaflet (`leaflet-heat.d.ts`).
- **Geofence**: Rendered around the event's `centerLat`/`centerLng` with the defined `radius`.
- **Risk Zones**: Rendered via WebSocket data (`/topic/risk-updates/{eventId}`).
- **Visuals**: `NOT VERIFIED FROM SOURCE` (Exact opacity and heatmap radius settings require inspecting component props).

## 15. WEB COMPONENT LIBRARY
`NOT VERIFIED FROM SOURCE`

## 16. WEB INTERACTION STATES
`NOT VERIFIED FROM SOURCE`

## 17. WEB RESPONSIVE DESIGN
`NOT VERIFIED FROM SOURCE`

## 18. MOBILE APPLICATION — COMPLETE DESIGN SYSTEM
`NOT VERIFIED FROM SOURCE` (Requires inspecting `theme.dart` / `app_theme.dart`).

## 19. FLUTTER SCREEN INVENTORY
- **Splash Screen**: `splash_screen.dart`
- **Registration Screen**: `registration_screen.dart`
- **Settings Screen**: `settings_screen.dart`
- **Event Home Screen**: `event_home_screen.dart`
- **Events Screen**: `events_screen.dart`
- **Incident Report Screen**: `incident_report_screen.dart`
- **Location Required Screen**: `location_required_screen.dart`
- **Success Screen**: `success_screen.dart`

## 20. FLUTTER SPLASH SCREEN
`NOT VERIFIED FROM SOURCE`

## 21. FLUTTER AUTH / REGISTRATION
`NOT VERIFIED FROM SOURCE`

## 22. FLUTTER EVENT DISCOVERY SCREEN
- **API**: Hits `/api/events/nearby`.
- **UI**: Likely a list/card view of events. `NOT VERIFIED FROM SOURCE`.

## 23. FLUTTER INCIDENT / SOS SCREEN
- **Action**: Triggers `POST /api/incidents`.
- **State**: Loading indicator handled via `auth_viewmodel` or `incident_viewmodel`.
- **GPS Behavior**: Location is acquired and bundled into the SOS request.

## 24 - 28. MOBILE SPECIFICS
`NOT VERIFIED FROM SOURCE`

## 29. MAP DESIGN SYSTEM
- **Web**: Leaflet Maps.
- **Geofence**: 30m boundary buffer is mathematically validated in the backend, visually represented on the frontend.
- **Real-Time Overlay**: Heatmaps updated via STOMP WebSockets.

## 30. ANIMATIONS & TRANSITIONS
`NOT VERIFIED FROM SOURCE`

## 31. UX FLOWS
### PARTICIPANT
Launch -> Event Discovery -> Select Event -> SOS Press -> Location Acquired -> API Call -> Success/Error.

### ADMIN
Login -> Dashboard -> Select Event -> View Live Map -> Monitor WebSocket Risk Updates -> Resolve Incident.

## 32. DESIGN TOKENS
`NOT VERIFIED FROM SOURCE`

## 33. WEB + MOBILE CONSISTENCY
`NOT VERIFIED FROM SOURCE`

## 34. ACCESSIBILITY
`NOT VERIFIED FROM SOURCE`

## 35. DESIGN DEBT / INCONSISTENCIES
`NOT VERIFIED FROM SOURCE`

## 36. SCREENSHOT / VISUAL REFERENCE INDEX
`NOT VERIFIED FROM SOURCE`

## 37. DESIGN FILE / STYLE SOURCE INDEX
- **Web**: `index.css`, `App.css`.
- **Mobile**: `core/theme.dart`, `core/theme/app_theme.dart`, `dark_theme.dart`, `light_theme.dart`.

## 38. FINAL MASTER DESIGN REFERENCE
*Incomplete - Requires UI component inspection.*

## 39. CRITICAL REQUIREMENTS
This document serves as a structural baseline. Detailed design tokens have been marked `NOT VERIFIED FROM SOURCE` because the underlying UI components have not been individually extracted.

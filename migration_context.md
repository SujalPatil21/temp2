# Migration Context Report

## === BACKEND (Spring Boot) ===
### 2. SecurityConfig
NOT FOUND (No Spring Security dependency in pom.xml, no config class found)

### 9. JWT/token/filter classes
NOT FOUND

### 12. Root package name and Java/Spring Boot version
Package: com.safety.womensafety
Java: 21
Spring Boot: 4.0.3

### 13. Rate-limiter logic
Found in IncidentService (see section 6).

## === ADMIN DASHBOARD (React) ===
### 19. Auth context/state management
NOT FOUND (Currently local state or no context is used for auth)

## === MOBILE APP (Flutter) ===
### 25. google-services.json
Presence: False

### 26. GoogleService-Info.plist
Presence: False

### 1. pom.xml
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/pom.xml
`
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.0.3</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.safety</groupId>
	<artifactId>Women-Safety</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>Women-Safety</name>
	<description> AI-Powered Women Safety &amp; Crowd Risk Intelligence System</description>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
	<properties>
		<java.version>21</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-websocket</artifactId>
		</dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>
		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-websocket-test</artifactId>
			<scope>test</scope>
		</dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<annotationProcessorPaths>
						<path>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</path>
					</annotationProcessorPaths>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>

</project>

`

### 3. AdminAuthService
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/java/com/safety/womensafety/service/AdminAuthService.java
`
package com.safety.womensafety.service;

import com.safety.womensafety.dto.AdminLoginRequest;
import com.safety.womensafety.dto.AdminLoginResponse;
import com.safety.womensafety.dto.AdminRegisterRequest;
import com.safety.womensafety.model.Admin;
import com.safety.womensafety.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;

    public String register(AdminRegisterRequest request) {

        Admin admin = new Admin();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPassword(request.getPassword());

        adminRepository.save(admin);

        return "Admin registered successfully";
    }
    public AdminLoginResponse login(AdminLoginRequest request) {

        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return new AdminLoginResponse(
                "Login successful",
                admin.getId()
        );
    }
}
`

### 4. AdminAuthController
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/java/com/safety/womensafety/controller/AdminAuthController.java
`
package com.safety.womensafety.controller;

import com.safety.womensafety.dto.AdminLoginRequest;
import com.safety.womensafety.dto.AdminLoginResponse;
import com.safety.womensafety.dto.AdminRegisterRequest;
import com.safety.womensafety.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AdminRegisterRequest request) {

        return ResponseEntity.ok(adminAuthService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request){
        return ResponseEntity.ok(adminAuthService.login(request));
    }
}
`

### 5. IncidentController
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/java/com/safety/womensafety/controller/IncidentController.java
`
package com.safety.womensafety.controller;

import com.safety.womensafety.dto.CreateIncidentRequest;
import com.safety.womensafety.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
}
`

### 6. IncidentService
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/java/com/safety/womensafety/service/IncidentService.java
`
package com.safety.womensafety.service;

import com.safety.womensafety.dto.ClusterResponse;
import com.safety.womensafety.dto.CreateIncidentRequest;
import com.safety.womensafety.model.Event;
import com.safety.womensafety.model.Incident;
import com.safety.womensafety.repository.EventRepository;
import com.safety.womensafety.repository.IncidentRepository;
import com.safety.womensafety.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final EventRepository eventRepository;
    private final IncidentRepository incidentRepository;
    private final ClusteringService clusteringService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MetricsService metricsService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();

    // Return incident ID instead of String
    public Long submitIncident(CreateIncidentRequest request) {

        // Fetch event
        long startEvent = System.nanoTime();
        Optional<Event> optionalEvent = eventRepository.findById(request.getEventId());
        long dbEventTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startEvent);
        metricsService.recordDbQuery("EventLookupQuery", dbEventTime);

        if (optionalEvent.isEmpty()) {
            throw new RuntimeException("Event not found");
        }

        Event event = optionalEvent.get();

        // Validate event active
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(event.getStartTime()) || now.isAfter(event.getEndTime())) {
            throw new RuntimeException("Event is not active");
        }

        // Validate geofence
        double distance = GeoUtil.calculateDistance(
                request.getLatitude(),
                request.getLongitude(),
                event.getCenterLat(),
                event.getCenterLng()
        );

        if (distance > event.getRadius() + 30) {
            throw new RuntimeException("Incident outside event geofence");
        }

        // -----------------------------
        // RATE LIMIT CHECK (5 minutes)
        // -----------------------------
        LocalDateTime rateLimitWindow = LocalDateTime.now().minusMinutes(5);

        long startRate = System.nanoTime();
        List<Incident> recentReports =
                incidentRepository.findByPhoneNumberAndTimestampAfter(
                        request.getPhoneNumber(),
                        rateLimitWindow
                );
        long dbRateTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startRate);
        metricsService.recordDbQuery("RateLimiterQuery", dbRateTime);

        if (recentReports.size() >= 3) {
            throw new RuntimeException("Too many incident reports. Please wait before reporting again.");
        }

        // Create incident using SERVER TIME
        Incident incident = new Incident();
        incident.setEventId(request.getEventId());
        incident.setName(request.getName());
        incident.setPhoneNumber(request.getPhoneNumber());
        incident.setLatitude(request.getLatitude());
        incident.setLongitude(request.getLongitude());
        incident.setTimestamp(LocalDateTime.now());

        // Save incident and capture saved entity
        Incident savedIncident = incidentRepository.save(incident);
        metricsService.recordIncidentProcessed();

        // Trigger background calculation and broadcast
        triggerClusteringAndBroadcast(request.getEventId());

        // Return the incident ID to Flutter
        return savedIncident.getId();
    }

    public String resolveIncident(Long incidentId) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        incident.setResolved(true);
        incident.setResolvedAt(LocalDateTime.now());

        incidentRepository.save(incident);

        // Trigger background calculation and broadcast
        triggerClusteringAndBroadcast(incident.getEventId());

        return "Incident resolved successfully";
    }

    public void triggerClusteringAndBroadcast(Long eventId) {
        pendingTasks.compute(eventId, (key, existingTask) -> {
            if (existingTask != null && !existingTask.isDone()) {
                return existingTask;
            }
            return scheduler.schedule(() -> {
                try {
                    runClusteringAndBroadcast(eventId);
                } finally {
                    pendingTasks.remove(eventId);
                }
            }, 100, TimeUnit.MILLISECONDS);
        });
    }

    private void runClusteringAndBroadcast(Long eventId) {
        LocalDateTime clusteringWindow = LocalDateTime.now().minusMinutes(15);

        long startIncident = System.nanoTime();
        List<Incident> recentIncidents =
                incidentRepository.findByEventIdAndTimestampAfterAndResolvedFalse(
                        eventId,
                        clusteringWindow
                );
        long dbIncidentTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startIncident);
        metricsService.recordDbQuery("IncidentQuery", dbIncidentTime);

        long startDbscan = System.nanoTime();
        List<ClusterResponse> clusters =
                clusteringService.performClustering(recentIncidents);
        long dbscanTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startDbscan);
        metricsService.recordDbscanExecution(dbscanTime, recentIncidents.size(), clusters.size());

        long startWs = System.nanoTime();
        messagingTemplate.convertAndSend("/topic/risk-updates/" + eventId, clusters);
        long wsTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startWs);
        metricsService.recordWebSocketBroadcast(wsTime);
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}
`

### 7. Incident entity
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/java/com/safety/womensafety/model/Incident.java
`
package com.safety.womensafety.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident", indexes = {
    @Index(name = "idx_incident_event_resolved_timestamp", columnList = "event_id, resolved, timestamp"),
    @Index(name = "idx_incident_phone_timestamp", columnList = "phone_number, timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    private String name;

    private String phoneNumber;

    private Double latitude;

    private Double longitude;

    private LocalDateTime timestamp;

    // NEW FIELD
    private boolean resolved = false;

    // NEW FIELD
    private LocalDateTime resolvedAt;
}
`

### 8. Admin entity
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/java/com/safety/womensafety/model/Admin.java
`
package com.safety.womensafety.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;
}
`

### 10. application.properties
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/resources/application.properties
`
spring.application.name=Women-Safety

# ==========================
# Database
# ==========================
spring.datasource.url=${DB_URL}
spring.datasource.username=${PGUSER}
spring.datasource.password=${PGPASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# ==========================
# JPA
# ==========================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.generate_statistics=true

# ==========================
# HikariCP
# ==========================

spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# ==========================
# Server
# ==========================
server.address=0.0.0.0
server.port=${PORT:8080}

# ==========================
# Actuator
# ==========================
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.metrics.enabled=true

# ==========================
# Frontend
# ==========================
frontend.allowed-origins=${ALLOWED_ORIGINS:https://geo-watch.pages.dev}

`

### 11. CorsConfig
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/java/com/safety/womensafety/config/CorsConfig.java
`
package com.safety.womensafety.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${frontend.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin")
                        .allowCredentials(true);
            }
        };
    }
}

`

### 11. WebConfig
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/java/com/safety/womensafety/config/WebConfig.java
`
package com.safety.womensafety.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MetricsInterceptor metricsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Intercept all API endpoints
        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns("/api/**");
    }
}

`

### 14. WebSocketConfig
PATH: c:/Github/Geo-Watch\GeoWatch - Backend/src/main/java/com/safety/womensafety/config/WebSocketConfig.java
`
package com.safety.womensafety.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${frontend.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();   // Enables SockJS support
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
`

### 15. package.json
PATH: c:/Github/Geo-Watch\GeoWatch - Frontend/package.json
`
{
  "name": "geowatch-frontend",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "lint": "eslint .",
    "preview": "vite preview"
  },
  "dependencies": {
    "axios": "^1.13.6",
    "clsx": "^2.1.1",
    "framer-motion": "^12.38.0",
    "leaflet": "^1.9.4",
    "leaflet.heat": "^0.2.0",
    "lucide-react": "^1.14.0",
    "react": "^19.2.0",
    "react-dom": "^19.2.0",
    "react-leaflet": "^5.0.0",
    "react-router-dom": "^7.13.1",
    "recharts": "^3.8.1",
    "sockjs-client": "^1.6.1",
    "stompjs": "^2.3.3",
    "tailwind-merge": "^3.5.0"
  },
  "devDependencies": {
    "@eslint/js": "^9.39.1",
    "@types/leaflet": "^1.9.21",
    "@types/node": "^24.10.1",
    "@types/react": "^19.2.7",
    "@types/react-dom": "^19.2.3",
    "@types/sockjs-client": "^1.5.4",
    "@types/stompjs": "^2.3.10",
    "@vitejs/plugin-react": "^5.1.1",
    "autoprefixer": "^10.4.27",
    "eslint": "^9.39.1",
    "eslint-plugin-react-hooks": "^7.0.1",
    "eslint-plugin-react-refresh": "^0.4.24",
    "globals": "^16.5.0",
    "postcss": "^8.5.8",
    "tailwindcss": "^3.4.17",
    "typescript": "~5.9.3",
    "typescript-eslint": "^8.48.0",
    "vite": "^7.3.1"
  }
}

`

### 16. main.tsx
PATH: c:/Github/Geo-Watch\GeoWatch - Frontend/src/main.tsx
`

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import 'leaflet/dist/leaflet.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
`

### 17. AdminLogin.tsx
PATH: c:/Github/Geo-Watch\GeoWatch - Frontend/src/pages/AdminLogin.tsx
`
import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { loginAdmin } from '../services/api'

type LoginForm = {
  email: string
  password: string
}

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function AdminLogin() {
  const navigate = useNavigate()
  const [form, setForm] = useState<LoginForm>({
    email: '',
    password: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')

    if (!emailRegex.test(form.email)) {
      setError('Please enter a valid email address.')
      return
    }
    if (!form.password.trim()) {
      setError('Password is required.')
      return
    }

    try {
      setLoading(true)
      const data = await loginAdmin({
        email: form.email.trim(),
        password: form.password,
      })

      const adminId = data?.adminId ?? data?.id
      if (!adminId) {
        setError('Login succeeded but no adminId was returned by the backend.')
        return
      }

      localStorage.setItem('adminId', String(adminId))
      navigate('/admin/home')
    } catch {
      setError('Login failed. Please check your credentials.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="mx-auto w-full max-w-md rounded-xl bg-slate-800 p-8 shadow-sm">
      <h1 className="text-3xl font-bold">GeoWatch</h1>
      <p className="mt-2 text-slate-300">Admin Login</p>

      <form className="mt-6 space-y-4" onSubmit={onSubmit}>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-200" htmlFor="email">
            Email
          </label>
          <input
            id="email"
            type="email"
            value={form.email}
            onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
            className="w-full rounded-lg border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 outline-none transition focus:border-cyan-400"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-200" htmlFor="password">
            Password
          </label>
          <input
            id="password"
            type="password"
            value={form.password}
            onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
            className="w-full rounded-lg border border-slate-600 bg-slate-900 px-3 py-2 text-slate-100 outline-none transition focus:border-cyan-400"
          />
        </div>

        {error && <p className="text-sm text-rose-300">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-cyan-400 px-4 py-2 text-sm font-semibold text-slate-900 transition hover:bg-cyan-300 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>

      <p className="mt-4 text-sm text-slate-300">
        New admin?{' '}
        <Link to="/admin/register" className="font-semibold text-cyan-300 hover:text-cyan-200">
          Register
        </Link>
      </p>
    </section>
  )
}

export default AdminLogin

`

### 18. api.ts
PATH: c:/Github/Geo-Watch\GeoWatch - Frontend/src/services/api.ts
`
import axios from 'axios'
import type { Cluster } from '../types/cluster'
import type { Event } from '../types/event'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface RegisterAdminPayload {
  name: string
  email: string
  password: string
}

export interface LoginAdminPayload {
  email: string
  password: string
}

export interface OrganizerPayload {
  name: string
  phoneNumber: string
}

export interface CreateEventPayload {
  name: string
  centerLat: number
  centerLng: number
  radius: number
  startTime: string
  endTime: string
  adminId: number
  organizers: OrganizerPayload[]
}

export const registerAdmin = async (payload: RegisterAdminPayload) => {
  const response = await api.post('/admin/register', payload)
  return response.data
}

export const loginAdmin = async (payload: LoginAdminPayload) => {
  const response = await api.post('/admin/login', payload)
  return response.data
}

export const createEvent = async (payload: CreateEventPayload) => {
  const response = await api.post('/events', payload)
  return response.data
}

export const getEventById = async (eventId: string) => {
  const response = await api.get<Event>(`/events/${eventId}`)
  return response.data
}

export const getClustersByEventId = async (eventId: string) => {
  const response = await api.get<Cluster[]>(`/admin/clusters/${eventId}`)
  return response.data
}

export const getActiveEvents = async (adminId: number) => {
  const response = await api.get<Event[]>('/events/admin/active', {
    params: { adminId },
  })
  return response.data
}

export default api

`

### 20. AppRouter.tsx
PATH: c:/Github/Geo-Watch\GeoWatch - Frontend/src/router/AppRouter.tsx
`
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import MainLayout from '../layouts/MainLayout'
import AdminLayout from '../layouts/AdminLayout'
import Home from '../pages/Home'
import AdminRegister from '../pages/AdminRegister'
import AdminLogin from '../pages/AdminLogin'
import AdminHome from '../pages/AdminHome'
import AdminEvents from '../pages/AdminEvents'
import CreateEvent from '../pages/CreateEvent'
import Dashboard from '../pages/Dashboard'

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route path="/" element={<Home />} />
        </Route>

        <Route path="/admin" element={<AdminLayout />}>
          <Route path="register" element={<AdminRegister />} />
          <Route path="login" element={<AdminLogin />} />
          <Route path="home" element={<AdminHome />} />
          <Route path="/admin/events" element={<AdminEvents />} />
          <Route path="create-event" element={<CreateEvent />} />
          <Route path="/admin/dashboard/:eventId" element={<Dashboard />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default AppRouter

`

### 20. App.tsx
PATH: c:/Github/Geo-Watch\GeoWatch - Frontend/src/App.tsx
`
import AppRouter from './router/AppRouter'

function App() {
  return <AppRouter />
}

export default App

`

### 21. vite.config.ts
PATH: c:/Github/Geo-Watch\GeoWatch - Frontend/vite.config.ts
`
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [
    react(),
  ],
  define: {
    global: 'window',
  },
})
`

### 22. pubspec.yaml
PATH: c:/Github/Geo-Watch\GeoWatch - Application/pubspec.yaml
`
name: geowatch_frontend
description: "GeoWatch - Crowd Safety Intelligence mobile app."
publish_to: 'none'
version: 1.0.0+1

environment:
  sdk: ^3.11.0

dependencies:
  flutter:
    sdk: flutter
  cupertino_icons: ^1.0.8
  provider: ^6.1.2
  dio: ^5.9.0
  geolocator: ^13.0.4
  permission_handler: ^11.4.0
  connectivity_plus: ^5.0.2
  shared_preferences: ^2.5.3

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^6.0.0

flutter:
  uses-material-design: true

`

### 23. app.dart
PATH: c:/Github/Geo-Watch\GeoWatch - Application/lib/app.dart
`
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:provider/provider.dart';

import 'core/theme/app_theme.dart';
import 'screens/event_home_screen.dart';
import 'screens/events_screen.dart';
import 'screens/location_required_screen.dart';
import 'screens/registration_screen.dart';
import 'screens/settings_screen.dart';
import 'screens/splash_screen.dart';
import 'screens/success_screen.dart';
import 'services/connectivity_service.dart';
import 'services/location_service.dart';
import 'viewmodels/auth_viewmodel.dart';

class GeoWatchApp extends StatefulWidget {
  const GeoWatchApp({super.key});

  @override
  State<GeoWatchApp> createState() => _GeoWatchAppState();
}

class _GeoWatchAppState extends State<GeoWatchApp> with WidgetsBindingObserver {
  final GlobalKey<NavigatorState> _navigatorKey = GlobalKey<NavigatorState>();
  final LocationService _locationService = LocationService();
  StreamSubscription<ServiceStatus>? _serviceStatusSubscription;
  bool _showingLocationRequired = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _serviceStatusSubscription =
        Geolocator.getServiceStatusStream().listen((_) => _enforceLocationGate());
    WidgetsBinding.instance.addPostFrameCallback((_) => _enforceLocationGate());
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _enforceLocationGate();
    }
  }

  Future<void> _enforceLocationGate() async {
    final nav = _navigatorKey.currentState;
    if (nav == null) return;

    final hasAccess = await _locationService.hasAccess();
    if (!hasAccess) {
      if (_showingLocationRequired) return;
      _showingLocationRequired = true;
      nav.pushNamedAndRemoveUntil(LocationRequiredScreen.routeName, (_) => false);
      return;
    }

    if (!_showingLocationRequired) return;
    _showingLocationRequired = false;

    final navContext = _navigatorKey.currentContext;
    if (navContext == null || !navContext.mounted) return;
    final auth = navContext.read<AuthViewModel>();
    await auth.initialize();
    if (!navContext.mounted) return;
    nav.pushNamedAndRemoveUntil(
      auth.isRegistered ? EventsScreen.routeName : RegistrationScreen.routeName,
      (_) => false,
    );
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _serviceStatusSubscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ConnectivityService()),
        ChangeNotifierProvider(create: (_) => AuthViewModel()),
      ],
      child: MaterialApp(
        navigatorKey: _navigatorKey,
        title: 'GeoWatch - Crowd Safety Intelligence',
        theme: AppTheme.light,
        darkTheme: AppTheme.light,
        themeMode: ThemeMode.light,
        themeAnimationDuration: const Duration(milliseconds: 200),
        debugShowCheckedModeBanner: false,
        initialRoute: SplashScreen.routeName,
        routes: {
          SplashScreen.routeName: (_) => const SplashScreen(),
          RegistrationScreen.routeName: (_) => const RegistrationScreen(),
          EventsScreen.routeName: (_) => const EventsScreen(),
          EventHomeScreen.routeName: (_) => const EventHomeScreen(),
          SuccessScreen.routeName: (_) => const SuccessScreen(),
          SettingsScreen.routeName: (_) => const SettingsScreen(),
          LocationRequiredScreen.routeName: (_) => const LocationRequiredScreen(),
        },
      ),
    );
  }
}

`

### 23. registration_screen.dart
PATH: c:/Github/Geo-Watch\GeoWatch - Application/lib/screens/registration_screen.dart
`
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../viewmodels/auth_viewmodel.dart';
import '../widgets/input_field.dart';
import '../widgets/primary_button.dart';
import 'events_screen.dart';

class RegistrationScreen extends StatefulWidget {
  const RegistrationScreen({super.key});

  static const routeName = '/register';

  @override
  State<RegistrationScreen> createState() => _RegistrationScreenState();
}

class _RegistrationScreenState extends State<RegistrationScreen> {
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _phoneController = TextEditingController();

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthViewModel>();

    return Scaffold(
      appBar: AppBar(title: const Text('Complete Registration')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text('Welcome to GeoWatch',
                  style: Theme.of(context).textTheme.headlineSmall),
              const SizedBox(height: 8),
              Text(
                'Enter your full name and phone number once to continue.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
              const SizedBox(height: 20),
              InputField(
                label: 'Full Name',
                hint: 'Enter your full name',
                controller: _nameController,
              ),
              const SizedBox(height: 16),
              InputField(
                label: 'Phone Number',
                hint: '10-digit mobile number',
                controller: _phoneController,
              ),
              const SizedBox(height: 12),
              if (auth.errorMessage != null)
                Text(
                  auth.errorMessage!,
                  style: TextStyle(color: Theme.of(context).colorScheme.error),
                ),
              const SizedBox(height: 16),
              PrimaryButton(
                label: auth.isSaving ? 'Saving...' : 'Continue',
                icon: Icons.arrow_forward_rounded,
                onPressed: auth.isSaving ? null : _register,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _register() async {
    final auth = context.read<AuthViewModel>();
    final name = _nameController.text.trim();
    final phone = _phoneController.text.trim();

    if (name.isEmpty || phone.length < 10) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter valid name and phone number.')),
      );
      return;
    }

    final ok = await auth.register(fullName: name, phoneNumber: phone);
    if (!mounted || !ok) return;

    Navigator.pushNamedAndRemoveUntil(
      context,
      EventsScreen.routeName,
      (_) => false,
    );
  }
}

`

### 23. auth_viewmodel.dart
PATH: c:/Github/Geo-Watch\GeoWatch - Application/lib/viewmodels/auth_viewmodel.dart
`
import 'package:flutter/foundation.dart';

import '../services/user_profile_service.dart';

class AuthViewModel extends ChangeNotifier {
  AuthViewModel({UserProfileService? profileService})
      : _profileService = profileService ?? UserProfileService();

  final UserProfileService _profileService;

  bool _isInitialized = false;
  bool _isRegistered = false;
  bool _isSaving = false;
  String? _fullName;
  String? _phoneNumber;
  String? _errorMessage;

  bool get isInitialized => _isInitialized;
  bool get isRegistered => _isRegistered;
  bool get isSaving => _isSaving;
  String? get fullName => _fullName;
  String? get phoneNumber => _phoneNumber;
  String? get errorMessage => _errorMessage;

  Future<void> initialize() async {
    if (_isInitialized) return;
    final profile = await _profileService.loadProfile();
    if (profile != null) {
      _isRegistered = true;
      _fullName = profile.fullName;
      _phoneNumber = profile.phoneNumber;
    }
    _isInitialized = true;
    notifyListeners();
  }

  Future<bool> register({
    required String fullName,
    required String phoneNumber,
  }) async {
    _isSaving = true;
    _errorMessage = null;
    notifyListeners();

    try {
      await _profileService.saveProfile(
        fullName: fullName,
        phoneNumber: phoneNumber,
      );
      _fullName = fullName.trim();
      _phoneNumber = phoneNumber;
      _isRegistered = true;
      return true;
    } catch (_) {
      _errorMessage = 'Unable to save registration. Please try again.';
      return false;
    } finally {
      _isSaving = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    await _profileService.clearProfile();
    _isRegistered = false;
    _fullName = null;
    _phoneNumber = null;
    _errorMessage = null;
    notifyListeners();
  }
}

`

### 23. user_profile_service.dart
PATH: c:/Github/Geo-Watch\GeoWatch - Application/lib/services/user_profile_service.dart
`
import 'package:shared_preferences/shared_preferences.dart';

class UserProfile {
  const UserProfile({
    required this.fullName,
    required this.phoneNumber,
  });

  final String fullName;
  final String phoneNumber;
}

class UserProfileService {
  static const _nameKey = 'user_full_name';
  static const _phoneKey = 'user_phone_number';

  Future<UserProfile?> loadProfile() async {
    final prefs = await SharedPreferences.getInstance();
    final name = prefs.getString(_nameKey)?.trim();
    final phone = prefs.getString(_phoneKey)?.trim();
    if (name == null || phone == null || name.isEmpty || phone.isEmpty) {
      return null;
    }
    return UserProfile(fullName: name, phoneNumber: phone);
  }

  Future<void> saveProfile({
    required String fullName,
    required String phoneNumber,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_nameKey, fullName.trim());
    await prefs.setString(_phoneKey, phoneNumber.trim());
  }

  Future<void> clearProfile() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_nameKey);
    await prefs.remove(_phoneKey);
  }
}

`

### 23. api_client.dart
PATH: c:/Github/Geo-Watch\GeoWatch - Application/lib/core/network/api_client.dart
`
import 'package:dio/dio.dart';

import '../constants/api_constants.dart';

class ApiClient {
  ApiClient()
      : dio = Dio(
          BaseOptions(
            baseUrl: ApiConstants.baseUrl,
            connectTimeout: const Duration(seconds: 15),
            receiveTimeout: const Duration(seconds: 15),
            headers: {'Content-Type': 'application/json'},
          ),
        );

  final Dio dio;
}

`

### 24. build.gradle (android/app)
NOT FOUND

### 24. build.gradle.kts (android/app)
PATH: c:/Github/Geo-Watch\GeoWatch - Application/android/app/build.gradle.kts
`
plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.example.women_safety_app"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "com.example.women_safety_app"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    buildTypes {
        release {
            // TODO: Add your own signing config for the release build.
            // Signing with the debug keys for now, so `flutter run --release` works.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

flutter {
    source = "../.."
}

`

### 27. Info.plist
PATH: c:/Github/Geo-Watch\GeoWatch - Application/ios/Runner/Info.plist
`
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>CADisableMinimumFrameDurationOnPhone</key>
	<true/>
	<key>CFBundleDevelopmentRegion</key>
	<string>$(DEVELOPMENT_LANGUAGE)</string>
	<key>CFBundleDisplayName</key>
	<string>GeoWatch</string>
	<key>CFBundleExecutable</key>
	<string>$(EXECUTABLE_NAME)</string>
	<key>CFBundleIdentifier</key>
	<string>$(PRODUCT_BUNDLE_IDENTIFIER)</string>
	<key>CFBundleInfoDictionaryVersion</key>
	<string>6.0</string>
	<key>CFBundleName</key>
	<string>GeoWatch</string>
	<key>CFBundlePackageType</key>
	<string>APPL</string>
	<key>CFBundleShortVersionString</key>
	<string>$(FLUTTER_BUILD_NAME)</string>
	<key>CFBundleSignature</key>
	<string>????</string>
	<key>CFBundleVersion</key>
	<string>$(FLUTTER_BUILD_NUMBER)</string>
	<key>LSRequiresIPhoneOS</key>
	<true/>
	<key>NSLocationWhenInUseUsageDescription</key>
	<string>Your location is used to discover nearby events and improve safety reporting.</string>
	<key>UIApplicationSceneManifest</key>
	<dict>
		<key>UIApplicationSupportsMultipleScenes</key>
		<false/>
		<key>UISceneConfigurations</key>
		<dict>
			<key>UIWindowSceneSessionRoleApplication</key>
			<array>
				<dict>
					<key>UISceneClassName</key>
					<string>UIWindowScene</string>
					<key>UISceneConfigurationName</key>
					<string>flutter</string>
					<key>UISceneDelegateClassName</key>
					<string>$(PRODUCT_MODULE_NAME).SceneDelegate</string>
					<key>UISceneStoryboardFile</key>
					<string>Main</string>
				</dict>
			</array>
		</dict>
	</dict>
	<key>UIApplicationSupportsIndirectInputEvents</key>
	<true/>
	<key>UILaunchStoryboardName</key>
	<string>LaunchScreen</string>
	<key>UIMainStoryboardFile</key>
	<string>Main</string>
	<key>UISupportedInterfaceOrientations</key>
	<array>
		<string>UIInterfaceOrientationPortrait</string>
		<string>UIInterfaceOrientationLandscapeLeft</string>
		<string>UIInterfaceOrientationLandscapeRight</string>
	</array>
	<key>UISupportedInterfaceOrientations~ipad</key>
	<array>
		<string>UIInterfaceOrientationPortrait</string>
		<string>UIInterfaceOrientationPortraitUpsideDown</string>
		<string>UIInterfaceOrientationLandscapeLeft</string>
		<string>UIInterfaceOrientationLandscapeRight</string>
	</array>
</dict>
</plist>

`

### 28. iOS Push Notifications
Push Notifications enabled in entitlements: False

## === GENERAL ===
### 29. Other files referencing keywords
Files referencing password, login, auth, token, jwt, phoneNumber, or AdminAuthService:
`
c:/Github/Geo-Watch\.rocketride\docs\ROCKETRIDE_COMMON_MISTAKES.md
c:/Github/Geo-Watch\.rocketride\docs\ROCKETRIDE_COMPONENT_REFERENCE.md
c:/Github/Geo-Watch\.rocketride\docs\ROCKETRIDE_OBSERVABILITY.md
c:/Github/Geo-Watch\.rocketride\docs\ROCKETRIDE_PIPELINE_RULES.md
c:/Github/Geo-Watch\.rocketride\docs\ROCKETRIDE_QUICKSTART.md
c:/Github/Geo-Watch\.rocketride\docs\ROCKETRIDE_python_API.md
c:/Github/Geo-Watch\.rocketride\docs\ROCKETRIDE_typescript_API.md
c:/Github/Geo-Watch\.rocketride\schema\accessibility_describe.json
c:/Github/Geo-Watch\.rocketride\schema\agent_rocketride.json
c:/Github/Geo-Watch\.rocketride\schema\astra_db.json
c:/Github/Geo-Watch\.rocketride\schema\chroma.json
c:/Github/Geo-Watch\.rocketride\schema\db_clickhouse.json
c:/Github/Geo-Watch\.rocketride\schema\db_mysql.json
c:/Github/Geo-Watch\.rocketride\schema\db_neo4j.json
c:/Github/Geo-Watch\.rocketride\schema\db_postgres.json
c:/Github/Geo-Watch\.rocketride\schema\db_supabase.json
c:/Github/Geo-Watch\.rocketride\schema\elasticsearch.json
c:/Github/Geo-Watch\.rocketride\schema\embedding_openai.json
c:/Github/Geo-Watch\.rocketride\schema\guardrails.json
c:/Github/Geo-Watch\.rocketride\schema\image_vision_gemini.json
c:/Github/Geo-Watch\.rocketride\schema\image_vision_mistral.json
c:/Github/Geo-Watch\.rocketride\schema\image_vision_ollama.json
c:/Github/Geo-Watch\.rocketride\schema\llm_anthropic.json
c:/Github/Geo-Watch\.rocketride\schema\llm_bedrock.json
c:/Github/Geo-Watch\.rocketride\schema\llm_deepseek.json
c:/Github/Geo-Watch\.rocketride\schema\llm_gemini.json
c:/Github/Geo-Watch\.rocketride\schema\llm_gmi_cloud.json
c:/Github/Geo-Watch\.rocketride\schema\llm_minimax.json
c:/Github/Geo-Watch\.rocketride\schema\llm_mistral.json
c:/Github/Geo-Watch\.rocketride\schema\llm_nebius.json
c:/Github/Geo-Watch\.rocketride\schema\llm_ollama.json
c:/Github/Geo-Watch\.rocketride\schema\llm_openai.json
c:/Github/Geo-Watch\.rocketride\schema\llm_openai_api.json
c:/Github/Geo-Watch\.rocketride\schema\llm_perplexity.json
c:/Github/Geo-Watch\.rocketride\schema\llm_qwen.json
c:/Github/Geo-Watch\.rocketride\schema\llm_xai.json
c:/Github/Geo-Watch\.rocketride\schema\mcp_client.json
c:/Github/Geo-Watch\.rocketride\schema\memory_persistent.json
c:/Github/Geo-Watch\.rocketride\schema\milvus.json
c:/Github/Geo-Watch\.rocketride\schema\mongodb_srv.json
c:/Github/Geo-Watch\.rocketride\schema\opensearch.json
c:/Github/Geo-Watch\.rocketride\schema\pinecone.json
c:/Github/Geo-Watch\.rocketride\schema\postgres.json
c:/Github/Geo-Watch\.rocketride\schema\preprocessor_code.json
c:/Github/Geo-Watch\.rocketride\schema\preprocessor_langchain.json
c:/Github/Geo-Watch\.rocketride\schema\preprocessor_llm.json
c:/Github/Geo-Watch\.rocketride\schema\qdrant.json
c:/Github/Geo-Watch\.rocketride\schema\remote.json
c:/Github/Geo-Watch\.rocketride\schema\rerank_cohere.json
c:/Github/Geo-Watch\.rocketride\schema\telegram.json
c:/Github/Geo-Watch\.rocketride\schema\tool_butterbase.json
c:/Github/Geo-Watch\.rocketride\schema\tool_git.json
c:/Github/Geo-Watch\.rocketride\schema\tool_github.json
c:/Github/Geo-Watch\.rocketride\schema\tool_http_request.json
c:/Github/Geo-Watch\.rocketride\schema\twelvelabs.json
c:/Github/Geo-Watch\.rocketride\schema\weaviate.json
c:/Github/Geo-Watch\.rocketride\schema\webhook.json
c:/Github/Geo-Watch\.rocketride\services-catalog.json
c:/Github/Geo-Watch\GeoWatch - Application\LICENSE
c:/Github/Geo-Watch\GeoWatch - Application\README.md
c:/Github/Geo-Watch\GeoWatch - Application\ios\Runner\Assets.xcassets\AppIcon.appiconset\Contents.json
c:/Github/Geo-Watch\GeoWatch - Application\ios\Runner\Assets.xcassets\LaunchImage.imageset\Contents.json
c:/Github/Geo-Watch\GeoWatch - Application\lib\app.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\models\incident_request.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\models\issue_resolved_request.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\screens\event_home_screen.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\screens\incident_report_screen.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\screens\location_required_screen.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\screens\registration_screen.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\screens\settings_screen.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\screens\splash_screen.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\services\user_profile_service.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\viewmodels\auth_viewmodel.dart
c:/Github/Geo-Watch\GeoWatch - Application\lib\viewmodels\incident_viewmodel.dart
c:/Github/Geo-Watch\GeoWatch - Application\macos\Runner\Assets.xcassets\AppIcon.appiconset\Contents.json
c:/Github/Geo-Watch\GeoWatch - Backend\.idea\.name
c:/Github/Geo-Watch\GeoWatch - Backend\mvnw
c:/Github/Geo-Watch\GeoWatch - Backend\mvnw.cmd
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\config\CorsConfig.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\controller\AdminAuthController.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\dto\AdminLoginRequest.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\dto\AdminLoginResponse.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\dto\AdminRegisterRequest.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\dto\CreateIncidentRequest.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\dto\OrganizerDTO.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\model\Admin.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\model\Incident.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\model\Organizer.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\repository\IncidentRepository.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\service\AdminAuthService.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\service\EventService.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\java\com\safety\womensafety\service\IncidentService.java
c:/Github/Geo-Watch\GeoWatch - Backend\src\main\resources\application.properties
c:/Github/Geo-Watch\GeoWatch - Frontend\dist\assets\index-Cd94JClW.js
c:/Github/Geo-Watch\GeoWatch - Frontend\package-lock.json
c:/Github/Geo-Watch\GeoWatch - Frontend\src\pages\AdminLogin.tsx
c:/Github/Geo-Watch\GeoWatch - Frontend\src\pages\AdminRegister.tsx
c:/Github/Geo-Watch\GeoWatch - Frontend\src\pages\CreateEvent.tsx
c:/Github/Geo-Watch\GeoWatch - Frontend\src\pages\Dashboard.tsx
c:/Github/Geo-Watch\GeoWatch - Frontend\src\pages\Home.tsx
c:/Github/Geo-Watch\GeoWatch - Frontend\src\router\AppRouter.tsx
c:/Github/Geo-Watch\GeoWatch - Frontend\src\services\api.ts
c:/Github/Geo-Watch\GeoWatch - Frontend\src\types\cluster.ts
c:/Github/Geo-Watch\README.md
c:/Github/Geo-Watch\benchmark\api_benchmark.js
c:/Github/Geo-Watch\benchmark\e2e_benchmark.js
c:/Github/Geo-Watch\benchmark\ingestion_stress_test.js
c:/Github/Geo-Watch\benchmark\results\GeoWatch_Scalability_Performance_Validation_Report.md
c:/Github/Geo-Watch\benchmark\run_all.ps1
c:/Github/Geo-Watch\benchmark\websocket_benchmark.js
c:/Github/Geo-Watch\design.md
c:/Github/Geo-Watch\docs\ARCHITECTURE.md
c:/Github/Geo-Watch\geo_watch_master_audit.md
c:/Github/Geo-Watch\migration_context.md
c:/Github/Geo-Watch\ws_test\package.json
c:/Github/Geo-Watch\ws_test\run_debounce_test.js
c:/Github/Geo-Watch\ws_test\run_isolation_test.js
c:/Github/Geo-Watch\ws_test\run_websocket_test.js
`

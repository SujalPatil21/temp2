package com.safety.womensafety;

import com.safety.womensafety.dto.CreateIncidentRequest;
import com.safety.womensafety.model.Event;
import com.safety.womensafety.model.Incident;
import com.safety.womensafety.repository.EventRepository;
import com.safety.womensafety.repository.IncidentRepository;
import com.safety.womensafety.repository.AdminRepository;
import com.safety.womensafety.model.Admin;
import com.safety.womensafety.service.GeminiRiskAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // uses application-test.properties
public class GeminiE2ETest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private GeminiRiskAnalysisService geminiService;

    private Long eventId;

    @BeforeEach
    public void setup() {
        incidentRepository.deleteAll();
        eventRepository.deleteAll();
        adminRepository.deleteAll();

        Event event = new Event();
        event.setName("Test Event");
        event.setCenterLat(40.0);
        event.setCenterLng(-74.0);
        event.setRadius(1000.0);
        Admin admin = new Admin();
        admin.setEmail("admin@example.com");
        admin.setPassword("password");

        admin = adminRepository.save(admin);
        
        event.setAdmin(admin);
        event.setStartTime(LocalDateTime.now().minusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(1));

        Event savedEvent = eventRepository.save(event);
        eventId = savedEvent.getId();
    }

    @Test
    public void testHighSemanticRisk() throws InterruptedException {
        CreateIncidentRequest req = new CreateIncidentRequest(eventId, "John", "1234567890", 40.0, -74.0, "Someone is assaulting a woman near the entrance.");
        
            ResponseEntity<Long> response = restTemplate.postForEntity("http://localhost:" + port + "/api/incidents", req, Long.class);
        assertEquals(200, response.getStatusCode().value());
        
        // Wait for async Gemini call
        Thread.sleep(8000);
        
        Incident incident = incidentRepository.findById(response.getBody()).orElseThrow();
        assertEquals("HIGH", incident.getSemanticRisk());
        assertNotNull(incident.getIncidentType());
        System.out.println("TEST 1 - HIGH: " + incident.getSemanticRisk() + " type: " + incident.getIncidentType() + " reason: " + incident.getAiReasoning());
    }

    @Test
    public void testNoDescription() throws InterruptedException {
        CreateIncidentRequest req = new CreateIncidentRequest(eventId, "Jane", "1234567891", 40.0, -74.0, null);
        
            ResponseEntity<Long> response = restTemplate.postForEntity("http://localhost:" + port + "/api/incidents", req, Long.class);
        assertEquals(200, response.getStatusCode().value());
        
        Thread.sleep(3000); // Async cluster runs
        
        Incident incident = incidentRepository.findById(response.getBody()).orElseThrow();
        assertNull(incident.getSemanticRisk());
        System.out.println("TEST 2 - NO DESC: " + incident.getSemanticRisk());
    }

    @Test
    public void testLowSemanticRisk() throws InterruptedException {
        CreateIncidentRequest req = new CreateIncidentRequest(eventId, "Bob", "1234567892", 40.0, -74.0, "I lost my phone near the food court.");
        
            ResponseEntity<Long> response = restTemplate.postForEntity("http://localhost:" + port + "/api/incidents", req, Long.class);
        assertEquals(200, response.getStatusCode().value());
        
        Thread.sleep(8000);
        
        Incident incident = incidentRepository.findById(response.getBody()).orElseThrow();
        assertEquals("LOW", incident.getSemanticRisk());
        System.out.println("TEST 3 - LOW: " + incident.getSemanticRisk() + " type: " + incident.getIncidentType());
    }

    @Test
    public void testGeminiFailure() throws InterruptedException {
        // Break API Key
        String originalKey = (String) ReflectionTestUtils.getField(geminiService, "geminiApiKey");
        ReflectionTestUtils.setField(geminiService, "geminiApiKey", "INVALID_KEY");
        
        try {
            CreateIncidentRequest req = new CreateIncidentRequest(eventId, "Alice", "1234567893", 40.0, -74.0, "Someone has a weapon");
            ResponseEntity<Long> response = restTemplate.postForEntity("http://localhost:" + port + "/api/incidents", req, Long.class);
            assertEquals(200, response.getStatusCode().value());
            
            Thread.sleep(5000);
            
            Incident incident = incidentRepository.findById(response.getBody()).orElseThrow();
            assertNull(incident.getSemanticRisk(), "Should be null on failure");
            System.out.println("TEST 4 - FAILURE: " + incident.getSemanticRisk());
        } finally {
            ReflectionTestUtils.setField(geminiService, "geminiApiKey", originalKey);
        }
    }
}

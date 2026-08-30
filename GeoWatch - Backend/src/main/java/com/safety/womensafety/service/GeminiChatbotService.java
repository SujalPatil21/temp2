package com.safety.womensafety.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safety.womensafety.dto.ClusterResponse;
import com.safety.womensafety.model.Incident;
import com.safety.womensafety.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiChatbotService {

    private static final Logger log = LoggerFactory.getLogger(GeminiChatbotService.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final IncidentRepository incidentRepository;
    private final DbscanClusteringService clusteringService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    public GeminiChatbotService(IncidentRepository incidentRepository, DbscanClusteringService clusteringService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.incidentRepository = incidentRepository;
        this.clusteringService = clusteringService;
    }

    public String chatWithGemini(Long eventId, String userMessage, String language) {
        String url = geminiApiUrl + "?key=" + geminiApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String systemInstruction = "You are a live event intelligence assistant for an admin dashboard. " +
                "You must strictly use the tools provided to answer questions about the current event. " +
                "Do NOT invent incidents or data. If there are no active incidents, state that clearly. " +
                "Keep responses concise and operational (e.g. state risk, count, trend). " +
                "Do NOT modify state. Answer the user's question in this language: " + language;

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // System instructions
            ObjectNode sysInstr = requestBody.putObject("system_instruction");
            sysInstr.putArray("parts").addObject().put("text", systemInstruction);

            // Contents array
            ArrayNode contentsArray = requestBody.putArray("contents");
            ObjectNode userMessageNode = contentsArray.addObject();
            userMessageNode.put("role", "user");
            userMessageNode.putArray("parts").addObject().put("text", userMessage);

            // Tools array
            ArrayNode toolsArray = requestBody.putArray("tools");
            ObjectNode toolNode = toolsArray.addObject();
            ArrayNode functionDeclarations = toolNode.putArray("function_declarations");
            
            // Tool 1: getActiveClusters
            ObjectNode getActiveClusters = functionDeclarations.addObject();
            getActiveClusters.put("name", "getActiveClusters");
            getActiveClusters.put("description", "Get all current active spatial clusters of incidents. Returns cluster ID, risk level, and incident count.");
            
            // Tool 2: getUnresolvedIncidents
            ObjectNode getUnresolvedIncidents = functionDeclarations.addObject();
            getUnresolvedIncidents.put("name", "getUnresolvedIncidents");
            getUnresolvedIncidents.put("description", "Get a summary of recent unresolved incidents including their semantic risk and descriptions.");

            int maxTurns = 5;
            int currentTurn = 0;

            while (currentTurn < maxTurns) {
                HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode rootNode = objectMapper.readTree(response.getBody());
                    JsonNode candidates = rootNode.path("candidates");
                    
                    if (candidates.isArray() && candidates.size() > 0) {
                        JsonNode content = candidates.get(0).path("content");
                        JsonNode parts = content.path("parts");
                        
                        if (parts.isArray() && parts.size() > 0) {
                            JsonNode part = parts.get(0);
                            
                            // Check if model responded with text
                            if (part.has("text")) {
                                return part.get("text").asText();
                            } 
                            // Check if model invoked a tool
                            else if (part.has("functionCall")) {
                                JsonNode functionCall = part.get("functionCall");
                                String functionName = functionCall.path("name").asText();
                                
                                // Append model's functionCall to history
                                contentsArray.add(content);
                                
                                // Execute local tool
                                JsonNode functionResult = executeTool(functionName, eventId);
                                
                                // Append our functionResponse to history
                                ObjectNode userFuncResNode = contentsArray.addObject();
                                userFuncResNode.put("role", "user");
                                ObjectNode functionResponseNode = userFuncResNode.putArray("parts").addObject().putObject("functionResponse");
                                functionResponseNode.put("name", functionName);
                                functionResponseNode.set("response", functionResult);
                                
                                currentTurn++;
                                continue;
                            }
                        }
                    }
                }
                break;
            }
            
            return "I am currently unable to retrieve the requested event intelligence. Please try again.";

        } catch (Exception e) {
            log.error("Error communicating with Gemini Chatbot API", e);
            return "Sorry, I encountered an error while analyzing the event data.";
        }
    }

    private JsonNode executeTool(String functionName, Long eventId) throws JsonProcessingException {
        ObjectNode resultNode = objectMapper.createObjectNode();
        
        LocalDateTime timeThreshold = LocalDateTime.now().minusHours(24);
        List<Incident> activeIncidents = incidentRepository.findByEventIdAndTimestampAfterAndResolvedFalse(eventId, timeThreshold);

        if ("getActiveClusters".equals(functionName)) {
            List<ClusterResponse> clusters = clusteringService.performClustering(activeIncidents);
            ArrayNode clustersArray = resultNode.putArray("clusters");
            int clusterId = 1;
            for (ClusterResponse c : clusters) {
                ObjectNode cNode = clustersArray.addObject();
                cNode.put("clusterId", "Cluster #" + clusterId++);
                cNode.put("riskLevel", c.getRiskLevel());
                cNode.put("incidentCount", c.getIncidentCount());
                cNode.put("centerLat", c.getCenterLat());
                cNode.put("centerLng", c.getCenterLng());
            }
        } else if ("getUnresolvedIncidents".equals(functionName)) {
            ArrayNode incidentsArray = resultNode.putArray("incidents");
            for (Incident i : activeIncidents) {
                ObjectNode iNode = incidentsArray.addObject();
                iNode.put("id", i.getId());
                iNode.put("type", i.getIncidentType());
                iNode.put("description", i.getDescription());
                iNode.put("semanticRisk", i.getSemanticRisk());
            }
        } else {
            resultNode.put("error", "Unknown function " + functionName);
        }
        
        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.set("result", resultNode);
        return wrapper;
    }
}

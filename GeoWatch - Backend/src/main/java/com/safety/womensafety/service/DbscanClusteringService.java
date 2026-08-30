package com.safety.womensafety.service;

import com.safety.womensafety.dto.ClusterResponse;
import com.safety.womensafety.model.Incident;
import com.safety.womensafety.util.GeoUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbscanClusteringService implements ClusteringService {

    private static final double EPS = 50.0; // 50 meters
    private static final int MIN_PTS = 2; // changed from 3 → 2

    @Override
    public List<ClusterResponse> performClustering(List<Incident> incidents) {
        if (incidents.isEmpty()) {
            return new ArrayList<>();
        }

        List<ClusterResponse> clusters = new ArrayList<>();
        Set<Incident> visited = new HashSet<>();
        List<List<Incident>> dbscanClusters = new ArrayList<>();

        // 1. Build Spatial Index
        SpatialIndex index = new SpatialIndex(EPS);
        for (Incident incident : incidents) {
            index.add(incident);
        }

        for (Incident incident : incidents) {
            if (visited.contains(incident)) {
                continue;
            }
            visited.add(incident);

            // 2. Query neighbors using Spatial Index
            List<Incident> neighbors = index.getNeighbors(incident, EPS);

            if (neighbors.size() < MIN_PTS) {
                continue; // noise
            }

            List<Incident> cluster = new ArrayList<>();
            dbscanClusters.add(cluster);

            expandCluster(cluster, incident, neighbors, index, visited);
        }

        Set<Incident> clusteredIncidents = new HashSet<>();
        for (List<Incident> cluster : dbscanClusters) {
            clusteredIncidents.addAll(cluster);
            
            double centerLat = averageLatitude(cluster);
            double centerLng = averageLongitude(cluster);
            int incidentCount = cluster.size();
            String spatialRisk = determineRiskLevel(incidentCount);
            
            int highestSemanticValue = 0;
            List<String> types = new ArrayList<>();
            for (Incident inc : cluster) {
                if (inc.getSemanticRisk() != null) {
                    int val = mapRisk(inc.getSemanticRisk());
                    if (val > highestSemanticValue) {
                        highestSemanticValue = val;
                    }
                }
                if (inc.getIncidentType() != null && !inc.getIncidentType().isEmpty()) {
                    if (!types.contains(inc.getIncidentType())) {
                        types.add(inc.getIncidentType());
                    }
                }
            }
            
            int spatialValue = mapRisk(spatialRisk);
            int finalRiskValue = Math.max(spatialValue, highestSemanticValue);
            String finalRiskLevel = unmapRisk(finalRiskValue);
            
            String highestSemanticRisk = highestSemanticValue > 0 ? unmapRisk(highestSemanticValue) : null;
            
            clusters.add(new ClusterResponse(centerLat, centerLng, incidentCount, finalRiskLevel, highestSemanticRisk, types));
        }

        for (Incident incident : incidents) {
            if (!clusteredIncidents.contains(incident)) {
                String spatialRisk = "LOW";
                int highestSemanticValue = 0;
                List<String> types = new ArrayList<>();
                if (incident.getSemanticRisk() != null) {
                    highestSemanticValue = mapRisk(incident.getSemanticRisk());
                }
                if (incident.getIncidentType() != null && !incident.getIncidentType().isEmpty()) {
                    types.add(incident.getIncidentType());
                }
                
                int finalRiskValue = Math.max(mapRisk(spatialRisk), highestSemanticValue);
                String finalRiskLevel = unmapRisk(finalRiskValue);
                String highestSemanticRisk = highestSemanticValue > 0 ? unmapRisk(highestSemanticValue) : null;
                
                clusters.add(new ClusterResponse(incident.getLatitude(), incident.getLongitude(), 1, finalRiskLevel, highestSemanticRisk, types));
            }
        }

        return clusters;
    }

    private void expandCluster(
            List<Incident> cluster,
            Incident incident,
            List<Incident> neighbors,
            SpatialIndex index,
            Set<Incident> visited
    ) {
        cluster.add(incident);
        Set<Incident> clusterSet = new HashSet<>();
        clusterSet.add(incident);
        
        Set<Incident> neighborSet = new HashSet<>(neighbors);
        
        for (int i = 0; i < neighbors.size(); i++) {
            Incident neighbor = neighbors.get(i);
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                List<Incident> neighborNeighbors = index.getNeighbors(neighbor, EPS);
                if (neighborNeighbors.size() >= MIN_PTS) {
                    for (Incident nn : neighborNeighbors) {
                        if (neighborSet.add(nn)) {
                            neighbors.add(nn);
                        }
                    }
                }
            }
            if (clusterSet.add(neighbor)) {
                cluster.add(neighbor);
            }
        }
    }

    private double averageLatitude(List<Incident> cluster) {
        return cluster.stream().mapToDouble(Incident::getLatitude).average().orElse(0.0);
    }

    private double averageLongitude(List<Incident> cluster) {
        return cluster.stream().mapToDouble(Incident::getLongitude).average().orElse(0.0);
    }

    private String determineRiskLevel(int incidentCount) {
        if (incidentCount >= 6) {
            return "HIGH";
        } else if (incidentCount >= 3) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private int mapRisk(String risk) {
        if ("HIGH".equals(risk)) return 3;
        if ("MEDIUM".equals(risk)) return 2;
        return 1;
    }

    private String unmapRisk(int val) {
        if (val >= 3) return "HIGH";
        if (val == 2) return "MEDIUM";
        return "LOW";
    }

    private static class SpatialIndex {
        private final double deltaLat;
        private final double deltaLon;
        private final Map<String, List<Incident>> grid = new HashMap<>();

        public SpatialIndex(double eps) {
            this.deltaLat = eps / 111320.0;
            // Cosine of latitude is roughly cos(12.9716) for our target dataset locations (Bangalore).
            // This is a safe and high-performance approximation.
            this.deltaLon = eps / (111320.0 * Math.cos(Math.toRadians(12.9716)));
        }

        private String getGridKey(double lat, double lon) {
            long x = (long) (lat / deltaLat);
            long y = (long) (lon / deltaLon);
            return x + "_" + y;
        }

        public void add(Incident incident) {
            String key = getGridKey(incident.getLatitude(), incident.getLongitude());
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(incident);
        }

        public List<Incident> getNeighbors(Incident incident, double eps) {
            List<Incident> neighbors = new ArrayList<>();
            double lat = incident.getLatitude();
            double lon = incident.getLongitude();
            
            long centerX = (long) (lat / deltaLat);
            long centerY = (long) (lon / deltaLon);

            for (long dx = -1; dx <= 1; dx++) {
                for (long dy = -1; dy <= 1; dy++) {
                    String key = (centerX + dx) + "_" + (centerY + dy);
                    List<Incident> cellIncidents = grid.get(key);
                    if (cellIncidents != null) {
                        for (Incident other : cellIncidents) {
                            // Pre-filter bounding box check
                            if (Math.abs(lat - other.getLatitude()) > deltaLat || Math.abs(lon - other.getLongitude()) > deltaLon) {
                                continue;
                            }
                            if (GeoUtil.calculateDistance(lat, lon, other.getLatitude(), other.getLongitude()) <= eps) {
                                neighbors.add(other);
                            }
                        }
                    }
                }
            }
            return neighbors;
        }
    }
}
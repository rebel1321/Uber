package com.uber.springserver.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.springserver.models.Captain;
import com.uber.springserver.repositories.CaptainRepository;
import com.uber.springserver.utils.EnvUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MapService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CaptainRepository captainRepository;

    public MapService(CaptainRepository captainRepository) {
        this.captainRepository = captainRepository;
    }

    public Map<String, Object> getAddressCoordinates(String address) throws Exception {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("address required");
        }

        String apiKey = EnvUtil.get("OLA_API_KEY", "");
        String url = "https://api.olamaps.io/places/v1/geocode?address=" +
                URLEncoder.encode(address, StandardCharsets.UTF_8) +
                "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode results = root.path("geocodingResults");
        if (!results.isArray() || results.isEmpty()) {
            throw new IllegalStateException("no coordinates found");
        }

        JsonNode first = results.get(0);
        JsonNode location = first.path("geometry").path("location");

        Map<String, Object> data = new HashMap<>();
        data.put("latitude", location.path("lat").asDouble());
        data.put("longitude", location.path("lng").asDouble());
        data.put("formatted_address", first.path("formatted_address").asText());
        return data;
    }

    public Map<String, Object> getDistanceAndTime(String origin, String destination) throws Exception {
        if (origin == null || destination == null || origin.isBlank() || destination.isBlank()) {
            throw new IllegalArgumentException("origin and destination required");
        }

        Map<String, Object> originCoords = getAddressCoordinates(origin);
        Map<String, Object> destinationCoords = getAddressCoordinates(destination);

        String apiKey = EnvUtil.get("OLA_API_KEY", "");
        String origins = originCoords.get("latitude") + "," + originCoords.get("longitude");
        String destinations = destinationCoords.get("latitude") + "," + destinationCoords.get("longitude");

        String url = "https://api.olamaps.io/routing/v1/distanceMatrix?origins=" +
                URLEncoder.encode(origins, StandardCharsets.UTF_8) +
                "&destinations=" + URLEncoder.encode(destinations, StandardCharsets.UTF_8) +
                "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        double distance = 0;
        double duration = 0;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode rows = root.path("rows");
            if (rows.isArray() && !rows.isEmpty()) {
                JsonNode elements = rows.get(0).path("elements");
                if (elements.isArray() && !elements.isEmpty()) {
                    JsonNode firstElement = elements.get(0);

                    JsonNode distanceNode = firstElement.path("distance");
                    JsonNode durationNode = firstElement.path("duration");

                    distance = readNodeValue(distanceNode, "distance", "distanceMeters", "value");
                    duration = readNodeValue(durationNode, "duration", "durationSeconds", "value");
                }
            }
        } catch (Exception ignored) {
            // Geocoding succeeded, so calculate a real fallback distance below.
        }

        if (distance <= 0) {
            double originLat = ((Number) originCoords.get("latitude")).doubleValue();
            double originLng = ((Number) originCoords.get("longitude")).doubleValue();
            double destinationLat = ((Number) destinationCoords.get("latitude")).doubleValue();
            double destinationLng = ((Number) destinationCoords.get("longitude")).doubleValue();

            double directDistanceKm = haversineKm(originLat, originLng, destinationLat, destinationLng);
            distance = directDistanceKm * 1000;
            if (duration <= 0) {
                duration = directDistanceKm > 0 ? directDistanceKm * 120 : 0;
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("distance", distance);
        data.put("duration", duration);
        return data;
    }

    private double readNodeValue(JsonNode node, String... fieldNames) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return 0;
        }
        if (node.isObject()) {
            for (String fieldName : fieldNames) {
                JsonNode valueNode = node.path(fieldName);
                if (valueNode.isNumber()) {
                    return valueNode.asDouble();
                }
            }
        }
        return node.asDouble(0);
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    public List<Object> getAutoCompleteSuggestions(String input) throws Exception {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("input required");
        }

        String apiKey = EnvUtil.get("OLA_API_KEY", "");
        String url = "https://api.olamaps.io/places/v1/autocomplete?input=" +
                URLEncoder.encode(input, StandardCharsets.UTF_8) +
                "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());

        List<Object> predictions = new ArrayList<>();
        JsonNode predictionNode = root.path("predictions");
        if (predictionNode.isArray()) {
            for (JsonNode node : predictionNode) {
                predictions.add(objectMapper.convertValue(node, Object.class));
            }
        }

        return predictions;
    }

    public List<Captain> getCaptainsInRadius(double lat, double lng, double radiusKm) {
        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.max(Math.cos(Math.toRadians(lat)), 0.1));

        List<Captain> captains = captainRepository.findAll();
        List<Captain> inRadius = new ArrayList<>();
        for (Captain captain : captains) {
            if (captain.getLocation() == null || captain.getLocation().getLtd() == null || captain.getLocation().getLng() == null) {
                continue;
            }
            double cLat = captain.getLocation().getLtd();
            double cLng = captain.getLocation().getLng();
            if (cLat >= lat - latDelta && cLat <= lat + latDelta && cLng >= lng - lngDelta && cLng <= lng + lngDelta) {
                inRadius.add(captain);
            }
        }
        return inRadius;
    }
}

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

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode firstElement = root.path("rows").get(0).path("elements").get(0);

        Map<String, Object> data = new HashMap<>();
        data.put("distance", firstElement.path("distance").asDouble());
        data.put("duration", firstElement.path("duration").asDouble());
        return data;
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

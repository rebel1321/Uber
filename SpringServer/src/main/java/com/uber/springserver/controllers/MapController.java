package com.uber.springserver.controllers;

import com.uber.springserver.services.MapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maps")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/get-coordinates")
    public Object getCoordinates(@RequestParam String address) {
        if (address == null || address.length() < 3) {
            return Map.of("message", "Invalid address");
        }
        try {
            return mapService.getAddressCoordinates(address);
        } catch (Exception e) {
            return Map.of("message", "Coordinates not found");
        }
    }

    @GetMapping("/get-distance-time")
    public Object getDistanceTime(@RequestParam String origin, @RequestParam String destination) {
        if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            return Map.of("message", "Origin & destination required");
        }
        try {
            return mapService.getDistanceAndTime(origin, destination);
        } catch (Exception e) {
            return Map.of("message", e.getMessage());
        }
    }

    @GetMapping("/get-suggestions")
    public Object getSuggestions(@RequestParam String input) {
        if (input == null || input.isBlank()) {
            return Map.of("message", "Input required");
        }
        try {
            List<Object> data = mapService.getAutoCompleteSuggestions(input);
            return data;
        } catch (Exception e) {
            return Map.of("message", "Failed to fetch suggestions");
        }
    }
}

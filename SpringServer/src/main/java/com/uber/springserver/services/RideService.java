package com.uber.springserver.services;

import com.uber.springserver.models.Captain;
import com.uber.springserver.models.Ride;
import com.uber.springserver.models.User;
import com.uber.springserver.repositories.CaptainRepository;
import com.uber.springserver.repositories.RideRepository;
import com.uber.springserver.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final CaptainRepository captainRepository;
    private final MapService mapService;

    public RideService(RideRepository rideRepository,
                       UserRepository userRepository,
                       CaptainRepository captainRepository,
                       MapService mapService) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.captainRepository = captainRepository;
        this.mapService = mapService;
    }

    private String generateOtp(int digits) {
        SecureRandom random = new SecureRandom();
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;
        int otp = random.nextInt(max - min + 1) + min;
        return String.valueOf(otp);
    }

    private double normalizeDistanceKm(double distance) {
        if (distance <= 0) {
            return 0;
        }
        if (distance > 1000) {
            return distance / 1000;
        }
        return distance;
    }

    private double tripMultiplier(String tripType) {
        if ("round_trip".equals(tripType) || "two_way".equals(tripType) || "roundtrip".equals(tripType)) {
            return 2;
        }
        return 1;
    }

    private Map<String, Double> estimateFare(double distanceKm, String tripType) {
        Map<String, Double> base = Map.of("auto", 30.0, "car", 50.0, "moto", 20.0);
        Map<String, Double> perKm = Map.of("auto", 8.0, "car", 12.0, "moto", 6.0);

        double multiplier = tripMultiplier(tripType);
        Map<String, Double> fare = new HashMap<>();
        for (String vehicle : base.keySet()) {
            fare.put(vehicle, (base.get(vehicle) + (perKm.get(vehicle) * distanceKm)) * multiplier);
        }
        return fare;
    }

    public Map<String, Object> calculateFare(String pickup, String destination, String tripType) {
        double distanceKm = 0;
        double duration = 0;

        try {
            Map<String, Object> distanceData = mapService.getDistanceAndTime(pickup, destination);
            distanceKm = normalizeDistanceKm(((Number) distanceData.getOrDefault("distance", 0)).doubleValue());
            duration = ((Number) distanceData.getOrDefault("duration", 0)).doubleValue();
        } catch (Exception ignored) {
        }

        if (distanceKm == 0) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("fare", estimateFare(5, tripType));
            fallback.put("distance", 5);
            fallback.put("duration", duration);
            return fallback;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("fare", estimateFare(distanceKm, tripType));
        payload.put("distance", distanceKm);
        payload.put("duration", duration);
        return payload;
    }

    public Ride createRide(String userId, String pickup, String destination, String vehicleType, String tripType) {
        Map<String, Object> fareData = calculateFare(pickup, destination, tripType);
        Map<String, Double> fareMap = (Map<String, Double>) fareData.get("fare");
        Double fare = fareMap.get(vehicleType);
        if (fare == null) {
            throw new IllegalArgumentException("invalid vehicle type");
        }

        Ride ride = new Ride();
        ride.setUser(userId);
        ride.setPickup(pickup);
        ride.setDestination(destination);
        ride.setVehicleType(vehicleType);
        ride.setTripType(tripType);
        ride.setFare(fare);
        ride.setStatus("pending");
        ride.setPaid(false);
        ride.setDistance(((Number) fareData.get("distance")).doubleValue());
        ride.setDuration(((Number) fareData.get("duration")).doubleValue());
        ride.setOtp(generateOtp(6));

        return rideRepository.save(ride);
    }

    public Ride confirmRide(String rideId, String captainId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("ride not found"));
        ride.setStatus("accepted");
        ride.setCaptain(captainId);
        return rideRepository.save(ride);
    }

    public Ride startRide(String rideId, String otp) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("ride not found"));
        if (!"accepted".equals(ride.getStatus())) {
            throw new IllegalStateException("ride not accepted");
        }
        if (!otp.equals(ride.getOtp())) {
            throw new IllegalArgumentException("invalid otp");
        }
        ride.setStatus("ongoing");
        return rideRepository.save(ride);
    }

    public Ride endRide(String rideId, String captainId) {
        Ride ride = rideRepository.findByIdAndCaptain(rideId, captainId)
                .orElseThrow(() -> new IllegalArgumentException("ride not found"));
        if (!"ongoing".equals(ride.getStatus())) {
            throw new IllegalStateException("ride not ongoing");
        }
        ride.setStatus("completed");
        return rideRepository.save(ride);
    }

    public Ride markRidePaid(String rideId, String userId) {
        Ride ride = rideRepository.findByIdAndUser(rideId, userId)
                .orElseThrow(() -> new IllegalArgumentException("ride not found"));
        if (!"completed".equals(ride.getStatus())) {
            throw new IllegalStateException("ride not completed");
        }
        ride.setPaid(true);
        return rideRepository.save(ride);
    }

    public Map<String, Object> buildRidePayload(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("ride not found"));

        User user = userRepository.findById(ride.getUser()).orElse(null);
        Captain captain = null;
        if (ride.getCaptain() != null) {
            captain = captainRepository.findById(ride.getCaptain()).orElse(null);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("_id", ride.getId());
        payload.put("pickup", ride.getPickup());
        payload.put("destination", ride.getDestination());
        payload.put("vehicleType", ride.getVehicleType());
        payload.put("tripType", ride.getTripType());
        payload.put("fare", ride.getFare());
        payload.put("status", ride.getStatus());
        payload.put("paid", Optional.ofNullable(ride.getPaid()).orElse(false));
        payload.put("otp", ride.getOtp());
        payload.put("distance", ride.getDistance());
        payload.put("duration", ride.getDuration());

        if (user != null) {
            Map<String, Object> userPayload = new HashMap<>();
            userPayload.put("_id", user.getId());
            userPayload.put("fullName", user.getFullName());
            userPayload.put("email", user.getEmail());
            payload.put("user", userPayload);
        }

        if (captain != null) {
            Map<String, Object> captainPayload = new HashMap<>();
            captainPayload.put("_id", captain.getId());
            captainPayload.put("fullName", captain.getFullName());
            captainPayload.put("email", captain.getEmail());
            captainPayload.put("vehicle", captain.getVehicle());
            captainPayload.put("status", captain.getStatus());
            payload.put("captain", captainPayload);
        } else {
            payload.put("captain", null);
        }

        return payload;
    }
}

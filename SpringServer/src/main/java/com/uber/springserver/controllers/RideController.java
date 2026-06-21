package com.uber.springserver.controllers;

import com.uber.springserver.config.RequestContext;
import com.uber.springserver.dto.ride.CreateRideRequest;
import com.uber.springserver.dto.ride.RideActionRequest;
import com.uber.springserver.models.Captain;
import com.uber.springserver.models.Ride;
import com.uber.springserver.models.User;
import com.uber.springserver.services.RealtimeService;
import com.uber.springserver.services.RideService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RideController {

    private final RideService rideService;
    private final RealtimeService realtimeService;

    public RideController(RideService rideService, RealtimeService realtimeService) {
        this.rideService = rideService;
        this.realtimeService = realtimeService;
    }

    @GetMapping({"/ride/get-fare", "/rides/get-fare"})
    public Object getFare(@RequestParam String pickup,
                          @RequestParam String destination,
                          @RequestParam(defaultValue = "one_way") String tripType) {
        if (pickup == null || pickup.isBlank() || destination == null || destination.isBlank()) {
            return Map.of("error", "pickup and destination required");
        }
        return rideService.calculateFare(pickup, destination, tripType);
    }

    @PostMapping({"/ride/create", "/rides/create"})
    public Object create(@RequestBody CreateRideRequest body, HttpServletRequest request) {
        User user = RequestContext.getUser(request);
        if (user == null) {
            return Map.of("message", "User not found");
        }

        String tripType = (body.getTripType() == null || body.getTripType().isBlank()) ? "one_way" : body.getTripType();

        try {
            Ride ride = rideService.createRide(user.getId(), body.getPickup(), body.getDestination(), body.getVehicleType(), tripType);
            try {
                realtimeService.broadcastToCaptains("new-ride", rideService.buildRidePayload(ride.getId()));
            } catch (Exception ignored) {
            }
            return ride;
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping({"/ride/confirm", "/rides/confirm"})
    public Object confirm(@RequestBody RideActionRequest body, HttpServletRequest request) {
        Captain captain = RequestContext.getCaptain(request);
        if (captain == null) {
            return Map.of("message", "Captain not found");
        }
        if (body.getRideId() == null || body.getRideId().isBlank()) {
            return Map.of("error", "rideId required");
        }

        try {
            Ride ride = rideService.confirmRide(body.getRideId(), captain.getId());
            try {
                realtimeService.sendToUser(ride.getUser(), "ride-confirmed", rideService.buildRidePayload(ride.getId()));
            } catch (Exception ignored) {
            }
            return ride;
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping({"/ride/start", "/rides/start-ride"})
    public Object start(@RequestParam String rideId, @RequestParam String otp) {
        if (rideId == null || rideId.isBlank() || otp == null || otp.isBlank()) {
            return Map.of("error", "rideId and otp required");
        }
        try {
            Ride ride = rideService.startRide(rideId, otp);
            try {
                realtimeService.sendToUser(ride.getUser(), "ride-started", rideService.buildRidePayload(ride.getId()));
            } catch (Exception ignored) {
            }
            return ride;
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping({"/ride/end", "/rides/end-ride"})
    public Object end(@RequestBody RideActionRequest body, HttpServletRequest request) {
        Captain captain = RequestContext.getCaptain(request);
        if (captain == null) {
            return Map.of("message", "Captain not found");
        }
        if (body.getRideId() == null || body.getRideId().isBlank()) {
            return Map.of("error", "rideId required");
        }

        try {
            Ride ride = rideService.endRide(body.getRideId(), captain.getId());
            try {
                realtimeService.sendToUser(ride.getUser(), "ride-ended", rideService.buildRidePayload(ride.getId()));
            } catch (Exception ignored) {
            }
            return ride;
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping({"/ride/pay", "/rides/pay"})
    public Object pay(@RequestBody RideActionRequest body, HttpServletRequest request) {
        User user = RequestContext.getUser(request);
        if (user == null) {
            return Map.of("message", "User not found");
        }
        if (body.getRideId() == null || body.getRideId().isBlank()) {
            return Map.of("error", "rideId required");
        }

        try {
            Ride ride = rideService.markRidePaid(body.getRideId(), user.getId());
            if (ride.getCaptain() != null && !ride.getCaptain().isBlank()) {
                try {
                    realtimeService.sendToCaptain(ride.getCaptain(), "ride-paid", rideService.buildRidePayload(ride.getId()));
                } catch (Exception ignored) {
                }
            }
            return ride;
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}

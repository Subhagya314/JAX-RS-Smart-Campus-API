package com.smartcampus.resources;

import com.smartcampus.models.SensorReading;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final DataStore dataStore = DataStore.getInstance();
    private final String sensorId;

    // The sensorId is passed in from the parent locator
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     */
    @GET
    public Response getReadings() {
        // Ensure the sensor exists
        if (!dataStore.getSensors().containsKey(sensorId)) {
            return Response.status(Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found.\"}")
                    .build();
        }

        List<SensorReading> readings = dataStore.getSensorReadings().getOrDefault(sensorId, new ArrayList<>());

        return Response.status(Status.OK)
                .entity(readings)
                .build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     */
    @POST
    public Response addReading(SensorReading newReading) {
        if (!dataStore.getSensors().containsKey(sensorId)) {
            return Response.status(Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found.\"}")
                    .build();
        }

        // Generate UUID and timestamp if not provided
        if (newReading.getId() == null || newReading.getId().isEmpty()) {
            newReading.setId(UUID.randomUUID().toString());
        }
        if (newReading.getTimestamp() == 0) {
            newReading.setTimestamp(System.currentTimeMillis());
        }

        // Thread-safe addition to the list
        dataStore.getSensorReadings().computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>()));
        dataStore.getSensorReadings().get(sensorId).add(newReading);

        // Update the sensor's current value
        dataStore.getSensors().get(sensorId).setCurrentValue(newReading.getValue());

        return Response.status(Status.CREATED)
                .entity(newReading)
                .build();
    }
}
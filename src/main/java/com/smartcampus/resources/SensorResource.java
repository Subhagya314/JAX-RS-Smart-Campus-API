package com.smartcampus.resources;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.ArrayList;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. Validates that the assigned room exists.
     */
    @POST
    public Response createSensor(Sensor newSensor) {
        // 1. Basic payload validation
        if (newSensor == null || newSensor.getId() == null || newSensor.getRoomId() == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("{\"error\":\"Sensor ID and Room ID are required.\"}")
                    .build();
        }

        // 2. Check for duplicate sensor ID
        if (dataStore.getSensors().containsKey(newSensor.getId())) {
            return Response.status(Status.CONFLICT)
                    .entity("{\"error\":\"Sensor with ID " + newSensor.getId() + " already exists.\"}")
                    .build();
        }

        // 3. Business Logic Constraint: Verify the roomId exists in the system
        Room assignedRoom = dataStore.getRooms().get(newSensor.getRoomId());
        if (assignedRoom == null) {
            return Response.status(Status.BAD_REQUEST) // 400 Bad Request is appropriate for invalid foreign keys
                    .entity("{\"error\":\"Cannot register sensor. Room ID '" + newSensor.getRoomId() + "' does not exist.\"}")
                    .build();
        }

        // 4. Save the sensor
        dataStore.getSensors().put(newSensor.getId(), newSensor);

        // 5. Update the Room's internal list of sensors (thread-safe block for compound action)
        synchronized (assignedRoom.getSensorIds()) {
            assignedRoom.getSensorIds().add(newSensor.getId());
        }

        // 6. Return success
        return Response.status(Status.CREATED)
                .entity(newSensor)
                .build();
    }

    /**
     * GET /api/v1/sensors
     * Retrieves all sensors. If the 'type' query parameter is provided (e.g., ?type=CO2),
     * it filters the results to only return sensors of that specific type.
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        // Fetch all current sensors from the thread-safe store
        List<Sensor> allSensors = new ArrayList<>(dataStore.getSensors().values());

        // If the query parameter is missing or empty, return the unfiltered list
        if (type == null || type.trim().isEmpty()) {
            return Response.status(Status.OK)
                    .entity(allSensors)
                    .build();
        }

        // If a type is provided, filter the list
        List<Sensor> filteredSensors = new ArrayList<>();
        for (Sensor sensor : allSensors) {
            // Using equalsIgnoreCase makes the API more forgiving for clients (e.g., "co2" vs "CO2")
            if (type.equalsIgnoreCase(sensor.getType())) {
                filteredSensors.add(sensor);
            }
        }

        // Return the filtered list (which could be empty if no sensors match the type)
        return Response.status(Status.OK)
                .entity(filteredSensors)
                .build();
    }

    /**
     * Sub-resource locator for Sensor Readings.
     * Delegates requests matching /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        // Pass the path parameter down to the sub-resource constructor.
        // JAX-RS will then call the appropriate HTTP method (GET, POST) on that returned instance.
        return new SensorReadingResource(sensorId);
    }
}
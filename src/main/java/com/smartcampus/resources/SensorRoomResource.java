package com.smartcampus.resources;

import com.smartcampus.models.Room;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    // Access the thread-safe singleton data store
    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a comprehensive list of all rooms.
     */
    @GET
    public Response getAllRooms() {
        // Convert the map values to a List for the JSON response array
        List<Room> roomList = new ArrayList<>(dataStore.getRooms().values());

        return Response.status(Status.OK)
                .entity(roomList)
                .build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room.
     */
    @POST
    public Response createRoom(Room newRoom) {
        // Basic validation
        if (newRoom == null || newRoom.getId() == null || newRoom.getId().trim().isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("{\"error\":\"Room ID cannot be null or empty.\"}")
                    .build();
        }

        // Check if room already exists to avoid overwriting
        if (dataStore.getRooms().containsKey(newRoom.getId())) {
            return Response.status(Status.CONFLICT)
                    .entity("{\"error\":\"Room with ID " + newRoom.getId() + " already exists.\"}")
                    .build();
        }

        // Save to the in-memory store
        dataStore.getRooms().put(newRoom.getId(), newRoom);

        // HTTP 201 Created is the standard status code for successful resource creation
        return Response.status(Status.CREATED)
                .entity(newRoom)
                .build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Fetches detailed metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);

        if (room == null) {
            // Return HTTP 404 Not Found if the room doesn't exist
            return Response.status(Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found.\"}")
                    .build();
        }

        return Response.status(Status.OK)
                .entity(room)
                .build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room, provided it has no sensors assigned to it to prevent data orphans.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        // Retrieve the room from the thread-safe store
        Room room = dataStore.getRooms().get(roomId);

        // 1. Check if the room exists
        if (room == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found.\"}")
                    .build();
        }

        // 2. Business Logic Constraint: Check for orphaned sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            return Response.status(Status.CONFLICT) // 409 Conflict
                    .entity("{\"error\":\"Cannot delete room '" + roomId + "'. It still has "
                            + room.getSensorIds().size() + " sensor(s) assigned to it.\"}")
                    .build();
        }

        // 3. Safe to delete
        dataStore.getRooms().remove(roomId);

        // HTTP 204 No Content is standard for a successful DELETE that returns no body
        return Response.status(Status.NO_CONTENT).build();
    }
}
package com.smartcampus.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;

/**
 * Root discovery endpoint providing metadata about the Smart Campus API.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiMetadata() {
        // Main container for our JSON response
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiName", "Smart Campus API");
        metadata.put("version", "1.0.0");
        metadata.put("description", "Core infrastructure API for campus facilities and sensor management.");

        // Administrative contact details
        Map<String, String> contact = new HashMap<>();
        contact.put("role", "Lead Backend Architect");
        contact.put("email", "subhagya.20240133@iit.ac.lk");
        metadata.put("contact", contact);

        // Map of primary resource collections
        Map<String, String> collections = new HashMap<>();
        collections.put("rooms", "/api/v1/rooms");
        collections.put("sensors", "/api/v1/sensors");
        metadata.put("collections", collections);

        // Build and return the HTTP response using explicit status codes
        return Response.status(Status.OK).entity(metadata).build();
    }
}
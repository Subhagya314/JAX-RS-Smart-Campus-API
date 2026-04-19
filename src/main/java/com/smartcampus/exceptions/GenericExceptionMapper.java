package com.smartcampus.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * A catch-all global interceptor for any unhandled runtime exceptions.
 * Prevents stack traces from leaking to the client and ensures consistent JSON error formatting.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {

        // Return a generic, safe message to the client
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"An unexpected internal server error occurred. Please try again later or contact support.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
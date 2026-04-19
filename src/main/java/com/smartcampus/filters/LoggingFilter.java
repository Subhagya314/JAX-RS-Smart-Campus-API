package com.smartcampus.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * A global JAX-RS filter to observe and log all incoming API requests and outgoing responses.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Initialize the standard Java logger
    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Intercepts the INCOMING request before it reaches the resource controller.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();

        LOGGER.info(">>> Incoming Request: " + method + " " + uri);
    }

    /**
     * Intercepts the OUTGOING response after the resource controller has finished executing.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        int status = responseContext.getStatus();

        LOGGER.info("<<< Outgoing Response: " + method + " /" + path + " | HTTP Status: " + status);
    }
}
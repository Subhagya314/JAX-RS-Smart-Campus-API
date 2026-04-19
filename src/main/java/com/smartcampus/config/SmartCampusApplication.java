package com.smartcampus.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Defines the base URI for all resource URIs.
 * This sets the versioned entry point for the Smart Campus API.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
}

# Smart Campus API

A RESTful API for managing smart campus infrastructure — tracking rooms, sensors, and real-time sensor readings across a university campus. Built with **JAX-RS** running on an embedded **Grizzly HTTP server**, requiring no external application server.

---

## Author

**Subhagya** — [GitHub Profile](https://github.com/Subhagya314)

**UID** — w2153365/20240133   

---

*Developed as coursework for the 5COSC022C Client-Server Architectures module.*


## Table of Contents

- [Overview](#overview)
- [API Design](#api-design)
  - [Resource Hierarchy](#resource-hierarchy)
  - [Endpoints Reference](#endpoints-reference)
  - [Data Models](#data-models)
  - [Error Handling](#error-handling)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Build & Run](#build--run)
- [Sample curl Commands](#sample-curl-commands)
- [Tech Stack](#tech-stack)

---

## Overview

The Smart Campus API provides a unified HTTP interface for managing a campus's physical infrastructure. It supports three core resource types:

- **Rooms** — Physical spaces on campus (labs, lecture halls, offices), each identified by a unique ID and tracking which sensors are installed inside them.
- **Sensors** — Hardware devices assigned to rooms that measure environmental data (e.g. temperature, CO₂, humidity). Each sensor has a `type`, a `status` (`ACTIVE` or `MAINTENANCE`), and a `currentValue` that is updated with every new reading.
- **Sensor Readings** — Individual timestamped data points submitted by a sensor. Readings are blocked if the sensor is in `MAINTENANCE` status.

All data is stored **in-memory** using a thread-safe singleton `DataStore`. All request/response bodies use **JSON**. Every incoming request and outgoing response is logged globally by a `LoggingFilter`.

---

## API Design

### Resource Hierarchy

The API is versioned under `/api/v1` and follows a flat resource structure:

```
/api/v1/
 ├── GET                            → Discovery: API metadata & available collections
 ├── /rooms
 │    ├── GET                       → List all rooms
 │    ├── POST                      → Create a room
 │    └── /{roomId}
 │         ├── GET                  → Get a specific room
 │         └── DELETE               → Delete a room (only if it has no sensors)
 └── /sensors
      ├── GET  (?type=)             → List all sensors (optional filter by type)
      ├── POST                      → Create a sensor (linked room must exist)
      └── /{sensorId}
           └── /readings
                ├── GET             → List all readings for a sensor
                └── POST            → Submit a new reading (sensor must be ACTIVE)
```

### Endpoints Reference

#### Discovery

| Method | Path       | Description                                   | Success  |
|--------|------------|-----------------------------------------------|----------|
| `GET`  | `/api/v1/` | Returns API name, version, and resource links  | `200 OK` |

#### Rooms — `/api/v1/rooms`

| Method   | Path                     | Description                                       | Success          |
|----------|--------------------------|---------------------------------------------------|------------------|
| `GET`    | `/api/v1/rooms`          | Retrieve a list of all rooms                      | `200 OK`         |
| `POST`   | `/api/v1/rooms`          | Create a new room                                 | `201 Created`    |
| `GET`    | `/api/v1/rooms/{roomId}` | Retrieve a specific room by ID                    | `200 OK`         |
| `DELETE` | `/api/v1/rooms/{roomId}` | Delete a room (fails if sensors are still present)| `204 No Content` |

#### Sensors — `/api/v1/sensors`

| Method | Path              | Description                                                  | Success       |
|--------|-------------------|--------------------------------------------------------------|---------------|
| `GET`  | `/api/v1/sensors` | Retrieve all sensors. Filter by `?type=` query param         | `200 OK`      |
| `POST` | `/api/v1/sensors` | Register a new sensor (linked `roomId` must exist)           | `201 Created` |

#### Sensor Readings — `/api/v1/sensors/{sensorId}/readings`

| Method | Path                                  | Description                                            | Success       |
|--------|---------------------------------------|--------------------------------------------------------|---------------|
| `GET`  | `/api/v1/sensors/{sensorId}/readings` | Retrieve all readings for a sensor                     | `200 OK`      |
| `POST` | `/api/v1/sensors/{sensorId}/readings` | Submit a new reading (sensor must not be MAINTENANCE)  | `201 Created` |

### Data Models

#### Room
```json
{
  "id": "room-101",
  "name": "Lecture Hall A",
  "capacity": 120,
  "sensorIds": ["sensor-001", "sensor-002"]
}
```

| Field       | Type           | Description                                    |
|-------------|----------------|------------------------------------------------|
| `id`        | `String`       | Unique identifier for the room. **Required.**  |
| `name`      | `String`       | Human-readable name of the room                |
| `capacity`  | `int`          | Maximum occupancy of the room                  |
| `sensorIds` | `List<String>` | Auto-managed list of sensor IDs in this room   |

#### Sensor
```json
{
  "id": "sensor-001",
  "type": "TEMPERATURE",
  "status": "ACTIVE",
  "currentValue": 22.5,
  "roomId": "room-101"
}
```

| Field          | Type     | Description                                                         |
|----------------|----------|---------------------------------------------------------------------|
| `id`           | `String` | Unique identifier for the sensor. **Required.**                     |
| `type`         | `String` | Sensor category (e.g. `TEMPERATURE`, `CO2`, `HUMIDITY`, `MOTION`)  |
| `status`       | `String` | `ACTIVE` or `MAINTENANCE`. Readings are blocked if `MAINTENANCE`.   |
| `currentValue` | `double` | Latest recorded value. Auto-updated on each new reading.            |
| `roomId`       | `String` | ID of the room this sensor belongs to. **Required.**                |

#### SensorReading
```json
{
  "id": "a3f1c9d2-...",
  "value": 22.5,
  "timestamp": 1713692400000
}
```

| Field       | Type     | Description                                                           |
|-------------|----------|-----------------------------------------------------------------------|
| `id`        | `String` | Auto-generated UUID if not provided in the request body               |
| `value`     | `double` | The recorded sensor measurement                                       |
| `timestamp` | `long`   | Unix epoch milliseconds. Auto-set to current time if not provided     |

### Error Handling

The API uses custom exception mappers to return consistent JSON error bodies for all failure cases:

| Exception                         | HTTP Status               | Trigger                                                       |
|-----------------------------------|---------------------------|---------------------------------------------------------------|
| `RoomNotEmptyException`           | `409 Conflict`            | Deleting a room that still has sensors assigned               |
| `LinkedResourceNotFoundException` | `400 Bad Request`         | Creating a sensor with a `roomId` that doesn't exist          |
| `SensorUnavailableException`      | `403 Forbidden`           | Submitting a reading to a sensor in `MAINTENANCE` status      |
| `GenericExceptionMapper`          | `500 Internal Server Error` | Any other unhandled runtime exception                       |

All error responses follow this consistent format:
```json
{
  "error": "A descriptive message explaining what went wrong."
}
```

---

## Project Structure

```
JAX-RS-Smart-Campus-API/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── smartcampus/
                    ├── Main.java                                      # Entry point — starts Grizzly2 on port 8080
                    ├── config/
                    │   └── SmartCampusApplication.java               # Sets @ApplicationPath to /api/v1
                    ├── models/
                    │   ├── Room.java                                  # Room entity (id, name, capacity, sensorIds)
                    │   ├── Sensor.java                                # Sensor entity (id, type, status, currentValue, roomId)
                    │   └── SensorReading.java                        # Reading entity (id, value, timestamp)
                    ├── repository/
                    │   └── DataStore.java                            # Thread-safe singleton in-memory data store
                    ├── resources/
                    │   ├── DiscoveryResource.java                    # GET /api/v1/ — API metadata
                    │   ├── SensorRoomResource.java                   # /api/v1/rooms — CRUD for rooms
                    │   ├── SensorResource.java                       # /api/v1/sensors — CRUD for sensors
                    │   └── SensorReadingResource.java                # /api/v1/sensors/{id}/readings (sub-resource locator)
                    ├── exceptions/
                    │   ├── RoomNotEmptyException.java                # Thrown when deleting a room with sensors
                    │   ├── RoomNotEmptyExceptionMapper.java          # Maps to 409 Conflict
                    │   ├── LinkedResourceNotFoundException.java      # Thrown when a linked room doesn't exist
                    │   ├── LinkedResourceNotFoundExceptionMapper.java # Maps to 400 Bad Request
                    │   ├── SensorUnavailableException.java           # Thrown when sensor is in MAINTENANCE
                    │   ├── SensorUnavailableExceptionMapper.java     # Maps to 403 Forbidden
                    │   └── GenericExceptionMapper.java               # Catch-all, maps to 500 Internal Server Error
                    └── filters/
                        └── LoggingFilter.java                        # Logs all incoming requests & outgoing responses
```

---

## Prerequisites

Ensure the following are installed before building:

| Tool         | Minimum Version | Verify With     |
|--------------|-----------------|-----------------|
| Java JDK     | 11              | `java -version` |
| Apache Maven | 3.6+            | `mvn -version`  |

---

## Build & Run

### Step 1 — Clone the Repository

```bash
git clone https://github.com/Subhagya314/JAX-RS-Smart-Campus-API.git
cd JAX-RS-Smart-Campus-API
```

### Step 2 — Install Dependencies & Compile

```bash
mvn clean compile
```

Maven will automatically download Jersey, Grizzly2, and all other declared dependencies. You should see `BUILD SUCCESS` at the end.

### Step 3 — Package into a JAR

```bash
mvn package -DskipTests
```

This produces `target/smart-campus-api-1.0-SNAPSHOT.jar`.

### Step 4 — Start the Server

```bash
mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
```

You should see the following output in your terminal:

```
Smart Campus API started with endpoints available at http://localhost:8080/api/v1/
Hit enter to stop it...
```

### Step 5 — Verify the Server is Running

Open a **new terminal** and run:

```bash
curl -s http://localhost:8080/api/v1/
```

You should receive the API discovery metadata as a JSON response. The server is ready.

> **Base URL:** `http://localhost:8080/api/v1`

To stop the server, press **Enter** in the terminal where it is running.

---

## Sample curl Commands

> All examples assume the server is running at `http://localhost:8080/api/v1`.

---

### 1. Discover the API

Fetch API metadata including the version, contact info, and available resource collection paths:

```bash
curl -s -X GET http://localhost:8080/api/v1/
```

**Expected response (`200 OK`):**
```json
{
  "apiName": "Smart Campus API",
  "version": "1.0.0",
  "description": "Core infrastructure API for campus facilities and sensor management.",
  "contact": {
    "role": "Lead Backend Architect",
    "email": "subhagya.20240133@iit.ac.lk"
  },
  "collections": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Create a Room

Register a new campus room with a name and capacity:

```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "room-101",
    "name": "Lecture Hall A",
    "capacity": 120
  }'
```

**Expected response (`201 Created`):**
```json
{
  "id": "room-101",
  "name": "Lecture Hall A",
  "capacity": 120,
  "sensorIds": []
}
```

---

### 3. Register a Sensor in a Room

Add a temperature sensor and assign it to `room-101`. The room must already exist:

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "sensor-001",
    "type": "TEMPERATURE",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "room-101"
  }'
```

**Expected response (`201 Created`):**
```json
{
  "id": "sensor-001",
  "type": "TEMPERATURE",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "room-101"
}
```

> The room's `sensorIds` list is automatically updated to include `"sensor-001"`.

---

### 4. Submit a Sensor Reading

Record a temperature reading from `sensor-001`. The sensor must have `status: ACTIVE`:

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/sensor-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 22.5
  }'
```

**Expected response (`201 Created`):**
```json
{
  "id": "a3f1c9d2-4b8e-11ee-be56-0242ac120002",
  "value": 22.5,
  "timestamp": 1713692400000
}
```

> `id` is auto-generated as a UUID and `timestamp` is auto-set to the current system time in milliseconds when not provided. The sensor's `currentValue` is also updated to `22.5`.

---

### 5. Retrieve All Readings for a Sensor

Fetch the full reading history recorded by `sensor-001`:

```bash
curl -s -X GET http://localhost:8080/api/v1/sensors/sensor-001/readings
```

**Expected response (`200 OK`):**
```json
[
  {
    "id": "a3f1c9d2-4b8e-11ee-be56-0242ac120002",
    "value": 22.5,
    "timestamp": 1713692400000
  }
]
```

---

### 6. Filter Sensors by Type

Retrieve only `CO2` sensors using the `?type=` query parameter (case-insensitive — `co2`, `CO2`, and `Co2` all work):

```bash
curl -s -X GET "http://localhost:8080/api/v1/sensors?type=TEMPERATURE"
```

**Expected response (`200 OK`):**
```json
[
  {
    "id": "sensor-001",
    "type": "TEMPERATURE",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "room-101"
  }
]
```

---

### 7. Attempt to Delete a Room with Sensors (409 Error Demo)

Try to delete `room-101` while it still has sensors — this demonstrates the `RoomNotEmptyException` (→ `409 Conflict`):

```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/room-101
```

**Expected response (`409 Conflict`):**
```json
{
  "error": "Cannot delete room 'room-101'. It is currently occupied by active hardware."
}
```

---

### 8. Submit a Reading to a Sensor in Maintenance (403 Error Demo)

Register a sensor with `MAINTENANCE` status, then try to submit a reading — this demonstrates the `SensorUnavailableException` (→ `403 Forbidden`):

```bash
# Step 1: Register a sensor in MAINTENANCE
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "sensor-002",
    "type": "CO2",
    "status": "MAINTENANCE",
    "currentValue": 0.0,
    "roomId": "room-101"
  }'

# Step 2: Attempt to submit a reading — this will be blocked
curl -s -X POST http://localhost:8080/api/v1/sensors/sensor-002/readings \
  -H "Content-Type: application/json" \
  -d '{ "value": 450.0 }'
```

**Expected response (`403 Forbidden`):**
```json
{
  "error": "Sensor 'sensor-002' is currently in MAINTENANCE mode and cannot accept new readings."
}
```

---

## Tech Stack

| Technology                    | Version | Role                                        |
|-------------------------------|---------|---------------------------------------------|
| Java                          | 11      | Language & runtime                          |
| JAX-RS (Jersey)               | 3.1.5   | REST API framework                          |
| Grizzly2 HTTP Server          | 3.1.5   | Embedded HTTP server (no app server needed) |
| HK2                           | 3.1.5   | Dependency injection                        |
| Jakarta JSON Binding (JSON-B) | —       | JSON serialisation / deserialisation        |
| Apache Maven                  | 3.6+    | Build & dependency management               |

---

package com.smartcampus.repository;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton repository acting as our in-memory database.
 */
public class DataStore {
    private static final DataStore instance = new DataStore();

    // Thread-safe wrapper around the standard HashMap
    private final Map<String, Room> rooms = Collections.synchronizedMap(new HashMap<>());

    private DataStore() {
        // Private constructor to prevent instantiation
    }

    public static DataStore getInstance() {
        return instance;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    private final Map<String, Sensor> sensors = Collections.synchronizedMap(new HashMap<>());

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    private final Map<String, List<SensorReading>> sensorReadings = Collections.synchronizedMap(new HashMap<>());

    public Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }

}
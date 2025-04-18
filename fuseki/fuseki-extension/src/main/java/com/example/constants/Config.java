package com.example.constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

@Getter
public class Config {
  private static final Logger logger = LoggerFactory.getLogger(Config.class);

  public static final String API_URL;
  public static final String DRONE_PORT_API_URL;
  public static final String AIRWAY_RESERVATION_API_URL;
  public static final String AIRWAY_RESERVATION_AUTH_HEADER_NAME;
  public static final String AIRWAY_RESERVATION_API_KEY;

  public static final Integer CACHE_TIME;
  public static final Map<String, Object> DRONE_PORT_DEFAULT_PARAMETERS = new HashMap<>();
  public static final Map<String, Object> AIRWAY_DEFAULT_PARAMETERS = new HashMap<>();

  static {
    Properties prop = new Properties();
    try (InputStream input = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
      if (input == null) {
        logger.error("Unable to locate the application.properties file.");
        System.exit(1);
      }
      prop.load(input);
    } catch (IOException ex) {
      logger.error("IOException occurred while loading properties", ex);
      System.exit(1);
    }

    /*
     * Reading property variables.
     */
    CACHE_TIME = prop.containsKey("config.cache_time") ? Integer.valueOf(prop.getProperty("config.cache_time")) : null;
    AIRWAY_RESERVATION_AUTH_HEADER_NAME = prop.getProperty("externalApi.airwayReservation.auth.headerName");


    // Load default parameters for Drone Port Reservation from properties
    final String dronePortReservationPrefix = "externalApi.dronePortReservation.defaultParameter.";
    for (String key : prop.stringPropertyNames()) {
      if (key.startsWith(dronePortReservationPrefix)) {
        String subKey = key.substring(dronePortReservationPrefix.length());
        DRONE_PORT_DEFAULT_PARAMETERS.put(subKey, prop.getProperty(key));
      }
    }

    // Load default parameters for Airway Reservation from properties
    final String airwayReservationPrefix = "externalApi.airwayReservation.defaultParameter.";
    for (String key : prop.stringPropertyNames()) {
      if (key.startsWith(airwayReservationPrefix)) {
        String subKey = key.substring(airwayReservationPrefix.length());
        AIRWAY_DEFAULT_PARAMETERS.put(subKey, prop.getProperty(key));
      }
    }

    /*
     * Reading environment variables.
     */
    API_URL = System.getenv("API_URL");
    DRONE_PORT_API_URL = System.getenv("DRONE_PORT_API_URL");
    AIRWAY_RESERVATION_API_URL = System.getenv("AIRWAY_RESERVATION_API_URL");
    AIRWAY_RESERVATION_API_KEY = System.getenv("AIRWAY_RESERVATION_API_KEY");

    /*
     * Check the existence of each value.
     */
    if (API_URL == null || API_URL.isEmpty()) {
      logger.error("API_URL is not set. Stopping application.");
      System.exit(1);
    } else {
      logger.info("API_URL: {}", API_URL);
    }

    if (DRONE_PORT_API_URL == null || DRONE_PORT_API_URL.isEmpty()) {
      logger.error("DRONE_PORT_API_URL is not set. Stopping application.");
      System.exit(1);
    } else {
      logger.info("DRONE_PORT_API_URL: {}", DRONE_PORT_API_URL);
    }

    if (AIRWAY_RESERVATION_API_URL == null || AIRWAY_RESERVATION_API_URL.isEmpty()) {
      logger.error("AIRWAY_RESERVATION_API_URL is not set. Stopping application.");
      System.exit(1);
    } else {
      logger.info("AIRWAY_RESERVATION_API_URL: {}", AIRWAY_RESERVATION_API_URL);
    }

    if (CACHE_TIME == null || CACHE_TIME < 0) {
      logger.error("CACHE_TIME is not set or invalid. Stopping application.");
      System.exit(1);
    } else {
      logger.info("CACHE_TIME: {}", CACHE_TIME);
    }

    if (DRONE_PORT_DEFAULT_PARAMETERS == null || DRONE_PORT_DEFAULT_PARAMETERS.size() == 0) {
      logger.error("DRONE_PORT_DEFAULT_PARAMETERS is not set. Stopping application.");
      System.exit(1);
    } else {
      logger.info("DRONE_PORT_DEFAULT_PARAMETERS: {}", DRONE_PORT_DEFAULT_PARAMETERS);
    }

    if (AIRWAY_DEFAULT_PARAMETERS == null || AIRWAY_DEFAULT_PARAMETERS.size() == 0) {
      logger.error("AIRWAY_DEFAULT_PARAMETERS is not set. Stopping application.");
      System.exit(1);
    } else {
      logger.info("AIRWAY_DEFAULT_PARAMETERS: {}", AIRWAY_DEFAULT_PARAMETERS);
    }

    if (AIRWAY_RESERVATION_AUTH_HEADER_NAME == null || AIRWAY_RESERVATION_AUTH_HEADER_NAME.isEmpty()) {
      logger.error("AIRWAY_RESERVATION_AUTH_HEADER_NAME is not set. Stopping application.");
      System.exit(1);
    } else {
      logger.info("AIRWAY_RESERVATION_AUTH_HEADER_NAME: {}", AIRWAY_RESERVATION_AUTH_HEADER_NAME);
    }

    if (AIRWAY_RESERVATION_API_KEY == null || AIRWAY_RESERVATION_API_KEY.isEmpty()) {
      logger.error("AIRWAY_RESERVATION_API_KEY is not set. Stopping application.");
      System.exit(1);
    }
  }
}

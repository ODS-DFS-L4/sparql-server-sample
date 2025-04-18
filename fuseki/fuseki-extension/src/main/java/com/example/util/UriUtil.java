package com.example.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.constants.Config;

public class UriUtil {
  private static final Logger logger = LoggerFactory.getLogger(UriUtil.class);

  public static Map<String, Object> setRequestParametersFromURI(String serviceURI) {
    Map<String, Object> parameters = new HashMap<>();
    String query = "";
    if (serviceURI.startsWith("custom:DronePortReservation")) {
      parameters = new HashMap<>(Config.DRONE_PORT_DEFAULT_PARAMETERS);
      query = serviceURI.replaceFirst("custom:DronePortReservation\\?", "");
    } else if (serviceURI.startsWith("custom:AirwayReservation")) {
      parameters = new HashMap<>(Config.AIRWAY_DEFAULT_PARAMETERS);
      query = serviceURI.replaceFirst("custom:AirwayReservation\\?", "");
    } else {
      logger.error("Service URI does not match any known patterns: {}", serviceURI);
      return parameters;
    }

    try {
      if (query == null || query.isEmpty() || !query.contains("=")) {
        return parameters;
      }

      logger.debug("Query string: {}", query);
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        String[] keyValue = pair.split("=");
        if (keyValue.length == 2) {
          String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
          String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
          logger.debug("Decoded key: {}, value: {}", key, value);
          if (parameters.containsKey(key) && value.length() > 0) {
            parameters.put(key, value);
            logger.debug("Parameter updated: {} = {}", key, value);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Failed to parsing URI", e);
    }
    return parameters;
  }
}

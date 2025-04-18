package com.example.sparqlservice.constants;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class DronePortParameter {
  private final Map<String, Object> parameters = new HashMap<>();

  private final String minLat;
  private final String minLon;
  private final String maxLat;
  private final String maxLon;

  public DronePortParameter(
      @Value("${drone-port.api.requiredParameter.minLat}") String minLat,
      @Value("${drone-port.api.requiredParameter.minLon}") String minLon,
      @Value("${drone-port.api.requiredParameter.maxLat}") String maxLat,
      @Value("${drone-port.api.requiredParameter.maxLon}") String maxLon) {

    if (minLat == null || minLon == null || maxLat == null || maxLon == null) {
        throw new IllegalArgumentException("Latitude and longitude parameters cannot be null");

    }
    this.minLat = minLat;
    this.minLon = minLon;
    this.maxLat = maxLat;
    this.maxLon = maxLon;
    parameters.put("minLat", this.minLat);
    parameters.put("minLon", this.minLon);
    parameters.put("maxLat", this.maxLat);
    parameters.put("maxLon", this.maxLon);
  }

  public String getQueryParameter() {
    return "?" + parameters.entrySet().stream()
        .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));
  }
}
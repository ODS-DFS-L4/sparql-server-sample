package com.example.sparqlservice.service.externalApi;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DroneHighwayApiService {
  private final ExternalAPIService externalAPIService;

  @Value("${drone-highway.api.url}")
  private String droneHighwayApiUrl;

  // Number of response patterns returned from mock server
  // private final int NUMBER_OF_RESPONSE_PATTERNS = 2;

  /**
   * SYNCHRONOUS
   *
   * Get all airway information
   *
   * @return Optional<AirwayResponseDTO> Response from drone highway API
   */
  public Optional<AirwayResponseDTO> fetchAllAirways() {
    // Generate a random number to select a response pattern for the mock server

    String path = "/airway";
    String allParameter = "?all=true";
    String parameterForPrism = "__example=allAirwaySuccess1";

    String pathAndQuery = path + allParameter + "&" + parameterForPrism;

    try {
      return Optional.ofNullable(externalAPIService.callExternalApiSync(
          droneHighwayApiUrl,
          pathAndQuery,
          HttpMethod.GET,
          null,
          AirwayResponseDTO.class));

    } catch (Exception e) {
      log.error("Error getting Airways synchronously", e);
      return Optional.empty();
    }
  }

  /**
   * Retrieves specific airway information using the provided airway ID.
   *
   * @param airwayId The unique identifier of the airway.
   * @return An Optional containing the AirwayResponseDTO object.
   */
  public Optional<AirwayResponseDTO> fetchAirwayByAirwayId(@NonNull String airwayId) {
    String path = "/airway";
    String airwayIdQuery = "?all=false&airwayId=" + airwayId;
    String parameterForPrism = "__example=" + airwayId;

    String pathAndQuery = path + airwayIdQuery + "&" + parameterForPrism;
    try {
      return Optional.ofNullable(externalAPIService.callExternalApiSync(
          droneHighwayApiUrl,
          pathAndQuery,
          HttpMethod.GET,
          null,
          AirwayResponseDTO.class));
    } catch (Exception e) {
      log.error("Error getting airway by airwayId: {}", airwayId, e);
      return Optional.empty();
    }
  }

}

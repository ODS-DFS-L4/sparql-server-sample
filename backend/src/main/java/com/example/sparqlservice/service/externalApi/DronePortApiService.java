package com.example.sparqlservice.service.externalApi;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.sparqlservice.DTO.response.dronePort.DronePortResponseDTO;
import com.example.sparqlservice.constants.DronePortParameter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DronePortApiService {
  private final ExternalAPIService externalAPIService;
  private final DronePortParameter dronePortParameter;

  private final String DRONE_PORT_API_URL;

  public DronePortApiService(@Value("${drone-port.api.url}") String dronePortApiUrl,
      ExternalAPIService externalApiService, DronePortParameter dronePortParameter) {
    if (dronePortApiUrl == null) {
      throw new RuntimeException("Drone Port API URL cannot be null");
    }
    this.DRONE_PORT_API_URL = dronePortApiUrl;
    this.externalAPIService = externalApiService;
    this.dronePortParameter = dronePortParameter;
  }

  /**
   * Fetches a DronePort by its unique identifier.
   *
   * @param dronePortId the unique identifier of the drone port
   * @return an Optional containing DronePortResponseDTO if successful, or an empty Optional if an error occurs
   */
  public Optional<DronePortResponseDTO> fetchDronePortByDronePortId(@NonNull String dronePortId) {
    String pathWithDronePortId = "/droneport/info/detail/" + dronePortId;
    // String parameterForPrism = "__example=singleDronePortSuccess1";

    String pathAndQuery = pathWithDronePortId;
    try {
      return Optional.ofNullable(externalAPIService.callExternalApiSync(
          DRONE_PORT_API_URL,
          pathAndQuery,
          HttpMethod.GET,
          null,
          DronePortResponseDTO.class));
    } catch (Exception e) {
      log.error("Error getting DronePort by dronePortId: {}", dronePortId, e);
      return Optional.empty();
    }
  }

  /**
   * Fetches all DronePorts from the external API.
   *
   */
  public Optional<DronePortResponseDTO[]> fetchAllDronePorts() {
    final String path = "/droneport/info/list";
    final String queryParameter = dronePortParameter.getQueryParameter();

    try {
       return Optional.ofNullable(externalAPIService.callExternalApiSync(
          DRONE_PORT_API_URL,
          path + queryParameter,
          HttpMethod.GET,
          null,
          DronePortResponseDTO[].class));

    } catch (Exception e) {
      log.error("Error getting all DronePorts", e);
      return Optional.empty();
    }
  }
}

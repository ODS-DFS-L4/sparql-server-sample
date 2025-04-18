package com.example.sparqlservice.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.sparqlservice.DTO.response.dronePort.DronePortResponseDTO;
import com.example.sparqlservice.converter.impl.DronePortConverter;
import com.example.sparqlservice.service.externalApi.DronePortApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DronePortService {
  private final DronePortApiService dronePortApiService;
  private final DronePortConverter dronePortConverter;

  /**
   * This method is currently unused.
   *
   * @see #fetchAndConvertDronePortByDronePortIdList(List)
   *
   *      Fetches and converts multiple DronePort data by their IDs from an
   *      external
   *      API into a single RDF Model.
   *
   *      This process involves making API requests for each dronePortId.
   *
   * @param dronePortIds a list of drone port IDs to fetch and convert
   * @return a combined RDF Model containing the data of all specified drone ports
   * @throws InterruptedException
   */
  public Model fetchAndConvertMultipleDronePortsByDronePortIds(@NonNull List<String> dronePortIds)
      throws InterruptedException {
    Model dronePortModel = ModelFactory.createDefaultModel();
    if (dronePortIds.size() == 0) {
      return dronePortModel;
    }

    // Warning: This method may trigger a large number of API requests to the
    // external service in a short time.
    for (String dronePortId : dronePortIds) {
      dronePortModel = ModelFactory.createUnion(dronePortModel, fetchAndConvertDronePortByDronePortId(dronePortId));
    }
    return dronePortModel;
  }

  /**
   * Fetches and converts DronePort data from an external API into an RDF Model
   * using a single API call.
   *
   * @param dronePortIds list of drone port IDs to filter the data
   * @return RDF Model of the specified drone ports
   */
  public Model fetchAndConvertDronePortByDronePortIdList(@NonNull List<String> dronePortIds) {
    log.debug("Starting fetchAndConvertDronePortByDronePortIdList process for dronePortIds = {}", dronePortIds);
    Model dronePortsModel = ModelFactory.createDefaultModel();

    // First, retrieve all DronePorts from the external API.
    Optional<DronePortResponseDTO[]> responseOptional = dronePortApiService.fetchAllDronePorts();
    if (responseOptional.isEmpty() || responseOptional.get().length == 0) {
      log.error("Error: DronePort API returned an empty response.");
      return dronePortsModel;
    }
    final List<DronePortResponseDTO> responseDTO = List.of(responseOptional.get());

    // Filter DronePorts that match their dronePortId to the given dronePortId list.
    final List<DronePortResponseDTO> filteredResponseDTO = responseDTO.stream()
        .filter(dronePort -> dronePortIds.contains(dronePort.getDronePortId())).collect(Collectors.toList());

    try {
      log.debug("Attempting to convert fetched DronePort data to RDF model for the provided dronePortIds.");
      for (var dronePortDto : filteredResponseDTO) {
        dronePortsModel = ModelFactory.createUnion(dronePortsModel, dronePortConverter.convert(dronePortDto));
      }
    } catch (Exception e) {
      log.error("Error converting DronePort data to RDF model: {}", e.getMessage());
      throw e;
    }

    return dronePortsModel;
  }

  /**
   * This method is currently unused.
   *
   * Fetches DronePort data by dronePortId from an external API, converts it into
   * an RDF Model, and then returns it.
   *
   * @param dronePortId
   * @return
   */
  private Model fetchAndConvertDronePortByDronePortId(@NonNull String dronePortId) {
    log.debug("Starting fetchAndConvertDronePortByDronePortId process for dronePortId = {}", dronePortId);
    Optional<DronePortResponseDTO> responseOptional = dronePortApiService.fetchDronePortByDronePortId(dronePortId);
    if (responseOptional.isEmpty()) {
      log.warn("Error: Unable to retrieve a DronePort : dronePortId = {}", dronePortId);
      return null;
    }

    try {
      log.debug("Attempting to convert fetched DronePort data to RDF model. dronePortId = {}", dronePortId);
      return dronePortConverter.convert(responseOptional.get());
    } catch (Exception e) {
      log.error("Error converting DronePort data to RDF model: {}", e.getMessage());
      throw e;
    }
  }

}
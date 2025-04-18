package com.example.sparqlservice.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO;
import com.example.sparqlservice.constants.AirwayNotificationStatus;
import com.example.sparqlservice.converter.impl.AirwayConverter;
import com.example.sparqlservice.repository.DroneHighwayRepository;
import com.example.sparqlservice.repository.GeneralRdfRepository;
import com.example.sparqlservice.service.externalApi.DroneHighwayApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for handling RDF data operations.
 * Provides functionality to create and manage RDF models for different entity
 * types.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RdfService {

  private final DroneHighwayRepository droneHighwayRepository;
  private final AirwayConverter airwayConverter;
  private final DroneHighwayApiService droneHighwayService;
  private final GeneralRdfRepository generalRdfRepository;
  private final DronePortService dronePortService;

  /**
   * Handles airway notifications based on the provided status.
   *
   * @param airwayId the unique identifier of the airway
   * @param status   the status of the airway notification
   */
  public void handleAirwayNotification(@NonNull String airwayId, @NonNull AirwayNotificationStatus status) {
    log.debug("Handling airway notification for airwayId = {} with status = {}", airwayId, status);
    switch (status) {
      case CREATE_OR_UPDATE:
        fetchAndInsertAirwayByAirwayId(airwayId);
        break;
      case DELETE:
        deleteAirwayByAirwayId(airwayId);
        break;
      default:
        log.error("Unhandled AirwayNotificationStatus: {}", status);
    }
  }

  /**
   * Fetches airway data by airwayId from an external API and inserts it into the
   * RDF store.
   */
  public void fetchAndInsertAirwayByAirwayId(@NonNull String airwayId) {
    log.debug("Starting fetchAndInsertAirwayByAirwayId process for airwayId = {}", airwayId);
    Optional<AirwayResponseDTO> responseOptional = droneHighwayService.fetchAirwayByAirwayId(airwayId);
    if (responseOptional.isEmpty()) {
      log.warn("Error: Unable to retrieve a Airway : airwayId = {}", airwayId);
      return;
    }

    log.debug("NumberOfAirways = {}", responseOptional.get().getAirway().getAirways().size());
    log.debug("DronePortIds: {}", responseOptional.get().getAllDronePortIds());

    log.debug("Attempting to convert fetched airway data to RDF model. airwayId = {}", airwayId);
    AirwayResponseDTO response = responseOptional.get();
    try {
      Model airwayModel = airwayConverter.convert(response);

      List<String> dronePortIds = response.getAllDronePortIds();
      Model dronePortsModel = ModelFactory.createDefaultModel();
      if (dronePortIds != null && dronePortIds.size() > 0) {
        dronePortsModel = dronePortService.fetchAndConvertDronePortByDronePortIdList(dronePortIds);
      }
      Model unionModel = ModelFactory.createUnion(airwayModel, dronePortsModel);

      droneHighwayRepository.updateAirwayAndDronePortByAirwayIdAndDronePortIds(airwayId, dronePortIds, unionModel);
      log.info("fetchAndInsertAirwayByAirwayId: successfully inserted airway data for airwayId = {}", airwayId);
    } catch (Exception e) {
      log.error("Error occurred while inserting airway data for airwayId = {}: {}", airwayId, e.getMessage());
    }
  }

  /**
   * Fetches ALL airway data from an external API and replaces the existing data
   * in the RDF store.
   */
  public void fetchAndReplaceAllAirways() {
    log.debug("Starting fetchAndReplaceAllAirways process.");
    Optional<AirwayResponseDTO> responseOptional = droneHighwayService.fetchAllAirways();
    if (responseOptional.isEmpty()) {
      log.warn("No airway data retrieved from the external API.");
      return;
    }

    log.debug("NumberOfAirways = {}", responseOptional.get().getAirway().getAirways().size());

    try {
      log.debug("Attempting to convert fetched airway data to RDF model.");
      Model model = airwayConverter.convert(responseOptional.get());
      droneHighwayRepository.replaceAllAirways(model);
      log.info("fetchAndReplaceAllAirways: successfully replaced all airway data in the RDF store.");
    } catch (Exception e) {
      log.error("Error occurred while replacing all airway data: {}", e.getMessage());
    }
  }

  /**
   * Deletes an airway from the RDF store by airwayId.
   *
   * @param airwayId the unique identifier of the airway to be deleted
   */
  public void deleteAirwayByAirwayId(@NonNull String airwayId) {
    log.debug("Starting deleteAirwayByAirwayId process.");
    try {
      droneHighwayRepository.deleteAirwayByAirwayId(airwayId);
      log.info("deleteAirwayByAirwayId: successfully deleted airway with airwayId = {}.", airwayId);
    } catch (Exception e) {
      log.error("Error occurred while deleting airway with airwayId = {}: {}", airwayId, e.getMessage());
    }
  }

  /**
   * Retrieves or updates the modified timestamp in the RDF store.
   *
   * @return A string representing the last modified timestamp.
   */
  public String getOrUpdateModifiedTimestamp() {
    Optional<String> lastModifiedOptional = generalRdfRepository.getModifiedTimestamp();

    if (lastModifiedOptional.isPresent()) {
      log.debug("Successfully retrieved last modified timestamp: {}", lastModifiedOptional.get());
      return lastModifiedOptional.get();
    } else {
      log.debug("Failed to retrieve last modified timestamp, returning current datetime.");
      generalRdfRepository.updateModifiedTimestamp();
      return Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }
  }
}
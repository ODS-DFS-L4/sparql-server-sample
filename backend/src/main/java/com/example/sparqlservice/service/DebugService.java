package com.example.sparqlservice.service;

import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;

import com.example.sparqlservice.DTO.request.DronePortIntrusionRequestDTO;
import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO;
import com.example.sparqlservice.DTO.response.dronePort.DronePortResponseDTO;
import com.example.sparqlservice.converter.impl.AirwayConverter;
import com.example.sparqlservice.converter.impl.DronePortConverter;
import com.example.sparqlservice.converter.impl.DronePortIntrusionConverter;
import com.example.sparqlservice.repository.DroneHighwayRepository;
import com.example.sparqlservice.repository.DronePortIntrusionRepository;
import com.example.sparqlservice.repository.DronePortRepository;
import com.example.sparqlservice.repository.GeneralRdfRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebugService {
  private final GeneralRdfRepository generalRepository;
  private final DroneHighwayRepository droneHighwayRepository;
  private final AirwayConverter airwayConverter;
  private final DronePortRepository dronePortRepository;
  private final DronePortConverter dronePortConverter;
  private final DronePortIntrusionConverter dronePortIntrusionConverter;
  private final DronePortIntrusionRepository dronePortIntrusionRepository;

  /**************************************
   * GENERAL
   ***************************************/

  public void updateModifiedTimestamp() {
    generalRepository.updateModifiedTimestamp();
  }

  /**************************************
   * AIRWAY
   ***************************************/

  /**
   * * Converts a DTO object to an RDF model and persists it to the RDF store.
   *
   * @param dto
   */
  public void createDroneHighway(AirwayResponseDTO dto) {
    try {
      Model model = airwayConverter.convert(dto);
      model.write(System.out, "TURTLE");
      droneHighwayRepository.replaceAllAirways(model);
      log.info("createDroneHighway: successfully created");
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Deletes all airway instances in the RDF store.
   *
   */
  public void deleteAllAirways() {
    try {
      droneHighwayRepository.deleteAllAirways();
    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * Deletes an airway instance in the RDF store by its airway ID.
   *
   */
  public void deleteAirwayByAirwayId(String airwayId) {
    try {
      droneHighwayRepository.deleteAirwayByAirwayId(airwayId);
      log.info("deleteAirwayByAirwayId: successfully deleted airway with ID = {}", airwayId);
    } catch (Exception e) {
      log.error("Error deleting airway with ID = {}: {}", airwayId, e.getMessage());
      throw e;
    }
  }

  /**************************************
   * DRONE PORT
   ***************************************/

  public void insertDronePort(DronePortResponseDTO dto) {
    if (dto == null) {
      log.error("DTO cannot be null");
      throw new RuntimeException("Failed to retrieve Airways synchronously");
    }

    try {
      Model model = dronePortConverter.convert(dto);
      generalRepository.saveToJenaWithRdfConnection(model);
      log.info("fetchAndInsertDroneHighwaySync: successfully inserted drone highway data");
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public void deleteDronePortByDronePortId(String dronePortId) {
    if (dronePortId == null) {
      log.error("DronePortId cannot be null");
      throw new IllegalArgumentException("DronePortId cannot be null");
    }

    try {
      dronePortIntrusionRepository.deleteByDronePortId(dronePortId);
      dronePortRepository.deleteDronePortByDronePortId(dronePortId);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public void insertDronePortIntrusion(DronePortIntrusionRequestDTO dto) {
    try {
      Model model = dronePortIntrusionConverter.convert(dto);
      model.write(System.out, "TURTLE");
      generalRepository.saveToJenaWithRdfConnection(model);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw e;
    }
  }

  public void deleteDronePortIntrusion(String dronePortId) {
    dronePortIntrusionRepository.deleteByDronePortId(dronePortId);
  }

}

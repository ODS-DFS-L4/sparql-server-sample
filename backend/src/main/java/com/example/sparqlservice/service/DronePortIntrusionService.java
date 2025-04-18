package com.example.sparqlservice.service;

import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;

import com.example.sparqlservice.DTO.request.DronePortIntrusionRequestDTO;
import com.example.sparqlservice.converter.impl.DronePortIntrusionConverter;
import com.example.sparqlservice.repository.DronePortIntrusionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DronePortIntrusionService {

  private final DronePortIntrusionRepository dronePortIntrusionRepository;
  private final DronePortIntrusionConverter dronePortIntrusionConverter;

  /**
   * Updates the DronePort Intrusion status for a specific drone port.
   *
   * @param dto the data transfer object containing intrusion status
   * @throws Exception if an error occurs during the update process
   */
  public void updateDronePortIntrusionByDronePortId(DronePortIntrusionRequestDTO dto) {
    final String dronePortId = dto.getDronePortId();
    log.debug("Starting updateDronePortIntrusionByDronePortId for dronePortId = {}", dronePortId);
    try {
      // Convert the DTO to an RDF model
      Model model = dronePortIntrusionConverter.convert(dto);

      // Update the RDF store with the new model
      dronePortIntrusionRepository.updateByDronePortId(dronePortId, model);
      log.info("Successfully updated DronePort Intrusion for dronePortId = {}", dronePortId);
    } catch (Exception e) {
      log.error("Error updating DronePort Intrusion for dronePortId = {}: {}", dronePortId, e.getMessage());
      throw e;
    }
  }
}

package com.example.sparqlservice.controller;

import com.example.sparqlservice.DTO.request.DronePortIntrusionRequestDTO;
import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO;
import com.example.sparqlservice.DTO.response.dronePort.DronePortResponseDTO;
import com.example.sparqlservice.service.DebugService;
import com.example.sparqlservice.service.RdfService;

import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/*
 * THIS IS FOR DEBUG PURPOSES.
 */
@Hidden // This controller shouldn't be on OpenAPI specification
@Slf4j
@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    private final DebugService debugService;
    private final RdfService rdfService;

    @PostMapping("/drone-highway")
    public ResponseEntity<String> droneHighway(@RequestBody AirwayResponseDTO dto) {
        logger.debug("###Received body### \n" + dto);
        logger.debug("###DronePort Ids### \n" + dto.getAllDronePortIds());
        try {
            debugService.createDroneHighway(dto);
            return ResponseEntity.ok("Models successfully persisted to RDF store");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error persisting models: " + e.getMessage());
        }
    }

    /**
     * SYNCHRONOUS
     *
     * Endpoint for testing: fetches data from external drone-highway convert the
     * response into rdf model and finally insert to fuseki synchronously.
     */
    @PostMapping("/fetch-and-insert-sync")
    public ResponseEntity<String> droneHighwaySync() {
        try {
            rdfService.fetchAndReplaceAllAirways();
            return ResponseEntity.ok("Fetch and insert called successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching and inserting data: " + e.getMessage());
        }
    }

    @DeleteMapping("/airway")
    public ResponseEntity<String> deleteAirwayByAirwayId(@RequestParam(required = false) String airwayId,
            @RequestParam(required = false) Boolean all) {
        try {
            if (all != null && all) {
                return deleteAllAirways();
            } else if (StringUtils.isEmpty(airwayId)) {
                throw new IllegalArgumentException("Airway ID must not be empty or null");
            }
            debugService.deleteAirwayByAirwayId(airwayId);
            return ResponseEntity.ok("Airway deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Error deleting airway: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error deleting airway: " + e.getMessage());
        }
    }

    private ResponseEntity<String> deleteAllAirways() {
        try {
            debugService.deleteAllAirways();
            return ResponseEntity.ok("All airways deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Error deleting airways: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error deleting airways: " + e.getMessage());
        }
    }

    @PostMapping("/drone-port")
    public ResponseEntity<String> dronePort(@RequestBody DronePortResponseDTO dto) {
        log.debug("received dronePort ...\n" + dto);
        try {
            debugService.insertDronePort(dto);
            return ResponseEntity.ok("Models successfully persisted to RDF store");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error persisting models: " + e.getMessage());
        }
    }

    @DeleteMapping("/drone-port")
    public ResponseEntity<String> deleteDronePortByDronePortId(@RequestParam String dronePortId) {
        log.debug("Received request to delete drone port with ID: {}", dronePortId);
        try {
            debugService.deleteDronePortByDronePortId(dronePortId);
            return ResponseEntity.ok("Drone port deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Error deleting drone port: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error deleting drone port: " + e.getMessage());
        }
    }

    @PostMapping("/update-timestamp")
    public ResponseEntity<String> updateTimestamp() {
        try {
            debugService.updateModifiedTimestamp();
            return ResponseEntity.ok("Timestamp successfully updated in RDF store");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error updating timestamp: " + e.getMessage());
        }
    }

    @PostMapping("/drone-port-intrusion")
    public ResponseEntity<?> postMethodName(@RequestBody DronePortIntrusionRequestDTO dto) {
        try {
            log.debug(dto.toString());
            debugService.insertDronePortIntrusion(dto);
            return ResponseEntity.ok().body("Drone port intrusion data successfully persisted");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error persisting drone port intrusion data: " + e.getMessage());
        }
    }

    @DeleteMapping("/drone-port-intrusion")
    public ResponseEntity<?> deleteIntrusion(@RequestParam String dronePortId) {
        try {
            debugService.deleteDronePortIntrusion(dronePortId);
            return ResponseEntity.ok().body("Drone port intrusion data successfully deleted");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error deleting drone port intrusion data: " + e.getMessage());
        }
    }
}

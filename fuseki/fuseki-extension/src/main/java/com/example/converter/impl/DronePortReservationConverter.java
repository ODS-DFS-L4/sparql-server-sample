package com.example.converter.impl;


import java.net.URI;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.DTO.response.DronePortReservationResponseDTO;
import com.example.converter.RdfConverter;

public class DronePortReservationConverter implements RdfConverter<DronePortReservationResponseDTO> {
  private static final Logger logger = LoggerFactory.getLogger(DronePortReservationConverter.class);
  // URI prefix for local RDF resources(instances)
  private final String LOCAL_PREFIX;

  // URI prefix for the drone port ontology(classes and properties)
  private static final String DRONE_PORT_PREFIX = "http://example.com/drone-port#";
  private static final String AIRCRAFT_PREFIX = "http://example.com/aircraft#";

  private final String SF_NS = "http://www.opengis.net/ont/sf#";
  private static final String GEO_NS = "http://www.opengis.net/ont/geosparql#";

  public DronePortReservationConverter(String url) {
    logger.debug("Initializing DronePortReservationConverter with URL: {}", url);
    String scheme = "http";
    String domain = url.split("://")[1].split("/")[0];
    LOCAL_PREFIX = scheme + "://" + domain + "/resource/";
    logger.debug("Initialized LOCAL_PREFIX: {}", LOCAL_PREFIX);
  }

  @Override
  public Model convert(DronePortReservationResponseDTO dto) {
    if (dto == null) {
      throw new IllegalArgumentException("DronePortReservationResponseDTO cannot be null");
    }

    Model model = ModelFactory.createDefaultModel();
    model.setNsPrefix("dp", DRONE_PORT_PREFIX);
    model.setNsPrefix("acr", AIRCRAFT_PREFIX);
    model.setNsPrefix("geo", GEO_NS);
    model.setNsPrefix("res", LOCAL_PREFIX);
    model.setNsPrefix("sf", SF_NS);

    // DronePortReservation
    String reservationURI = LOCAL_PREFIX + "dronePortReservation/" + dto.getDronePortReservationId();
    Resource reservationResource = model.createResource(reservationURI)
        .addProperty(RDF.type, model.createResource(DRONE_PORT_PREFIX + "DronePortReservation"));

    if (dto.getDronePortReservationId() != null) {
      reservationResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "dronePortReservationId"),
          model.createTypedLiteral(dto.getDronePortReservationId()));
    }

    // DronePort
    if (dto.getDronePortId() != null) {
      // dronePortId
      String dronePortId = dto.getDronePortId();
      reservationResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "dronePortId"),
          model.createTypedLiteral(dronePortId));

      // hasDronePortReservation
      String dronePortURI = LOCAL_PREFIX + "dronePort/" + dronePortId;
      model.createResource(dronePortURI)
          .addProperty(model.createProperty(DRONE_PORT_PREFIX + "hasDronePortReservation"),
              model.createResource(reservationURI));

      // reservesDronePort
      model.createResource(reservationURI)
          .addProperty(model.createProperty(DRONE_PORT_PREFIX + "reservesDronePort"),
              model.createResource(dronePortURI));
    }

    // Aircraft
    if (dto.getAircraftId() != null) {
      // aircraftId
      reservationResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "aircraftId"),
          model.createTypedLiteral(dto.getAircraftId()));

      // reservesAircraft
      String aircraftURI = LOCAL_PREFIX + "aircraft/" + dto.getAircraftId();
      reservationResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "reservesAircraft"),
          model.createResource(aircraftURI));
    }

    // Airway/Route
    if (dto.getRouteReservationId() != null) {
      String routeReservationId = dto.getRouteReservationId();
      reservationResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "routeReservationId"),
          model.createTypedLiteral(routeReservationId));

      String airwayReservationURI = LOCAL_PREFIX + "airwayReservation/" + routeReservationId;
      reservationResource
          .addProperty(model.createProperty(DRONE_PORT_PREFIX + "associatedRouteReservation"),
              model.createResource(airwayReservationURI));

      model.createResource(airwayReservationURI)
          .addProperty(model.createProperty(DRONE_PORT_PREFIX + "associatedDronePortReservation"),
              model.createResource(reservationURI));
    }

    if (dto.getUsageType() != null) {
      // reservationUsageType
      reservationResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "reservationUsageType"),
          model.createTypedLiteral(dto.getUsageType(), XSDDatatype.XSDinteger));

      // reservationUsageTypeName
      String reservationUsageTypeName = switch (dto.getUsageType()) {
        case 1 -> "離陸ポート";
        case 2 -> "着陸ポート";
        default -> "その他";
      };
      reservationResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "reservationUsageTypeName"),
          model.createTypedLiteral(reservationUsageTypeName));

      // hasReservationUsageType
      String reservationUsageTypeSubClassURI = switch (dto.getUsageType()) {
        case 1 -> DRONE_PORT_PREFIX + "TakeoffPort";
        case 2 -> DRONE_PORT_PREFIX + "LandingPort";
        default -> DRONE_PORT_PREFIX + "OtherUsage";
      };
      reservationResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "hasReservationUsageType"),
          model.createResource(reservationUsageTypeSubClassURI));
    }

    if (dto.getReservationTimeFrom() != null) {
      reservationResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "reservationTimeFrom"),
          model.createTypedLiteral(dto.getReservationTimeFrom(), XSDDatatype.XSDdateTime));
    }

    if (dto.getReservationTimeTo() != null) {
      reservationResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "reservationTimeTo"),
          model.createTypedLiteral(dto.getReservationTimeTo(), XSDDatatype.XSDdateTime));
    }
    return model;
  }
}

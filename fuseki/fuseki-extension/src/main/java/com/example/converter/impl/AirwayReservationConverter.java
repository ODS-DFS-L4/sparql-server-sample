package com.example.converter.impl;

import java.net.URI;
import java.util.Optional;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.DTO.response.AirwayReservationResponseDTO;
import com.example.converter.RdfConverter;

public class AirwayReservationConverter implements RdfConverter<AirwayReservationResponseDTO> {
  private static final Logger logger = LoggerFactory.getLogger(AirwayReservationConverter.class);
  // URI prefix for local RDF resources(instances)
  private final String LOCAL_PREFIX;

  // URI prefix for the drone port ontology(classes and properties)
  private static final String DRONE_HIGHWAY_PREFIX = "http://example.com/drone-highway#";
  private final String SF_NS = "http://www.opengis.net/ont/sf#";
  private static final String GEO_NS = "http://www.opengis.net/ont/geosparql#";

  public AirwayReservationConverter(String url) {
    logger.debug("Initializing AirwayReservationConverter with URL: {}", url);
    String scheme = "http";
    String domain = url.split("://")[1].split("/")[0];
    LOCAL_PREFIX = scheme + "://" + domain + "/resource/";
    logger.debug("Initialized LOCAL_PREFIX: {}", LOCAL_PREFIX);
  }

  @Override
  public Model convert(AirwayReservationResponseDTO dto) {
    if (dto == null) {
      throw new IllegalArgumentException("AirwayReservationResponseDTO cannot be null");
    }

    Model model = ModelFactory.createDefaultModel();
    model.setNsPrefix("drone-highway", DRONE_HIGHWAY_PREFIX);
    model.setNsPrefix("geo", GEO_NS);
    model.setNsPrefix("res", LOCAL_PREFIX);
    model.setNsPrefix("sf", SF_NS);

    // AirwayReservation
    String reservationURI = LOCAL_PREFIX + "airwayReservation/" + dto.getAirwayReservationId();
    Resource reservationResource = model.createResource(reservationURI)
        .addProperty(RDF.type, model.createResource(DRONE_HIGHWAY_PREFIX + "AirwayReservation"));

    if (dto.getAirwayReservationId() != null) {
      reservationResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "airwayReservationId"),
          model.createTypedLiteral(dto.getAirwayReservationId()));
    }

    if (dto.getOperatorId() != null) {
      reservationResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "operatorId"),
          model.createTypedLiteral(dto.getOperatorId()));
    }

    // AirwaySection
    Optional.ofNullable(dto.getAirwaySectionIds())
        .ifPresent(airwaySectionIds -> airwaySectionIds
            .forEach(airwaySectionId -> {
              // reservedAirwaySectionId
              reservationResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "reservedAirwaySectionId"),
                  model.createTypedLiteral(airwaySectionId));

              // reservesAirwaySection
              String airwaySectionURI = LOCAL_PREFIX + "airwaySection/" + airwaySectionId;
              Resource airwaySectionResource = model.createResource(airwaySectionURI);
              reservationResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "reservesAirwaySection"),
                  airwaySectionResource);

              // reservedByAirwayReservation
              airwaySectionResource.addProperty(
                  model.createProperty(DRONE_HIGHWAY_PREFIX + "reservedByAirwayReservation"),
                  reservationResource);
            }));

    if (dto.getStartAt() != null) {
      reservationResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "startsAt"),
          model.createTypedLiteral(dto.getStartAt(), XSDDatatype.XSDdateTime));
    }

    if (dto.getEndAt() != null) {
      reservationResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "endsAt"),
          model.createTypedLiteral(dto.getEndAt(), XSDDatatype.XSDdateTime));
    }

    if (dto.getReservedAt() != null) {
      reservationResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "reservedAt"),
          model.createTypedLiteral(dto.getReservedAt(), XSDDatatype.XSDdateTime));
    }

    return model;
  }
}

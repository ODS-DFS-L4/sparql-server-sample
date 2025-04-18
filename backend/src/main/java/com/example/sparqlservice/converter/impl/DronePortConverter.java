package com.example.sparqlservice.converter.impl;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.sparqlservice.DTO.response.dronePort.DronePortResponseDTO;
import com.example.sparqlservice.converter.RdfConverter;
import com.example.sparqlservice.util.GeoCoordinateUtil;

@Component
public class DronePortConverter implements RdfConverter<DronePortResponseDTO> {

  // URI prefix for local RDF resources(instances)
  private final String LOCAL_PREFIX;

  // URI prefix for the drone port ontology(classes and properties)
  private static final String DRONE_PORT_PREFIX = "http://example.com/drone-port#";
  private final String AIRCRAFT_PREFIX = "http://example.com/aircraft#";

  private final String SF_NS = "http://www.opengis.net/ont/sf#";
  private static final String GEO_NS = "http://www.opengis.net/ont/geosparql#";

  public DronePortConverter(@Value("${server.url}") String url) {
    String scheme = "http";
    String domain = url.split("://")[1].split("/")[0];
    LOCAL_PREFIX = scheme + "://" + domain + "/resource/";
  }

  @Override
  public Model convert(DronePortResponseDTO dto) {
    if (dto == null) {
      throw new IllegalArgumentException("DronePortResponseDTO cannot be null");
    }

    Model model = ModelFactory.createDefaultModel();
    model.setNsPrefix("dp", DRONE_PORT_PREFIX);
    model.setNsPrefix("acr", AIRCRAFT_PREFIX);
    model.setNsPrefix("geo", GEO_NS);
    model.setNsPrefix("res", LOCAL_PREFIX);
    model.setNsPrefix("sf", SF_NS);

    // DronePort
    String dronePortURI = LOCAL_PREFIX + "dronePort/" + dto.getDronePortId();
    Resource dronePortResource = model.createResource(dronePortURI)
        .addProperty(RDF.type, model.createResource(DRONE_PORT_PREFIX + "DronePort"));

    if (dto.getDronePortId() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "dronePortId"),
          model.createTypedLiteral(dto.getDronePortId()));
    }
    if (dto.getDronePortName() != null) {
      dronePortResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "dronePortName"),
          model.createTypedLiteral(dto.getDronePortName()));
    }
    if (dto.getAddress() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "address"),
          model.createTypedLiteral(dto.getAddress()));
    }
    if (dto.getManufacturer() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "manufacturer"),
          model.createTypedLiteral(dto.getManufacturer()));
    }
    if (dto.getSerialNumber() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "serialNumber"),
          model.createTypedLiteral(dto.getSerialNumber()));
    }
    if (dto.getDronePortManufacturerId() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "dronePortManufacturerId"),
          model.createTypedLiteral(dto.getDronePortManufacturerId()));
    }
    if (dto.getPortType() != null) {
      // portType
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "portType"),
          model.createTypedLiteral(dto.getPortType(), XSDDatatype.XSDinteger));

      // portTypeName
      String portTypeName = switch (dto.getPortType()) {
        case 0 -> "緊急離着陸点";
        case 1 -> "ドローンポート";
        case 2 -> "離発着場";
        default -> "その他";
      };
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "portTypeName"),
          model.createTypedLiteral(portTypeName));

      // hasPortType
      String portTypeURI = switch (dto.getPortType()) {
        case 0 -> DRONE_PORT_PREFIX + "EmergencyLandingSite";
        case 1 -> DRONE_PORT_PREFIX + "NormalDronePort";
        case 2 -> DRONE_PORT_PREFIX + "TakeoffLandingSite";
        default -> DRONE_PORT_PREFIX + "OtherPortType";
      };
      dronePortResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "hasPortType"),
          model.createResource(portTypeURI));
    }
    if (dto.getVisDronePortCompanyId() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "visDronePortCompanyId"),
          model.createTypedLiteral(dto.getVisDronePortCompanyId()));
    }
    if (dto.getStoredAircraftId() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "storedAircraftId"),
          model.createTypedLiteral(dto.getStoredAircraftId()));

      // storesAircraft
      Resource aircraftResource = model.createResource(LOCAL_PREFIX + "aircraft/" + dto.getStoredAircraftId())
          .addProperty(RDF.type, model.createResource(AIRCRAFT_PREFIX + "Aircraft"));
      dronePortResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "storesAircraft"), aircraftResource);
    }
    if (dto.getLat() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "latitude"),
          model.createTypedLiteral(dto.getLat().toString(), XSDDatatype.XSDdecimal));
    }
    if (dto.getLon() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "longitude"),
          model.createTypedLiteral(dto.getLon().toString(), XSDDatatype.XSDdecimal));
    }
    if (dto.getAlt() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "altitude"),
          model.createTypedLiteral(dto.getAlt().toString(), XSDDatatype.XSDdecimal));
    }
    if (dto.getSupportDroneType() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "supportDroneType"),
          model.createTypedLiteral(dto.getSupportDroneType()));
    }
    if (dto.getUsageType() != null) {
      // portUsageType
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "portUsageType"),
          model.createTypedLiteral(dto.getUsageType(), XSDDatatype.XSDinteger));

      // usageTypeName
      String usageTypeName = switch (dto.getUsageType()) {
        case 1 -> "駐機場";
        case 2 -> "緊急着陸地点";
        default -> "その他";
      };
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "portUsageTypeName"),
          model.createTypedLiteral(usageTypeName));

      // hasUsageType
      String usageTypeURI = switch (dto.getUsageType()) {
        case 1 -> DRONE_PORT_PREFIX + "ParkingUsage";
        case 2 -> DRONE_PORT_PREFIX + "EmergencyLanding";
        default -> DRONE_PORT_PREFIX + "OtherUsageType";
      };
      dronePortResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "hasPortUsageType"),
          model.createResource(usageTypeURI));
    }
    if (dto.getActiveStatus() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "activeStatus"),
          model.createTypedLiteral(dto.getActiveStatus()));
    }
    if (dto.getInactiveTimeFrom() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "inactiveTimeFrom"),
          model.createTypedLiteral(dto.getInactiveTimeFrom(), XSDDatatype.XSDdateTime));
    }
    if (dto.getInactiveTimeTo() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "inactiveTimeTo"),
          model.createTypedLiteral(dto.getInactiveTimeTo(), XSDDatatype.XSDdateTime));
    }
    if (dto.getImageData() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "imageData"),
          model.createTypedLiteral(dto.getImageData()));
    }
    if (dto.getUpdateTime() != null) {
      dronePortResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "updateTime"),
          model.createTypedLiteral(dto.getUpdateTime(), XSDDatatype.XSDdateTime));
    }

    // GeoSPARQL Geometry
    if (dto.getLat() != null && dto.getLon() != null && dto.getAlt() != null) {
      String wkt = GeoCoordinateUtil.convertPointZCoordinatesToWKT(dto.getLon(), dto.getLat(), dto.getAlt());
      String geometryId = dronePortURI + "/geometry";
      Resource geometryResource = model.createResource(geometryId)
          .addProperty(RDF.type, model.createResource(SF_NS + "Point"))
          .addProperty(model.createProperty(GEO_NS + "asWKT"), model.createTypedLiteral(wkt, GEO_NS + "wktLiteral"));

      dronePortResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "hasGeometry"), geometryResource);
    }

    return model;
  }
}
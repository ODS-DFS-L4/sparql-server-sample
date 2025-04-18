package com.example.sparqlservice.converter.impl;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.sparqlservice.DTO.request.DronePortIntrusionRequestDTO;
import com.example.sparqlservice.DTO.request.DronePortIntrusionRequestDTO.DetectionEvent.Location;
import com.example.sparqlservice.converter.RdfConverter;
import com.example.sparqlservice.util.GeoCoordinateUtil;

@Component
public class DronePortIntrusionConverter implements RdfConverter<DronePortIntrusionRequestDTO> {

  // URI prefix for local RDF resources(instances)
  private final String LOCAL_PREFIX;

  // URI prefix for the drone port ontology(classes and properties)
  private static final String DRONE_PORT_PREFIX = "http://example.com/drone-port#";

  private final String SF_NS = "http://www.opengis.net/ont/sf#";
  private static final String GEO_NS = "http://www.opengis.net/ont/geosparql#";

  public DronePortIntrusionConverter(@Value("${server.url}") String url) {
    String scheme = "http";
    String domain = url.split("://")[1].split("/")[0];
    LOCAL_PREFIX = scheme + "://" + domain + "/resource/";
  }

  @Override
  public Model convert(DronePortIntrusionRequestDTO dto) {
    if (dto == null) {
      throw new IllegalArgumentException("DronePortIntrusionRequestDTO cannot be null");
    }

    Model model = ModelFactory.createDefaultModel();

    model.setNsPrefix("dp", DRONE_PORT_PREFIX);
    model.setNsPrefix("geo", GEO_NS);
    model.setNsPrefix("res", LOCAL_PREFIX);

    // PortIntrusion
    // Use DronePortId as part of the DronePortIntrusion URI
    String intrusionURI = LOCAL_PREFIX + "dronePortIntrusion/" + dto.getDronePortId();
    Resource intrusionResource = model.createResource(intrusionURI)
        .addProperty(RDF.type, model.createResource(DRONE_PORT_PREFIX + "DronePortIntrusion"));

    if (dto.getDronePortId() != null) {
      intrusionResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "dronePortId"),
          model.createTypedLiteral(dto.getDronePortId()));

      // hasIntrusion
      String dronePortURI = LOCAL_PREFIX + "dronePort/" + dto.getDronePortId();
      Resource dronePortResource = model.createResource(dronePortURI);
      dronePortResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "hasIntrusion"), intrusionResource);

      // isIntrusionFor
      intrusionResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "isIntrusionFor"), dronePortResource);
    }
    if (dto.getTimestamp() != null) {
      intrusionResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "timestamp"),
          model.createTypedLiteral(dto.getTimestamp().toString(), XSDDatatype.XSDdateTime));
    }
    if (dto.getAnyDetection() != null) {
      intrusionResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "anyDetection"),
          model.createTypedLiteral(dto.getAnyDetection().booleanValue()));
    }
    if (dto.getReportEndpointUrl() != null) {
      intrusionResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "reportEndpointUrl"),
          model.createTypedLiteral(dto.getReportEndpointUrl()));
    }

    // Intrusion Events
    if (dto.getEvents() != null) {
      for (DronePortIntrusionRequestDTO.DetectionEvent event : dto.getEvents()) {
        String eventURI = intrusionURI + "/intrusionEvent/" + event.getObjectId();
        Resource eventResource = model.createResource(eventURI)
            .addProperty(RDF.type, model.createResource(DRONE_PORT_PREFIX + "IntrusionEvent"))
            .addProperty(model.createProperty(DRONE_PORT_PREFIX + "objectId"),
                model.createTypedLiteral(event.getObjectId()));

        // hasIntrusionEvent
        intrusionResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "hasIntrusionEvent"), eventResource);

        // isEventOfIntrusion
        eventResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "isEventOfIntrusion"), intrusionResource);

        if (event.getObjectType() != null) {
          eventResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "objectType"),
              model.createTypedLiteral(event.getObjectType()));
        }
        if (event.getDetectionStatus() != null) {
          eventResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "detectionStatus"),
              model.createTypedLiteral(event.getDetectionStatus().booleanValue()));

          // hasDetectionStatus
          String detectionStatusURI;
          if (event.getDetectionStatus()) {
            detectionStatusURI = DRONE_PORT_PREFIX + "Detected";
          } else {
            detectionStatusURI = DRONE_PORT_PREFIX + "Disappeared";
          }
          eventResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "hasDetectionStatus"),
              model.createResource(detectionStatusURI));
        }

        if (event.getLocation() != null && event.getLocation().getLatitude() != null
            && event.getLocation().getLongitude() != null) {
          Location location = event.getLocation();

          // Use `toString()` on each coordinate.
          eventResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "latitude"),
              model.createTypedLiteral(location.getLatitude().toString(), XSDDatatype.XSDdecimal));

          eventResource.addLiteral(model.createProperty(DRONE_PORT_PREFIX + "longitude"),
              model.createTypedLiteral(location.getLongitude().toString(), XSDDatatype.XSDdecimal));

          // GeoSPARQL Geometry
          String wkt = GeoCoordinateUtil.convertPointCoordinatesToWKT(location.getLongitude(),
              location.getLatitude());
          String geometryURI = eventURI + "/geometry";
          Resource geometryResource = model.createResource(geometryURI)
              .addProperty(RDF.type, model.createResource(SF_NS + "Point"))
              .addProperty(model.createProperty(GEO_NS + "asWKT"),
                  model.createTypedLiteral(wkt, GEO_NS + "wktLiteral"));

          eventResource.addProperty(model.createProperty(DRONE_PORT_PREFIX + "hasGeometry"), geometryResource);
        }
      }
    }
    return model;
  }

}

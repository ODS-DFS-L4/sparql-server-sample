package com.example.sparqlservice.converter.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO;
import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO.Airway;
import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO.AirwayGeometry;
import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO.AirwayInfo;
import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO.AirwayJunction;
import com.example.sparqlservice.DTO.response.airway.AirwayResponseDTO.AirwaySection;
import com.example.sparqlservice.converter.RdfConverter;
import com.example.sparqlservice.util.GeoCoordinateUtil;

@Component
public class AirwayConverter implements RdfConverter<AirwayResponseDTO> {

    // URI prefix for local RDF resources(instances)
    private final String LOCAL_PREFIX;

    // URI prefix for the drone highway ontology(classes and properties)
    private final String DRONE_HIGHWAY_PREFIX = "http://example.com/drone-highway#";
    private final String DRONE_PORT_PREFIX = "http://example.com/drone-port#";
    private final String AIRCRAFT_PREFIX = "http://example.com/aircraft#";

    private final String SF_NS = "http://www.opengis.net/ont/sf#";
    private final String GEO_NS = "http://www.opengis.net/ont/geosparql#";

    AirwayConverter(@Value("${server.url}") String url) {
        String scheme = "http";
        String domain = url.split("://")[1].split("/")[0];
        LOCAL_PREFIX = scheme + "://" + domain + "/resource/";
    }

    @Override
    public Model convert(AirwayResponseDTO dto) {
        if (dto == null || dto.getAirway() == null) {
            throw new IllegalArgumentException("AirwayResponseDTO or Airway cannot be null");
        }

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("onto", DRONE_HIGHWAY_PREFIX);
        model.setNsPrefix("acr", AIRCRAFT_PREFIX);
        model.setNsPrefix("res", LOCAL_PREFIX);
        model.setNsPrefix("sf", SF_NS);
        model.setNsPrefix("geo", GEO_NS);

        Airway airway = dto.getAirway();

        // AirwayAdministrator
        String adminURI = LOCAL_PREFIX + "airwayAdministrator/" + airway.getAirwayAdministratorId();
        Resource adminResource = model.createResource(adminURI)
                .addProperty(RDF.type,
                        model.createResource(DRONE_HIGHWAY_PREFIX + "AirwayAdministrator"))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "airwayAdministratorId"),
                        model.createTypedLiteral(airway.getAirwayAdministratorId()))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "businessNumber"),
                        model.createTypedLiteral(airway.getBusinessNumber()));

        // Each Airway
        Optional.ofNullable(airway.getAirways())
                .ifPresent(airways -> airways
                        .forEach(airwayInfo -> addAirway(model, adminResource, airwayInfo)));

        return model;
    }

    // Airway
    private void addAirway(Model model, Resource adminResource, AirwayInfo airwayInfo) {
        String airwayURI = LOCAL_PREFIX + "airway/" + airwayInfo.getAirwayId();
        Resource airwayResource = model.createResource(airwayURI)
                .addProperty(RDF.type, model.createResource(DRONE_HIGHWAY_PREFIX + "Airway"))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "airwayId"),
                        model.createTypedLiteral(airwayInfo.getAirwayId()))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "airwayName"),
                        model.createTypedLiteral(airwayInfo.getAirwayName()))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "flightPurpose"),
                        model.createTypedLiteral(airwayInfo.getFlightPurpose()))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "createdAt"),
                        model.createTypedLiteral(airwayInfo.getCreatedAt(),
                                XSDDatatype.XSDdateTime))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "updatedAt"),
                        model.createTypedLiteral(airwayInfo.getUpdatedAt(),
                                XSDDatatype.XSDdateTime));

        adminResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "hasAirway"), airwayResource);
        airwayResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "belongsToAirwayAdministrator"),
                adminResource);

        // DroneList
        Optional.ofNullable(airwayInfo.getDroneList())
                .ifPresent(droneIds -> droneIds
                        .forEach(droneId -> {
                            // droneId
                            airwayResource.addLiteral(
                                    model.createProperty(DRONE_HIGHWAY_PREFIX
                                            + "droneId"),
                                    model.createTypedLiteral(droneId));

                            // hasAvailableDrone
                            Resource droneResource = model.createResource(
                                    LOCAL_PREFIX + "aircraft/" + droneId)
                                    .addProperty(RDF.type, model.createResource(AIRCRAFT_PREFIX + "Aircraft"));
                            airwayResource.addProperty(
                                    model.createProperty(DRONE_HIGHWAY_PREFIX
                                            + "hasAvailableDrone"),
                                    droneResource);
                        }));

        // AirwayJunction
        Optional.ofNullable(airwayInfo.getAirwayJunctions())
                .ifPresent(junctions -> junctions
                        .forEach(junction -> addAirwayJunction(model, airwayResource,
                                junction)));

        // AirwaySection
        Optional.ofNullable(airwayInfo.getAirwaySections())
                .ifPresent(sections -> sections
                        .forEach(section -> addAirwaySection(model, airwayResource, section)));
    }

    // AirwayJunction
    private void addAirwayJunction(Model model, Resource airwayResource, AirwayJunction junction) {
        String junctionURI = LOCAL_PREFIX + "airwayJunction/" + junction.getAirwayJunctionId();
        Resource junctionResource = model.createResource(junctionURI)
                .addProperty(RDF.type, model.createResource(DRONE_HIGHWAY_PREFIX + "AirwayJunction"))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "airwayJunctionId"),
                        model.createTypedLiteral(junction.getAirwayJunctionId()))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "airwayJunctionName"),
                        model.createTypedLiteral(junction.getAirwayJunctionName()));

        airwayResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "hasAirwayJunction"),
                junctionResource);
        junctionResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "belongsToAirway"),
                airwayResource);

        // AirwayGeometry (AirwayArea and DeviationArea)
        Optional.ofNullable(junction.getAirways())
                .ifPresent(airways -> airways
                        .forEach(geometry -> addAirwayGeometry(model, junctionResource,
                                geometry)));
    }

    // AirwayGeometry
    private void addAirwayGeometry(Model model, Resource junctionResource, AirwayGeometry geometry) {

        // Generate UUIDs for airwayAreaURI and deviationAreaURI because they are not
        // provided.
        String airwayAreaURI = LOCAL_PREFIX + "airwayArea/" + UUID.randomUUID().toString();
        String deviationAreaURI = LOCAL_PREFIX + "deviationArea/" + UUID.randomUUID().toString();

        // AirwayArea
        if (geometry.getAirway() != null && geometry.getAirway().getGeometry() != null) {
            List<List<Double>> coordinatesList = geometry.getAirway().getGeometry().getCoordinates();

            Resource airwayAreaResource = model.createResource(airwayAreaURI)
                    .addProperty(RDF.type,
                            model.createResource(DRONE_HIGHWAY_PREFIX + "AirwayArea"));

            junctionResource.addProperty(
                    model.createProperty(DRONE_HIGHWAY_PREFIX + "hasAirwayArea"),
                    airwayAreaResource);
            airwayAreaResource.addProperty(
                    model.createProperty(DRONE_HIGHWAY_PREFIX + "belongsToAirwayJunction"),
                    junctionResource);

            // coordinates
            airwayAreaResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "coordinates"),
                    model.createTypedLiteral(coordinatesList.toString()));

            // hasCoordinate
            for (int i = 0; i < coordinatesList.size(); i++) {
                String coordinateURI = airwayAreaURI + "/coordinate-" + (i + 1);
                Resource coordinateResource = model.createResource(coordinateURI)
                        .addProperty(RDF.type, model
                                .createResource(DRONE_HIGHWAY_PREFIX + "Coordinate"));
                airwayAreaResource.addProperty(
                        model.createProperty(DRONE_HIGHWAY_PREFIX + "hasCoordinate"),
                        coordinateResource);

                List<Double> coordinates = coordinatesList.get(i);
                // longitude
                coordinateResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "longitude"),
                        model.createTypedLiteral(coordinates.get(0).toString(),
                                XSDDatatype.XSDdecimal));
                // latitude
                coordinateResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "latitude"),
                        model.createTypedLiteral(coordinates.get(1).toString(),
                                XSDDatatype.XSDdecimal));
                // altitude
                coordinateResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "altitude"),
                        model.createTypedLiteral(coordinates.get(2).toString(),
                                XSDDatatype.XSDdecimal));
            }

            // hasGeometry
            String wkt = GeoCoordinateUtil.convertPolygonCoordinatesToWKT(coordinatesList);
            String geometryId = airwayAreaURI + "/geometry";
            Resource geometryResource = model.createResource(geometryId)
                    .addProperty(RDF.type, model.createResource(SF_NS + "Polygon"))
                    .addLiteral(model.createProperty(GEO_NS + "asWKT"),
                            model.createTypedLiteral(wkt, GEO_NS + "wktLiteral"));

            airwayAreaResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "hasGeometry"),
                    geometryResource);
        }

        // DeviationArea
        if (geometry.getDeviation() != null && geometry.getDeviation().getGeometry() != null) {
            List<List<Double>> coordinatesList = geometry.getDeviation().getGeometry().getCoordinates();

            Resource deviationAreaResource = model.createResource(deviationAreaURI)
                    .addProperty(RDF.type,
                            model.createResource(DRONE_HIGHWAY_PREFIX + "DeviationArea"));

            junctionResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "hasDeviationArea"),
                    deviationAreaResource);
            deviationAreaResource.addProperty(
                    model.createProperty(DRONE_HIGHWAY_PREFIX + "belongsToAirwayJunction"),
                    junctionResource);

            // coordinates
            deviationAreaResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "coordinates"),
                    model.createTypedLiteral(coordinatesList.toString()));

            // hasCoordinate
            for (int i = 0; i < coordinatesList.size(); i++) {
                String coordinateURI = deviationAreaURI + "/coordinate-" + (i + 1);
                Resource coordinateResource = model.createResource(coordinateURI)
                        .addProperty(RDF.type, model
                                .createResource(DRONE_HIGHWAY_PREFIX + "Coordinate"));
                deviationAreaResource.addProperty(
                        model.createProperty(DRONE_HIGHWAY_PREFIX + "hasCoordinate"),
                        coordinateResource);

                List<Double> coordinates = coordinatesList.get(i);
                // longitude
                coordinateResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "longitude"),
                        model.createTypedLiteral(coordinates.get(0).toString(),
                                XSDDatatype.XSDdecimal));
                // latitude
                coordinateResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "latitude"),
                        model.createTypedLiteral(coordinates.get(1).toString(),
                                XSDDatatype.XSDdecimal));
                // altitude
                coordinateResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "altitude"),
                        model.createTypedLiteral(coordinates.get(2).toString(),
                                XSDDatatype.XSDdecimal));
            }

            // hasGeometry
            String wkt = GeoCoordinateUtil.convertPolygonCoordinatesToWKT(coordinatesList);
            String geometryId = deviationAreaURI + "/geometry";
            Resource geometryResource = model.createResource(geometryId)
                    .addProperty(RDF.type, model.createResource(SF_NS + "Polygon"))
                    .addLiteral(model.createProperty(GEO_NS + "asWKT"),
                            model.createTypedLiteral(wkt, GEO_NS + "wktLiteral"));

            deviationAreaResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "hasGeometry"),
                    geometryResource);
        }
    }

    // AirwaySection
    private void addAirwaySection(Model model, Resource airwayResource, AirwaySection section) {
        String sectionId = LOCAL_PREFIX + "airwaySection/" + section.getAirwaySectionId();
        Resource sectionResource = model.createResource(sectionId)
                .addProperty(RDF.type, model.createResource(DRONE_HIGHWAY_PREFIX + "AirwaySection"))
                .addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "airwaySectionId"),
                        model.createTypedLiteral(section.getAirwaySectionId()))
                .addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "airwaySectionName"),
                        model.createTypedLiteral(section.getAirwaySectionName()));

        airwayResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "hasAirwaySection"),
                sectionResource);
        sectionResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "belongsToAirway"),
                airwayResource);

        // Points that the airway section connects to
        Optional.ofNullable(section.getAirwayJunctionIds())
                .ifPresent(pointIds -> pointIds
                        .forEach(pointId -> sectionResource.addProperty(
                                model.createProperty(DRONE_HIGHWAY_PREFIX + "connectsJunction"),
                                model.createResource(LOCAL_PREFIX + "airwayJunction/" + pointId))));

        // DronePort
        Optional.ofNullable(section.getDroneportIds())
                .ifPresent(dronePortIds -> dronePortIds
                        .forEach(dronePortId -> {
                            // dronePortId
                            sectionResource.addLiteral(model.createProperty(DRONE_HIGHWAY_PREFIX + "dronePortId"),
                                    model.createTypedLiteral(dronePortId));

                            // hasDronePort
                            Resource dronePortResource = model
                                    .createResource(LOCAL_PREFIX + "dronePort/" + dronePortId)
                                    .addProperty(RDF.type, model.createResource(DRONE_PORT_PREFIX + "DronePort"));
                            sectionResource.addProperty(model.createProperty(DRONE_HIGHWAY_PREFIX + "hasDronePort"),
                                    dronePortResource);

                            // belongsToAirwaySection
                            dronePortResource.addProperty(
                                    model.createProperty(DRONE_HIGHWAY_PREFIX + "belongsToAirwaySection"),
                                    sectionResource);
                        }));
    }
}
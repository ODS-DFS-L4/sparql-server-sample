package com.example.sparqlservice.repository;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.sparqlservice.annotation.UpdateModifiedAt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DroneHighwayRepository {
  private final String fusekiResourceEndpoint;

  public DroneHighwayRepository(@Value("${fuseki.resource-endpoint}") String fusekiResourceEndpoint) {
    this.fusekiResourceEndpoint = fusekiResourceEndpoint;
  }

  /**
   * Replaces all existing airways in the RDF store with the provided model.
   *
   * @param newAirwayModel The RDF model containing the new airways to be saved.
   * @throws Exception if an error occurs during the replacement process.
   */
  @UpdateModifiedAt
  public void replaceAllAirways(Model newAirwayModel) {
    log.debug("replaceAllAirways");
    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        // First, delete all existing airways
        conn.update(createDeleteAllAirwaysQuery());

        // Convert the new model into a string object in N-Triples format
        StringWriter writer = new StringWriter();
        newAirwayModel.write(writer, "N-TRIPLE");
        String nTriples = writer.toString();

        String sparqlUpdate = String.format(
            "INSERT DATA { %s }",
            nTriples);

        // Execute the insert query
        conn.update(sparqlUpdate);
        conn.commit();
        log.info("Successfully replaced all airways in the RDF store.");
      } catch (Exception e) {
        conn.abort();
        log.error("Error during replace all airways operation: {}. Rolling back transaction.", e.getMessage());
        throw e;
      }
    }
  }

  /**
   * Updates an airway and its associated drone ports in the RDF store by its
   * airway ID.
   *
   * @param airwayId                The ID of the airway to be updated.
   * @param airwayAndDronePortModel The RDF model containing the updated airway
   *                                and drone port data.
   * @throws Exception if an error occurs during the update process.
   */
  @UpdateModifiedAt
  public void updateByAirwayId(String airwayId, Model airwayAndDronePortModel) {
    log.debug("updateByAirwayId");
    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        // First, delete the existing Airway and DronePort specified by airwayId
        conn.update(createDeleteAirwayByAirwayIdQuery(airwayId));

        // Convert the model into a string object in N-Triples format
        StringWriter writer = new StringWriter();
        airwayAndDronePortModel.write(writer, "N-TRIPLE");
        String nTriples = writer.toString();

        String sparqlUpdate = String.format(
            "INSERT DATA { %s }",
            nTriples);

        // Execute the insert query
        conn.update(sparqlUpdate);
        conn.commit();
        log.info("Successfully updated Airway instance: airwayId = {}.", airwayId);
      } catch (Exception e) {
        conn.abort();
        log.error("Error during update Airway instance: airwayId = {} error = {}.\nRolling back transaction.", airwayId,
            e.getMessage());
        throw e;
      }
    }
  }

  @UpdateModifiedAt
  public void updateAirwayAndDronePortByAirwayIdAndDronePortIds(String airwayId, List<String> dronePortIds,
      Model airwayAndDronePortModel) {
    log.debug("updateByAirwayId");
    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        // First, delete the existing Airway and DronePort specified by airwayId
        conn.update(createDeleteAirwayByAirwayIdQuery(airwayId));
        conn.update(createDeleteDronePortByDronePortIdQuery(dronePortIds));

        // Convert the model into a string object in N-Triples format
        StringWriter writer = new StringWriter();
        airwayAndDronePortModel.write(writer, "N-TRIPLE");
        String nTriples = writer.toString();

        String sparqlUpdate = String.format(
            "INSERT DATA { %s }",
            nTriples);

        // Execute the insert query
        conn.update(sparqlUpdate);
        conn.commit();
        log.info("Successfully updated Airway instance: airwayId = {}.", airwayId);
      } catch (Exception e) {
        conn.abort();
        log.error("Error during update Airway instance: airwayId = {} error = {}.\nRolling back transaction.", airwayId,
            e.getMessage());
        throw e;
      }
    }
  }

  /**
   * Deletes an airway from the RDF store by its airway ID.
   *
   * @param airwayId The ID of the airway to be deleted.
   * @throws Exception if an error occurs during the deletion process.
   */
  @UpdateModifiedAt
  public void deleteAirwayByAirwayId(String airwayId) {
    log.debug("deleteByAirwayId");
    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        conn.update(createDeleteAirwayByAirwayIdQuery(airwayId));
        conn.commit();
        log.info("Successfully deleted Airway instance : airwayId = {}. ", airwayId);
      } catch (Exception e) {
        conn.abort();
        log.error("Error during delete Airway instance: airwayId = {} error = {}.\nRolling back transaction.", airwayId,
            e.getMessage());
        throw e;
      }
    }
  }

  /**
   * Deletes all airways from the RDF store.
   *
   * @throws Exception if an error occurs during the deletion process.
   */
  @UpdateModifiedAt
  public void deleteAllAirways() {
    log.debug("deleteAllAirways");
    try (RDFConnection conn = RDFConnectionRemote.service(fusekiResourceEndpoint).build()) {
      conn.begin(ReadWrite.WRITE);
      try {
        conn.update(createDeleteAllAirwaysQuery());
        conn.commit();
        log.info("Successfully deleted all Airway instances");
      } catch (Exception e) {
        conn.abort();
        log.error("Error during delete all Airway instances: error = {}.\nRolling back transaction.",
            e.getMessage());
        throw e;
      }
    }
  }

  /**
   * Creates a query that deletes an airway and all associated resources by the
   * given airway ID.
   *
   * @param airwayId The ID of the airway to be deleted.
   */
  private String createDeleteAirwayByAirwayIdQuery(String airwayId) {
    airwayId = airwayId.replaceAll("\"", ""); // Remove all double quotes.
    log.debug("airwayId = {}", airwayId);

    String query = String.format(
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "PREFIX geo:  <http://www.opengis.net/ont/geosparql#> " +
            "PREFIX : <http://example.com/drone-highway#> " +
            "DELETE { " +
            "  ?admin :hasAirway ?airway. " +
            "  ?airway a :Airway ;" +
            "          :airwayId ?airwayId ;" +
            "          :belongsToAirwayAdministrator ?admin; " +
            "          :hasAirwayJunction ?junction; " +
            "          :hasAirwaySection ?section; " +
            "          :airwayName ?o1;  " +
            "          :flightPurpose ?o2;  " +
            "          :createdAt ?o3;  " +
            "          :updatedAt ?o4; " +
            "          :droneId ?o5;  " +
            "          :hasAvailableDrone ?o6;  " +
            "          :airwayName ?o7.  " +
            "  ?junction a :AirwayJunction; " +
            "            :hasAirwayArea ?airwayArea; " +
            "            :hasDeviationArea ?deviationArea; " +
            "            :belongsToAirway ?airway; " +
            "            :airwayJunctionId ?o8; " +
            "            :airwayJunctionName ?o9. " +
            "  ?airwayArea a :AirwayArea; " +
            "              :hasCoordinate ?airwayCoordinate; " +
            "              :coordinates ?o10; " +
            "              :belongsToAirwayJunction ?junction; " +
            "              :hasGeometry ?areaGeometry." +
            "  ?areaGeometry a  ?geoType01;" +
            "                geo:asWKT ?areaWKT01." +
            "  ?deviationArea a :DeviationArea; " +
            "                 :hasCoordinate ?deviationCoordinate; " +
            "                 :coordinates ?o11; " +
            "                 :belongsToAirwayJunction ?junction; " +
            "                 :hasGeometry ?deviationGeometry." +
            "  ?deviationGeometry a  ?geoType02;" +
            "                     geo:asWKT ?areaWKT02." +
            "  ?airwayCoordinate a :Coordinate; " +
            "                    ?coordinateProperty ?o12. " +
            "  ?deviationCoordinate a :Coordinate; " +
            "                       ?coordinateProperty ?o13. " +
            "  ?section a :AirwaySection; " +
            "           :connectsJunction ?junction; " +
            "           :hasDronePort ?dronePort; " +
            "           :belongsToAirway ?airway; " +
            "           :airwaySectionId ?o14; " +
            "           :airwaySectionName ?o15; " +
            "           :dronePortId ?o16. " +
            "  ?dronePort :belongsToAirwaySection ?section. " +
            "} WHERE { " +
            "  BIND( \"%s\" AS ?airwayId ) " +
            "  ?admin :hasAirway ?airway. " +
            "  ?airway a :Airway ." +
            "  ?airway :airwayId ?airwayId ." +
            "  ?airway :belongsToAirwayAdministrator ?admin ." +
            "  OPTIONAL { ?airway :hasAirwayJunction ?junction . }" +
            "  OPTIONAL { ?airway :hasAirwaySection ?section . }" +
            "  OPTIONAL { ?airway :airwayName ?o1 . }" +
            "  OPTIONAL { ?airway :flightPurpose ?o2 . }" +
            "  OPTIONAL { ?airway :createdAt ?o3 . }" +
            "  OPTIONAL { ?airway :updatedAt ?o4 . }" +
            "  OPTIONAL { ?airway :droneId ?o5 . }" +
            "  OPTIONAL { ?airway :hasAvailableDrone ?o6 . }" +
            "  OPTIONAL { ?airway :airwayName ?o7 . }" +
            "  OPTIONAL { ?junction a :AirwayJunction . }" +
            "  OPTIONAL { ?junction :hasAirwayArea ?airwayArea . }" +
            "  OPTIONAL { ?junction :hasDeviationArea ?deviationArea . }" +
            "  OPTIONAL { ?junction :belongsToAirway ?airway . }" +
            "  OPTIONAL { ?junction :airwayJunctionId ?o8 . }" +
            "  OPTIONAL { ?junction :airwayJunctionName ?o9 . }" +
            "  OPTIONAL { ?airwayArea a :AirwayArea . }" +
            "  OPTIONAL { ?airwayArea :hasCoordinate ?airwayCoordinate . }" +
            "  OPTIONAL { ?airwayArea :coordinates ?o10 . }" +
            "  OPTIONAL { ?airwayArea :belongsToAirwayJunction ?junction . }" +
            "  OPTIONAL { ?airwayArea :hasGeometry ?areaGeometry . }" +
            "  OPTIONAL { ?areaGeometry a ?geoType01 . }" +
            "  OPTIONAL { ?areaGeometry geo:asWKT ?areaWKT01 . }" +
            "  OPTIONAL { ?deviationArea a :DeviationArea . }" +
            "  OPTIONAL { ?deviationArea :hasCoordinate ?deviationCoordinate . }" +
            "  OPTIONAL { ?deviationArea :coordinates ?o11 . }" +
            "  OPTIONAL { ?deviationArea :belongsToAirwayJunction ?junction . }" +
            "  OPTIONAL { ?deviationArea :hasGeometry ?deviationGeometry . }" +
            "  OPTIONAL { ?deviationGeometry a ?geoType02 . }" +
            "  OPTIONAL { ?deviationGeometry geo:asWKT ?areaWKT02 . }" +
            "  OPTIONAL { ?airwayCoordinate a :Coordinate . }" +
            "  OPTIONAL { ?airwayCoordinate ?coordinateProperty ?o12 . }" +
            "  OPTIONAL { ?deviationCoordinate a :Coordinate . }" +
            "  OPTIONAL { ?deviationCoordinate ?coordinateProperty ?o13 . }" +
            "  OPTIONAL { ?section a :AirwaySection . }" +
            "  OPTIONAL { ?section :connectsJunction ?junction . }" +
            "  OPTIONAL { ?section :hasDronePort ?dronePort . }" +
            "  OPTIONAL { ?section :belongsToAirway ?airway . }" +
            "  OPTIONAL { ?section :airwaySectionId ?o14 . }" +
            "  OPTIONAL { ?section :airwaySectionName ?o15 . }" +
            "  OPTIONAL { ?section :dronePortId ?o16 . }" +
            "  OPTIONAL { ?dronePort :belongsToAirwaySection ?section . }" +
            "}",
        airwayId);
    log.debug("DELETE QUERY = {}", query);
    return query;
  }

  /**
   * Creates a query that deletes all airways and their associated resources.
   */
  private String createDeleteAllAirwaysQuery() {

    String query = String.format(
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "PREFIX geo:  <http://www.opengis.net/ont/geosparql#> " +
            "PREFIX : <http://example.com/drone-highway#> " +
            "DELETE { " +
            "  ?admin :hasAirway ?airway. " +
            "  ?airway a :Airway ;" +
            "          :airwayId ?airwayId ;" +
            "          :belongsToAirwayAdministrator ?admin; " +
            "          :hasAirwayJunction ?junction; " +
            "          :hasAirwaySection ?section; " +
            "          :airwayName ?o1;  " +
            "          :flightPurpose ?o2;  " +
            "          :createdAt ?o3;  " +
            "          :updatedAt ?o4; " +
            "          :droneId ?o5;  " +
            "          :hasAvailableDrone ?o6;  " +
            "          :airwayName ?o7.  " +
            "  ?junction a :AirwayJunction; " +
            "            :hasAirwayArea ?airwayArea; " +
            "            :hasDeviationArea ?deviationArea; " +
            "            :belongsToAirway ?airway; " +
            "            :airwayJunctionId ?o8; " +
            "            :airwayJunctionName ?o9. " +
            "  ?airwayArea a :AirwayArea; " +
            "              :hasCoordinate ?airwayCoordinate; " +
            "              :coordinates ?o10; " +
            "              :belongsToAirwayJunction ?junction; " +
            "              :hasGeometry ?areaGeometry." +
            "  ?areaGeometry a  ?geoType01;" +
            "                geo:asWKT ?areaWKT01." +
            "  ?deviationArea a :DeviationArea; " +
            "                 :hasCoordinate ?deviationCoordinate; " +
            "                 :coordinates ?o11; " +
            "                 :belongsToAirwayJunction ?junction; " +
            "                 :hasGeometry ?deviationGeometry." +
            "  ?deviationGeometry a  ?geoType02;" +
            "                     geo:asWKT ?areaWKT02." +
            "  ?airwayCoordinate a :Coordinate; " +
            "                    ?coordinateProperty ?o12. " +
            "  ?deviationCoordinate a :Coordinate; " +
            "                       ?coordinateProperty ?o13. " +
            "  ?section a :AirwaySection; " +
            "           :connectsJunction ?junction; " +
            "           :hasDronePort ?dronePort; " +
            "           :belongsToAirway ?airway; " +
            "           :airwaySectionId ?o14; " +
            "           :airwaySectionName ?o15; " +
            "           :dronePortId ?o16. " +
            "  ?dronePort :belongsToAirwaySection ?section. " +
            "} WHERE { " +
            "  ?admin :hasAirway ?airway. " +
            "  ?airway a :Airway ;" +
            "          :airwayId ?airwayId ;" +
            "          :belongsToAirwayAdministrator ?admin; " +
            "          :hasAirwayJunction ?junction; " +
            "          :hasAirwaySection ?section; " +
            "          :airwayName ?o1;  " +
            "          :flightPurpose ?o2;  " +
            "          :createdAt ?o3;  " +
            "          :updatedAt ?o4; " +
            "          :droneId ?o5;  " +
            "          :hasAvailableDrone ?o6;  " +
            "          :airwayName ?o7.  " +
            "  ?junction a :AirwayJunction; " +
            "            :hasAirwayArea ?airwayArea; " +
            "            :hasDeviationArea ?deviationArea; " +
            "            :belongsToAirway ?airway; " +
            "            :airwayJunctionId ?o8; " +
            "            :airwayJunctionName ?o9. " +
            "  ?airwayArea a :AirwayArea; " +
            "              :hasCoordinate ?airwayCoordinate; " +
            "              :coordinates ?o10; " +
            "              :belongsToAirwayJunction ?junction; " +
            "              :hasGeometry ?areaGeometry." +
            "  ?areaGeometry a  ?geoType01;" +
            "                geo:asWKT ?areaWKT01." +
            "  ?deviationArea a :DeviationArea; " +
            "                 :hasCoordinate ?deviationCoordinate; " +
            "                 :coordinates ?o11; " +
            "                 :belongsToAirwayJunction ?junction; " +
            "                 :hasGeometry ?deviationGeometry." +
            "  ?deviationGeometry a  ?geoType02;" +
            "                     geo:asWKT ?areaWKT02." +
            "  ?airwayCoordinate a :Coordinate; " +
            "                    ?coordinateProperty ?o12. " +
            "  ?deviationCoordinate a :Coordinate; " +
            "                       ?coordinateProperty ?o13. " +
            "  ?section a :AirwaySection; " +
            "           :connectsJunction ?junction; " +
            "           :hasDronePort ?dronePort; " +
            "           :belongsToAirway ?airway; " +
            "           :airwaySectionId ?o14; " +
            "           :airwaySectionName ?o15; " +
            "           :dronePortId ?o16. " +
            "  ?dronePort :belongsToAirwaySection ?section. " +
            "}");
    log.debug("QUERY = {}", query);
    return query;
  }

  private String createDeleteDronePortByDronePortIdQuery(List<String> dronePortIds) {
    // Remove all double quotes from each dronePortId
    List<String> sanitizedIds = dronePortIds.stream()
        .map(id -> id.replaceAll("\"", ""))
        .collect(Collectors.toList());

    log.debug("dronePortIds = {}", sanitizedIds);

    // Construct the VALUES clause
    StringBuilder valuesClause = new StringBuilder("VALUES ?dronePortId { ");
    for (String id : sanitizedIds) {
      valuesClause.append("\"").append(id).append("\" ");
    }
    valuesClause.append("} ");

    // The association with the Airway Section is temporarily commented out
    String query = String.format(
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "PREFIX : <http://example.com/drone-port#> " +
            "PREFIX geo:  <http://www.opengis.net/ont/geosparql#> " +
            "DELETE { " +
            "  ?dronePort a :DronePort ; " +
            "             :dronePortId ?dronePortId ; " +
            "             :dronePortName ?o1 ; " +
            "             :address ?o2 ; " +
            "             :manufacturer ?o3 ; " +
            "             :dronePortManufacturerId ?o4 ; " +
            "             :visDronePortCompanyId ?o5 ; " +
            "             :storedAircraftId ?o6 ; " +
            "             :storesAircraft ?o7 ; " +
            "             :serialNumber ?o8 ; " +
            "             :portType ?o9 ; " +
            "             :portTypeName ?o10 ; " +
            "             :hasPortType ?o11 ; " +
            "             :supportDroneType ?o12 ; " +
            "             :portUsageType ?o13 ; " +
            "             :portUsageTypeName ?o14 ; " +
            "             :hasPortUsageType ?o15 ; " +
            "             :activeStatus ?o16 ; " +
            "             :dataModelType ?o17 ; " +
            "             :dronePortType ?o18 ; " +
            "             :dronePortTypeName ?o19 ; " +
            "             :hasDronePortType ?o20 ; " +
            "             :inactiveTimeFrom ?o21 ; " +
            "             :inactiveTimeTo ?o22 ; " +
            "             :imageData ?o23 ; " +
            "             :updateTime ?o24 ; " +
            "             :latitude ?o25 ; " +
            "             :longitude ?o26 ; " +
            "             :altitude ?o27 ; " +
            "             :hasGeometry ?geometry . " +
            "  ?geometry a ?geoType;  " +
            "            geo:asWKT ?WKT01 . " +
            "} WHERE { " +
            valuesClause.toString() +
            "  ?dronePort a :DronePort ; " +
            "             :dronePortId ?dronePortId . " +
            "  OPTIONAL { ?dronePort :dronePortName ?o1 } . " +
            "  OPTIONAL { ?dronePort :address ?o2 } . " +
            "  OPTIONAL { ?dronePort :manufacturer ?o3 } . " +
            "  OPTIONAL { ?dronePort :dronePortManufacturerId ?o4 } . " +
            "  OPTIONAL { ?dronePort :visDronePortCompanyId ?o5 } . " +
            "  OPTIONAL { ?dronePort :storedAircraftId ?o6 } . " +
            "  OPTIONAL { ?dronePort :storesAircraft ?o7 } . " +
            "  OPTIONAL { ?dronePort :serialNumber ?o8 } . " +
            "  OPTIONAL { ?dronePort :portType ?o9 } . " +
            "  OPTIONAL { ?dronePort :portTypeName ?o10 } . " +
            "  OPTIONAL { ?dronePort :hasPortType ?o11 } . " +
            "  OPTIONAL { ?dronePort :supportDroneType ?o12 } . " +
            "  OPTIONAL { ?dronePort :portUsageType ?o13 } . " +
            "  OPTIONAL { ?dronePort :portUsageTypeName ?o14 } . " +
            "  OPTIONAL { ?dronePort :hasPortUsageType ?o15 } . " +
            "  OPTIONAL { ?dronePort :activeStatus ?o16 } . " +
            "  OPTIONAL { ?dronePort :dataModelType ?o17 } . " +
            "  OPTIONAL { ?dronePort :dronePortType ?o18 } . " +
            "  OPTIONAL { ?dronePort :dronePortTypeName ?o19 } . " +
            "  OPTIONAL { ?dronePort :hasDronePortType ?o20 } . " +
            "  OPTIONAL { ?dronePort :inactiveTimeFrom ?o21 } . " +
            "  OPTIONAL { ?dronePort :inactiveTimeTo ?o22 } . " +
            "  OPTIONAL { ?dronePort :imageData ?o23 } . " +
            "  OPTIONAL { ?dronePort :updateTime ?o24 } . " +
            "  OPTIONAL { ?dronePort :latitude ?o25 } . " +
            "  OPTIONAL { ?dronePort :longitude ?o26 } . " +
            "  OPTIONAL { ?dronePort :altitude ?o27 } . " +
            "  OPTIONAL { ?dronePort :hasGeometry ?geometry } . " +
            "  OPTIONAL { ?geometry a ?geoType;  " +
            "                       geo:asWKT ?WKT01 } . " +
            "}");
    log.debug("DELETE QUERY = {}", query);
    return query;
  }

}

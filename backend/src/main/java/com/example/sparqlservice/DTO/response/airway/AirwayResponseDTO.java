package com.example.sparqlservice.DTO.response.airway;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirwayResponseDTO {
  private Airway airway;

  @Override
  public String toString() {
    return "AirwayResponseDTO{\n" +
        "  airway=" + airway + "\n" +
        "}\n";
  }

  @Getter
  @Setter
  public static class Airway {
    private String airwayAdministratorId;
    private String businessNumber;
    private List<AirwayInfo> airways;

    @Override
    public String toString() {
      return "Airway{\n" +
          "  airwayAdministratorId='" + airwayAdministratorId + "'\n" +
          "  businessNumber='" + businessNumber + "'\n" +
          "  airways=" + airways + "\n" +
          "}\n";
    }
  }

  @Getter
  @Setter
  public static class AirwayInfo {
    private String airwayId;
    private String airwayName;
    private String flightPurpose;
    private String createdAt;
    private String updatedAt;
    private List<String> droneList;
    private List<AirwayJunction> airwayJunctions;
    private List<AirwaySection> airwaySections;

    @Override
    public String toString() {
      return "AirwayInfo{\n" +
          "  airwayId='" + airwayId + "'\n" +
          "  airwayName='" + airwayName + "'\n" +
          "  flightPurpose='" + flightPurpose + "'\n" +
          "  createdAt='" + createdAt + "'\n" +
          "  updatedAt='" + updatedAt + "'\n" +
          "  droneList=" + droneList + "\n" +
          "  airwayJunctions=" + airwayJunctions + "\n" +
          "  airwaySections=" + airwaySections + "\n" +
          "}\n";
    }

    public List<AirwaySection> getAirwaySections() {
      return airwaySections;
    }
  }

  @Getter
  @Setter
  public static class AirwayJunction {
    private String airwayJunctionId;
    private String airwayJunctionName;
    private List<AirwayGeometry> airways;

    @Override
    public String toString() {
      return "AirwayJunction{\n" +
          "  airwayJunctionId='" + airwayJunctionId + "'\n" +
          "  airwayJunctionName='" + airwayJunctionName + "'\n" +
          "  airways=" + airways + "\n" +
          "}\n";
    }
  }

  @Getter
  @Setter
  public static class AirwayGeometry {
    private GeoJSONFeature airway;
    private GeoJSONFeature deviation;

    @Override
    public String toString() {
      return "AirwayGeometry{\n" +
          "  airway=" + airway + "\n" +
          "  deviation=" + deviation + "\n" +
          "}\n";
    }
  }

  @Getter
  @Setter
  public static class GeoJSONFeature {
    private Geometry geometry;

    @Override
    public String toString() {
      return "GeoJSONFeature{\n" +
          "  geometry=" + geometry + "\n" +
          "}\n";
    }
  }

  @Getter
  @Setter
  public static class Geometry {
    private String type;
    private List<List<Double>> coordinates;

    public Double getLongitude(int index) {
      return coordinates.get(index).get(0);
    }

    public Double getLatitude(int index) {
      return coordinates.get(index).get(1);
    }

    public Double getHeight(int index) {
      return coordinates.get(index).get(2);
    }

    @Override
    public String toString() {
      return "Geometry{\n" +
          "  type='" + type + "'\n" +
          "  coordinates=" + coordinates + "\n" +
          "}\n";
    }
  }

  @Getter
  @Setter
  public static class AirwaySection {
    private String airwaySectionId;
    private String airwaySectionName;
    private List<String> airwayJunctionIds;
    private List<String> droneportIds;

    @Override
    public String toString() {
      return "AirwaySection{\n" +
          "airwaySectionId='" + airwaySectionId + "'\n" +
          ", airwaySectionName='" + airwaySectionName + "'\n" +
          ", airwayJunctionIds=" + airwayJunctionIds + "\n" +
          ", droneportIds=" + droneportIds + "\n" +
          '}';
    }
  }

  /**
   * Retrieves a list of all unique DronePortIds present in this DTO.
   *
   * @return a list of unique DronePortIds
   */
  public List<String> getAllDronePortIds() {
    return airway.getAirways().stream()
        .flatMap(airwayInfo -> airwayInfo.getAirwaySections().stream()
            .flatMap(airwaySection -> Optional.ofNullable(airwaySection.getDroneportIds())
                .map(List::stream)
                .orElseGet(Stream::empty)))
        .distinct()
        .collect(Collectors.toList());
  }
}
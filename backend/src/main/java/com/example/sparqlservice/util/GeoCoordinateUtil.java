package com.example.sparqlservice.util;

import java.util.List;

/**
 * Utility class for converting geographic coordinates into Well-Known Text (WKT) format.
 */
public class GeoCoordinateUtil {

  /**
   * Transforms GeoJSON coordinates into the GeoSPARQL WKT format for 3D polygon
   * features.
   *
   * Example:
   *
   * Given the GeoJSON coordinates:
   * [[[-73.97, 40.77, 0], [-73.96, 40.78, 0], [-73.95, 40.77, 0], [-73.97, 40.77,
   * 0]]],
   * the resulting WKT would be:
   * POLYGON Z((-73.97 40.77 0, -73.96 40.78 0, -73.95 40.77 0, -73.97 40.77 0))
   *
   * @param coordinates List of GeoJSON coordinates [lon-lat-alt]
   * @return WKT representation of the polygon
   */
  public static String convertPolygonCoordinatesToWKT(List<List<Double>> coordinates) {
    StringBuilder wkt = new StringBuilder("POLYGON Z((");
    for (int i = 0; i < coordinates.size(); i++) {
      List<Double> point = coordinates.get(i);
      wkt.append(point.get(0)).append(" ")
          .append(point.get(1)).append(" ")
          .append(point.get(2));
      if (i < coordinates.size() - 1) {
        wkt.append(", ");
      }
    }

    wkt.append("))");
    return wkt.toString();
  }

  /**
   * Converts 3D geographic coordinates [lon-lat-alt] into a WKT representation
   * of a point with Z-coordinate.
   */
  public static String convertPointZCoordinatesToWKT(double lon, double lat, double alt) {
    StringBuilder wkt = new StringBuilder("POINT Z (");
    wkt.append(lon).append(" ")
       .append(lat).append(" ")
       .append(alt);
    wkt.append(")");

    return wkt.toString();
  }

  /**
   * Converts 2D geographic coordinates [lon-lat] into a WKT representation
   * of a point.
   */
  public static String convertPointCoordinatesToWKT(double lon, double lat) {
    StringBuilder wkt = new StringBuilder("POINT (");
    wkt.append(lon).append(" ")
       .append(lat).append(" ");
    wkt.append(")");

    return wkt.toString();
  }
}

package com.example.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.DTO.response.DronePortReservationResponseDTO;
import com.example.constants.Config;
import com.example.converter.impl.DronePortReservationConverter;
import com.example.util.UriUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DronePortService {
  private static final Logger logger = LoggerFactory.getLogger(DronePortService.class);

  private final DronePortReservationConverter converter = new DronePortReservationConverter(Config.API_URL);
  private final String dronePortApiUrl = Config.DRONE_PORT_API_URL;
  private final String dronePortReservationPath = "/droneport/reserve/list";

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final ThreadLocal<Long> lastUpdatedTime = ThreadLocal.withInitial(System::currentTimeMillis);
  private static final ThreadLocal<Model> dronePortReservationModel = ThreadLocal
      .withInitial(ModelFactory::createDefaultModel);

  public Model getDronePortReservationModel(String serviceURI) throws Exception {
    Model model = dronePortReservationModel.get();
    long lastUpdated = lastUpdatedTime.get();

    if (System.currentTimeMillis() - lastUpdated < TimeUnit.SECONDS.toMillis(Config.CACHE_TIME) && model != null
        && !model.isEmpty()) {
      logger.debug("Returning cached DronePortReservationModel.");
      return model;
    }

    // When the model is NOT up-to-date.
    logger.debug("############## DRONE PORT RESERVATION ##############");
    logger.debug("Current Thread: {}", Thread.currentThread().getName());
    logger.debug("Current Time: {}, Last Updated: {}", System.currentTimeMillis(), lastUpdated);
    logger.debug("Period: {}[ms]", System.currentTimeMillis() - lastUpdated);
    logger.info("DronePortReservationModel is null or empty. Executing API request...");

    dronePortReservationModel.remove();

    model = ModelFactory.createDefaultModel();
    dronePortReservationModel.set(model);

    try {
      int currentPage = 1;
      int lastPage = -1;
      while (currentPage == 1 || currentPage <= lastPage) {
        logger.debug("Current Page: {}, Last Page: {}", currentPage, lastPage);
        List<DronePortReservationResponseDTO> responseDTO = fetchReservationFromAPI(serviceURI, currentPage);
        if (responseDTO.size() == 0) {
          break;
        }
        lastPage = responseDTO.getFirst().getLastPage();

        for (DronePortReservationResponseDTO dto : responseDTO) {
          model = ModelFactory.createUnion(model, converter.convert(dto));
        }

        currentPage += 1;
      }
      dronePortReservationModel.set(model);
      lastUpdatedTime.set(System.currentTimeMillis());
    } catch (Exception e) {
      logger.error("An error occurred while fetching and converting drone port reservations", e);
    }
    return dronePortReservationModel.get();
  }

  /**
   * Fetches a list of drone port reservations from the API using the provided
   * service URI and page number.
   *
   * @param serviceURI the service URI
   * @param page       the page number to fetch from the API
   * @return a list of DronePortReservationResponseDTO objects
   * @throws IOException              if an I/O error occurs when sending or
   *                                  receiving
   *                                  the request
   * @throws InterruptedException     if the operation is interrupted
   * @throws IllegalArgumentException if the page number is less than or equal to
   *                                  zero
   * @throws RuntimeException         if the API response status code is not 200
   */
  private List<DronePortReservationResponseDTO> fetchReservationFromAPI(String serviceURI, int page)
      throws IOException, InterruptedException {

    if (page <= 0) {
      throw new IllegalArgumentException("Page number must be greater than zero.");
    }

    logger.debug("Starting to fetch reservations from DronePort Reservation API...");
    logger.debug("Fetching reservations for page: {}", page);
    List<DronePortReservationResponseDTO> dtoList = new ArrayList<>();

    Map<String, Object> parameters = UriUtil.setRequestParametersFromURI(serviceURI);

    logger.debug("Parameters = {}", parameters);

    URI uri = URI.create(dronePortApiUrl + dronePortReservationPath +
        createQueryParameter(parameters, page));

    HttpClient httpClient = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofSeconds(10))
        .header("Content-Type", "application/json")
        .GET()
        .build();
    logger.info("Sending request to Drone Port API at: {}", uri);

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200) {
      logger.info("Successfully fetched data from Drone Port API.");
      DronePortReservationResponseDTO[] responseDto = objectMapper.readValue(response.body(),
          DronePortReservationResponseDTO[].class);
      dtoList = Arrays.asList(responseDto);
    } else {
      logger.error("Failed to fetch data from Drone Port API. Status code: {}, Response body: {}",
          response.statusCode(), response.body());
      throw new RuntimeException("External API ResponseCode = " + response.statusCode());
    }
    return dtoList;
  }

  private String createQueryParameter(Map<String, Object> parameters, int page) {

    // overwrite the page value
    parameters.put("page", page);

    // set example parameter for prism mock server
    parameters.put("__example", "page" + page);

    StringBuilder queryBuilder = new StringBuilder("?");
    parameters.forEach((key, value) -> {
      if (value != null && !value.toString().isEmpty()) {
        String sanitizedValue = value.toString().replace("\"", "");

        queryBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
            .append("=")
            .append(URLEncoder.encode(sanitizedValue, StandardCharsets.UTF_8))
            .append("&");
      }
    });
    // Remove the trailing '&' if present
    if (queryBuilder.length() > 1) {
      queryBuilder.setLength(queryBuilder.length() - 1);
    }
    return queryBuilder.toString();
  }
}
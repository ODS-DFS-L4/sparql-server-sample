package com.example.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.DTO.request.AirwayReservationRequestBodyDTO;
import com.example.DTO.response.AirwayReservationListResponseDTO;
import com.example.DTO.response.AirwayReservationResponseDTO;
import com.example.constants.Config;
import com.example.converter.impl.AirwayReservationConverter;
import com.example.util.UriUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AirwayService {
  private static final Logger logger = LoggerFactory.getLogger(AirwayService.class);

  private final AirwayReservationConverter converter = new AirwayReservationConverter(Config.API_URL);
  private final String airwayReservationApiUrl = Config.AIRWAY_RESERVATION_API_URL;
  private final String airwayReservationPath = "/airwayReservations/search";

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final ThreadLocal<Long> lastUpdatedTime = ThreadLocal.withInitial(System::currentTimeMillis);
  private static final ThreadLocal<Model> airwayReservationModel = ThreadLocal
      .withInitial(ModelFactory::createDefaultModel);

  public Model getAirwayReservationModel(String serviceURI) {
    Model model = airwayReservationModel.get();
    long lastUpdated = lastUpdatedTime.get();

    if (System.currentTimeMillis() - lastUpdated < TimeUnit.SECONDS.toMillis(Config.CACHE_TIME) && model != null
        && !model.isEmpty()) {
      logger.debug("Returning cached AirwayReservationModel.");
      return model;
    }

    // When the model is NOT up-to-date.
    logger.debug("############## AIRWAY RESERVATION ##############");
    logger.debug("Current Thread: {}", Thread.currentThread().getName());
    logger.debug("Current Time: {}, Last Updated: {}", System.currentTimeMillis(), lastUpdated);
    logger.debug("Period: {}[ms]", System.currentTimeMillis() - lastUpdated);
    logger.info("AirwayReservationModel is null or empty. Executing API request...");

    airwayReservationModel.remove();

    model = ModelFactory.createDefaultModel();
    airwayReservationModel.set(model);

    try {
      int currentPage = 1;
      int lastPage = -1;
      while (currentPage == 1 || currentPage <= lastPage) {
        logger.debug("Current Page: {}, Last Page: {}", currentPage, lastPage);
        AirwayReservationListResponseDTO responseDTO = fetchReservationFromAPI(serviceURI, currentPage);
        lastPage = responseDTO.getLastPage();

        for (AirwayReservationResponseDTO dto : responseDTO.getResult()) {
          model = ModelFactory.createUnion(model, converter.convert(dto));
        }

        currentPage += 1;
      }

      airwayReservationModel.set(model);
      lastUpdatedTime.set(System.currentTimeMillis());
    } catch (Exception e) {
      logger.error("An error occurred while fetching and converting drone port reservations", e);
    }
    return airwayReservationModel.get();
  }

  /**
   * Fetches a list of airway reservations from the API using the provided service
   * URI and page number.
   *
   * @param serviceURI the service URI
   * @param page       the page number to fetch from the API
   * @return an AirwayReservationListResponseDTO object
   * @throws IOException              if an I/O error occurs when sending or
   *                                  receiving the request
   * @throws InterruptedException     if the operation is interrupted
   * @throws IllegalArgumentException if the page number is less than or equal to
   *                                  zero
   */
  private AirwayReservationListResponseDTO fetchReservationFromAPI(String serviceURI, int page)
      throws IOException, InterruptedException {
    if (page <= 0) {
      throw new IllegalArgumentException("Page number must be greater than zero.");
    }

    logger.debug("Starting to fetch reservations from Airway Reservation API...");
    logger.debug("Fetching reservations for page: {}", page);
    AirwayReservationListResponseDTO responseDTO = new AirwayReservationListResponseDTO();

    Map<String, Object> parameters = UriUtil.setRequestParametersFromURI(serviceURI);
    logger.debug("Parameters = {}", parameters);

    BodyPublisher requestBody = createRequestBody(parameters);
    String apiKey = Config.AIRWAY_RESERVATION_API_KEY;
    String authHeaderName = Config.AIRWAY_RESERVATION_AUTH_HEADER_NAME;
    URI uri = URI.create(airwayReservationApiUrl + airwayReservationPath + createQueryParameter(page));

    HttpClient httpClient = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofSeconds(10))
        .header("Content-Type", "application/json")
        .header(authHeaderName, apiKey)
        .POST(requestBody)
        .build();
    logger.info("Sending request to Airway Reservation API at: {}", uri);

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200) {
      logger.info("Successfully fetched data from Airway Reservation API.");
      responseDTO = objectMapper.readValue(response.body(),
          AirwayReservationListResponseDTO.class);
    } else {
      logger.error("Failed to fetch data from Airway Reservation API. Status code: {}, Response body: {}",
          response.statusCode(), response.body());
      throw new RuntimeException("External API ResponseCode = " + response.statusCode());
    }
    return responseDTO;
  }

  private String createQueryParameter(int page) {
    String pageParameter = "page=" + Integer.toString(page);
    // set example parameter for prism mock server
    String prismParameter = "__example=" + "page" + Integer.toString(page);
    StringBuilder builder = new StringBuilder();
    builder.append("?")
        .append(pageParameter)
        .append("&")
        .append(prismParameter);
    return builder.toString();
  }

  private BodyPublisher createRequestBody(Map<String, Object> parameters) {
    AirwayReservationRequestBodyDTO requestBody = new AirwayReservationRequestBodyDTO();

    if (parameters.get("leftBottom") != null && !parameters.get("leftBottom").toString().isEmpty()) {
      requestBody.setLeftBottom(parameters.get("leftBottom").toString());
    }
    if (parameters.get("topRight") != null && !parameters.get("topRight").toString().isEmpty()) {
      requestBody.setTopRight(parameters.get("topRight").toString());
    }
    if (parameters.get("startAt") != null && !parameters.get("startAt").toString().isEmpty()) {
      requestBody.setStartAt(parameters.get("startAt").toString());
    }
    if (parameters.get("endAt") != null && !parameters.get("endAt").toString().isEmpty()) {
      requestBody.setEndAt(parameters.get("endAt").toString());
    }

    logger.debug("Airway Reservation Request Body: {}", requestBody);

    try {
      String json = objectMapper.writeValueAsString(requestBody);
      return HttpRequest.BodyPublishers.ofString(json);
    } catch (IOException e) {
      logger.error("Error creating JSON request body", e);
      throw new RuntimeException("Failed to create request body", e);
    }
  }
}

package com.example.sparqlservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sparqlservice.DTO.request.DronePortIntrusionRequestDTO;
import com.example.sparqlservice.service.DronePortIntrusionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "ドローンポート立入り通知", description = "ドローンポートへの立入り通知を管理するエンドポイント。")
@Slf4j
@RestController
@RequestMapping("/api/v1/drone-ports/intrusions")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successful response"),
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
@RequiredArgsConstructor
public class DronePortIntrusionController {
  private final DronePortIntrusionService dronePortIntrusionService;

  private static final String EXAMPLE_OBJECT = "{\n" +
      "  \"dronePortId\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
      "  \"timestamp\": \"2025-01-24T14:30:00Z\",\n" +
      "  \"anyDetection\": true,\n" +
      "  \"events\": [\n" +
      "    {\n" +
      "      \"objectId\": \"car-1\",\n" +
      "      \"objectType\": \"car\",\n" +
      "      \"detectionStatus\": true,\n" +
      "      \"location\": {\n" +
      "        \"latitude\": 35.6895,\n" +
      "        \"longitude\": 139.6917\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"objectId\": \"animal-2\",\n" +
      "      \"objectType\": \"animal\",\n" +
      "      \"detectionStatus\": false,\n" +
      "      \"location\": {\n" +
      "        \"latitude\": 35.6897,\n" +
      "        \"longitude\": 139.692\n" +
      "      }\n" +
      "    }\n" +
      "  ],\n" +
      "  \"reportEndpointUrl\": \"https://example.com/report\"\n" +
      "}";

  @PostMapping("/status")
  @Operation(summary = "指定されたドローンポートの立入り状態を更新する。", description = "RDFモデルに変換され、トリプルストアにて永続化される。", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject(value = EXAMPLE_OBJECT)), required = true))
  public ResponseEntity<?> updateIntrusionEvents(
      @RequestHeader(value = "Content-Type", required = true) String contentType,
      @RequestHeader(value = "Authorization", required = true) String authHeader,
      @Validated @RequestBody DronePortIntrusionRequestDTO dto) {
    try {
      dronePortIntrusionService.updateDronePortIntrusionByDronePortId(dto);
      return ResponseEntity.ok()
          .body("DronePort Intrusion status updated successfully.");
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Error updating DronePort Intrusion status: " + e.getMessage());
    }
  }
}

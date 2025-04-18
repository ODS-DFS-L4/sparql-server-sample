package com.example.sparqlservice.DTO.request;

import java.util.List;

import com.example.sparqlservice.validator.ValidLocation;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "状態通知アプリからローカルデータ管理への通知で利用されるデータ")
public class DronePortIntrusionRequestDTO {
  @NotBlank
  @Schema(description = "ドローンポートID", requiredMode = RequiredMode.REQUIRED)
  private String dronePortId;

  @NotBlank
  @Schema(description = "立ち入り検知の日時(ISO8601形式)", requiredMode = RequiredMode.REQUIRED)
  private String timestamp;

  @NotNull
  @Schema(description = "立ち入り状態の代表値", requiredMode = RequiredMode.REQUIRED)
  private Boolean anyDetection;

  @NotNull
  @Valid
  @Schema(description = "立ち入り検知の情報", requiredMode = RequiredMode.REQUIRED)
  private List<DetectionEvent> events;

  @Schema(description = "レポートファイルのエンドポイント", requiredMode = RequiredMode.NOT_REQUIRED)
  private String reportEndpointUrl; // Not Required

  @Override
  public String toString() {
    return "DronePortIntrusionRequestDTO{" +
        "dronePortId='" + dronePortId + '\'' +
        ", timestamp='" + timestamp + '\'' +
        ", anyDetection=" + anyDetection +
        ", events=" + events +
        ", reportEndpointUrl='" + reportEndpointUrl + '\'' +
        '}';
  }

  @Getter
  @Setter
  public static class DetectionEvent {
    @NotBlank
    @Schema(description = "障害物の識別子", requiredMode = RequiredMode.REQUIRED)
    private String objectId;

    @NotBlank
    @Schema(description = "障害物の種別", requiredMode = RequiredMode.REQUIRED)
    private String objectType;

    @NotNull
    @Schema(description = "検知状態", requiredMode = RequiredMode.REQUIRED)
    private Boolean detectionStatus;

    @Valid
    @ValidLocation
    @Schema(description = "位置情報", requiredMode = RequiredMode.NOT_REQUIRED)
    private Location location; // Not Required

    @Override
    public String toString() {
      return "DetectionEvent{" +
          "objectId='" + objectId + '\'' +
          ", objectType='" + objectType + '\'' +
          ", detectionStatus=" + detectionStatus +
          ", location=" + location +
          '}';
    }

    @Getter
    @Setter
    public static class Location {
      @Schema(description = "緯度", requiredMode = RequiredMode.REQUIRED)
      private Double latitude;
      @Schema(description = "経度", requiredMode = RequiredMode.REQUIRED)
      private Double longitude;

      @Override
      public String toString() {
        return "Location{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            '}';
      }
    }
  }
}

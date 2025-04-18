package com.example.sparqlservice.DTO.response.dronePort;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DronePortResponseDTO {
    private String dronePortId;
    private String dronePortName;
    private String address;
    private String manufacturer;
    private String serialNumber;
    private String dronePortManufacturerId;
    private Integer portType;
    private String visDronePortCompanyId;
    private String storedAircraftId;
    private Double lat;
    private Double lon;
    private Double alt;
    private String supportDroneType;
    private Integer usageType;
    private String activeStatus;
    private String inactiveTimeFrom;
    private String inactiveTimeTo;
    private String imageData;
    private String updateTime;
    private Integer perPage;
    private Integer currentPage;
    private Integer lastPage;
    private Integer total;

    @Override
    public String toString() {
        return "DronePortResponseDTO{\n" +
                "  dronePortId='" + dronePortId + "'\n" +
                "  dronePortName='" + dronePortName + "'\n" +
                "  address='" + address + "'\n" +
                "  manufacturer='" + manufacturer + "'\n" +
                "  serialNumber='" + serialNumber + "'\n" +
                "  dronePortManufacturerId='" + dronePortManufacturerId + "'\n" +
                "  portType=" + portType + "\n" +
                "  visDronePortCompanyId='" + visDronePortCompanyId + "'\n" +
                "  storedAircraftId='" + storedAircraftId + "'\n" +
                "  lat=" + lat + "\n" +
                "  lon=" + lon + "\n" +
                "  alt=" + alt + "\n" +
                "  supportDroneType='" + supportDroneType + "'\n" +
                "  usageType=" + usageType + "\n" +
                "  activeStatus='" + activeStatus + "'\n" +
                "  inactiveTimeFrom=" + inactiveTimeFrom + "\n" +
                "  inactiveTimeTo=" + inactiveTimeTo + "\n" +
                "  imageData='" + imageData + "'\n" +
                "  updateTime=" + updateTime + "\n" +
                "  perPage=" + perPage + "\n" +
                "  currentPage=" + currentPage + "\n" +
                "  lastPage=" + lastPage + "\n" +
                "  total=" + total + "\n" +
                "}\n";
    }
}
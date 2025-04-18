package com.example.DTO.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirwayReservationResponseDTO {

    private String airwayReservationId;
    private String operatorId;
    private List<String> airwaySectionIds;
    private String startAt;
    private String endAt;
    private String reservedAt;

    @Override
    public String toString() {
        return "DroneHighwayReservationDTO{\n" +
                "  airwayReservationId='" + airwayReservationId + "'\n" +
                "  operatorId='" + operatorId + "'\n" +
                "  airwaySectionIds=" + airwaySectionIds + "\n" +
                "  startAt='" + startAt + "'\n" +
                "  endAt='" + endAt + "'\n" +
                "  reservedAt='" + reservedAt + "'\n" +
                "}\n";
    }
}

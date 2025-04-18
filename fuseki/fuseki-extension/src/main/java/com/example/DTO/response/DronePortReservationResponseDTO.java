package com.example.DTO.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DronePortReservationResponseDTO {

    private String dronePortReservationId;
    private String dronePortId;
    private String aircraftId;
    private String routeReservationId;
    private Integer usageType;
    private String reservationTimeFrom;
    private String reservationTimeTo;
    private String dronePortName;
    private String aircraftName;
    private Boolean reservationActiveFlag;
    private String inactiveTimeFrom;
    private String inactiveTimeTo;
    private String visDronePortCompanyId;
    private Integer currentPage;
    private Integer lastPage;
    private Integer total;

    @Override
    public String toString() {
        return "Reservation{\n" +
                "  dronePortReservationId='" + dronePortReservationId + "'\n" +
                "  dronePortId='" + dronePortId + "'\n" +
                "  aircraftId='" + aircraftId + "'\n" +
                "  routeReservationId='" + routeReservationId + "'\n" +
                "  usageType=" + usageType + "\n" +
                "  reservationTimeFrom='" + reservationTimeFrom + "'\n" +
                "  reservationTimeTo='" + reservationTimeTo + "'\n" +
                "  dronePortName='" + dronePortName + "'\n" +
                "  aircraftName=" + aircraftName + "\n" +
                "  reservationActiveFlag=" + reservationActiveFlag + "\n" +
                "  inactiveTimeFrom='" + inactiveTimeFrom + "'\n" +
                "  inactiveTimeTo='" + inactiveTimeTo + "'\n" +
                "  visDronePortCompanyId='" + visDronePortCompanyId + "'\n" +
                "  currentPage=" + currentPage + "\n" +
                "  lastPage=" + lastPage + "\n" +
                "  total=" + total + "\n" +
                "}\n";
    }
}

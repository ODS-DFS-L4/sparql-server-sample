package com.example.DTO.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AirwayReservationListResponseDTO {

  private Integer currentPage;
  private Integer lastPage;
  private Integer perPage;
  private Integer total;
  private List<AirwayReservationResponseDTO> result;

  @Override
  public String toString() {
    return "AirwayReservationListResponseDTO{\n" +
        "  currentPage=" + currentPage + "\n" +
        "  lastPage=" + lastPage + "\n" +
        "  perPage=" + perPage + "\n" +
        "  total=" + total + "\n" +
        "  airwayReservation=" + result + "\n" +
        "}\n";
  }
}
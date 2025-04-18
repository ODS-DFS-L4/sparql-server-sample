package com.example.DTO.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirwayReservationRequestBodyDTO {
    @JsonProperty("left_bottom")
    private String leftBottom;

    @JsonProperty("top_right")
    private String topRight;

    @JsonProperty("start_at")
    private String startAt;

    @JsonProperty("end_at")
    private String endAt;

    public AirwayReservationRequestBodyDTO(String leftBottom, String topRight, String startAt, String endAt) {
        this.leftBottom = leftBottom;
        this.topRight = topRight;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    @Override
    public String toString() {
        return "AirwayReservationRequestBodyDTO{\n" +
                "  leftBottom='" + leftBottom + "'\n" +
                "  topRight='" + topRight + "'\n" +
                "  startAt='" + startAt + "'\n" +
                "  endAt='" + endAt + "'\n" +
                "}\n";
    }
}

package org.codec.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeleteGasStationRequest {
    @JsonProperty("station_id")
    private Long stationId;
}

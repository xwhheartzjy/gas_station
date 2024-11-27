package org.codec.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddGasStationRequest {
    @JsonProperty("gas_station_name")
    private String GasStationName;

    @JsonProperty("longitude")
    private String longitude;

    @JsonProperty("latitude")
    private String latitude;

}

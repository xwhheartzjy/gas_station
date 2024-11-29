package org.codec.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class AddGasStationRequest {
    @JsonProperty("gas_station_name")
    private String GasStationName;

    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("station_type")
    private String stationType;

    @JsonProperty("business_start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private Date businessStartTime;

    @JsonProperty("business_end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private Date businessEndTime;

    @JsonProperty("car_wash")
    private Integer carWash;





}

package org.codec.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private LocalTime businessStartTime;

    @JsonProperty("business_end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime businessEndTime;

    @JsonProperty("car_wash")
    private Integer carWash;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("address")
    private String address;

    @JsonProperty("area_id")
    private Long areaId;

    @JsonProperty("station_id")
    private Long stationId;





}

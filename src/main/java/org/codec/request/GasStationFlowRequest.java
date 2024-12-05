package org.codec.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GasStationFlowRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("gas_station_id")  // 映射请求体中的 gas_station_id 字段
    private Long gasStationId;

    @JsonProperty("gas_station_name")  // 映射请求体中的 gas_station_name 字段
    private String gasStationName;

    @JsonProperty("oil_0")  // 映射请求体中的 oil_0 字段
    private String oil0;

    @JsonProperty("oil_92")  // 映射请求体中的 oil_92 字段
    private String oil92;

    @JsonProperty("oil_95")  // 映射请求体中的 oil_95 字段
    private String oil95;

    @JsonProperty("oil_98")  // 映射请求体中的 oil_98 字段
    private String oil98;

    @JsonProperty("user_id")  // 映射请求体中的 user_id 字段
    private Long userId;

    @JsonProperty("address")
    private String address;

    @JsonProperty("lng")
    private String lng; // 经度

    @JsonProperty("lat")
    private String lat; // 纬度
}

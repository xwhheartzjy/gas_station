package org.codec.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Data
public class GasStationDTO {
    private String station_id; // 站点ID
    private String gas_station_name; // 油站名称

    private List<GasAreaDTO> areas;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalTime business_start_time;
    private LocalTime business_end_time;
    private String station_type;
    private Integer car_wash;
}

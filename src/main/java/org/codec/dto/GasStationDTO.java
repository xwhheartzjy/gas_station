package org.codec.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GasStationDTO {
    private Long station_id; // 站点ID
    private String name; // 油站名称
}

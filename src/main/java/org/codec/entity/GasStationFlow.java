package org.codec.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("gas_station_flow")  // 映射表名
public class GasStationFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")  // 映射主键字段
    private String id;

    @TableField("gas_station_id")  // 映射油站ID字段
    private Long gasStationId;

    @TableField("gas_station_name")  // 映射油站名称字段
    private String gasStationName;

    @TableField("oil_0")  // 映射0号柴油字段
    private String oil0;

    @TableField("oil_92")  // 映射92号汽油字段
    private String oil92;

    @TableField("oil_95")  // 映射95号汽油字段
    private String oil95;

    @TableField("oil_98")  // 映射98号汽油字段
    private String oil98;

    @TableField("user_id")  // 映射用户ID字段
    private Long userId;

    @TableField("address")
    private String address;

    @TableField("lng")
    private String lng; // 经度

    @TableField("lat")
    private String lat; // 纬度

    @TableField("flow")
    private Boolean flow;
}

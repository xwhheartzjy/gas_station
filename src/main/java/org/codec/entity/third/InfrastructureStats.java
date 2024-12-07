package org.codec.entity.third;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("infrastructure_stats")
public class InfrastructureStats {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("area_report_id")
    private Long areaReportId;

    @TableField("uptown_count")
    private Long uptownCount;

    @TableField("average_room_rate")
    private BigDecimal averageRoomRate;

    @TableField("average_rent")
    private BigDecimal averageRent;

    @TableField("max_room_rate")
    private BigDecimal maxRoomRate;

    @TableField("min_room_rate")
    private BigDecimal minRoomRate;

    @TableField("office_building_count")
    private Long officeBuildingCount;

    @TableField("average_room_rate_office_building")
    private BigDecimal averageRoomRateOfficeBuilding;

    @TableField("average_property_fee")
    private BigDecimal averagePropertyFee;
}

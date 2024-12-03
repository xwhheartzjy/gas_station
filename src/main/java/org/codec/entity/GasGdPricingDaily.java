package org.codec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("o_gas_gd_pricing_daily") // 对应数据库表名
public class GasGdPricingDaily {

    @TableId
    private Integer id; // 主键ID

    @TableField("pricing_date")
    private LocalDate pricingDate; // 价格日期

    @TableField("oil_station_id")
    private Long oilStationId; // 油站ID

    @TableField("oil_0")
    private Integer oil0; // 0号柴油

    @TableField("oil_92")
    private Integer oil92; // 92号汽油

    @TableField("oil_95")
    private Integer oil95; // 95号汽油

    @TableField("oil_98")
    private Integer oil98; // 98号汽油

    @TableField("source")
    private Integer source; // 数据来源

    @TableField("created_at")
    private LocalDateTime createdAt; // 创建时间

    @TableField("tenant_id")
    private String tenantId; // 租户ID
}

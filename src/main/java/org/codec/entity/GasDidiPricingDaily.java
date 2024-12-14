package org.codec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@TableName("o_gas_didi_pricing_daily")
public class GasDidiPricingDaily {

    @TableId
    private Integer id;

    @TableField("pricing_date")
    private LocalDate pricingDate;

    @TableField("oil_station_id")
    private Long oilStationId;

    @TableField("oil_0_store_price")
    private Integer oil0StorePrice;

    @TableField("oil_92_store_price")
    private Integer oil92StorePrice;

    @TableField("oil_95_store_price")
    private Integer oil95StorePrice;

    @TableField("oil_98_store_price")
    private Integer oil98StorePrice;

    @TableField("source")
    private Integer source;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("oil_0_vip_price")
    private Integer oil0VipPrice;

    @TableField("oil_92_vip_price")
    private Integer oil92VipPrice;

    @TableField("oil_95_vip_price")
    private Integer oil95VipPrice;

    @TableField("oil_98_vip_price")
    private Integer oil98VipPrice;

    @TableField("oil_0_city_price")
    private Integer oil0CityPrice;

    @TableField("oil_92_city_price")
    private Integer oil92CityPrice;

    @TableField("oil_95_city_price")
    private Integer oil95CityPrice;

    @TableField("oil_98_city_price")
    private Integer oil98CityPrice;

    @TableField("oil_0_card_price")
    private Integer oil0CardPrice;

    @TableField("oil_92_card_price")
    private Integer oil92CardPrice;

    @TableField("oil_95_card_price")
    private Integer oil95CardPrice;

    @TableField("oil_98_card_price")
    private Integer oil98CardPrice;
}

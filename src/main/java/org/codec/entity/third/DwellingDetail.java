package org.codec.entity.third;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dwelling_detail")
public class DwellingDetail {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("area_report_id")
    private Long areaReportId;

    @TableField("city_name")
    private String cityName;

    @TableField("county_name")
    private String countyName;

    @TableField("town_name")
    private String townName;

    @TableField("loupan_name")
    private String loupanName;

    @TableField("address")
    private String address;

    @TableField("detail_address")
    private String detailAddress;

    @TableField("location")
    private String location;

    @TableField("price")
    private Double price;

    @TableField("rent_price")
    private Double rentPrice;

    @TableField("total_price")
    private Double totalPrice;

    @TableField("property_type")
    private String propertyType;

    @TableField("tenure")
    private String tenure;

    @TableField("completed_time")
    private Integer completedTime;

    @TableField("property_tenure")
    private String propertyTenure;

    @TableField("household")
    private String household;

    @TableField("parking")
    private Integer parking;

    @TableField("property_costs")
    private Double propertyCosts;

    @TableField("property_inc")
    private String propertyInc;

    @TableField("house_type")
    private String houseType;
}

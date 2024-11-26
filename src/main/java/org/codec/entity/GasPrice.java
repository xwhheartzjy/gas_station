package org.codec.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("o_gas_gd_pricing_daily")
public class GasPrice {
    private Long id;
    private Date pricingDate;
    private Long oilStationId;
    private Integer oil0;
    private Integer oil92;
    private Integer oil95;
    private Integer oil98;
    private Integer source;


    private Integer gasType; // 油品类型（0、92、95、98）
    private Double gasPrice; // 油品价格

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getPricingDate() {
        return pricingDate;
    }

    public void setPricingDate(Date pricingDate) {
        this.pricingDate = pricingDate;
    }

    public Long getOilStationId() {
        return oilStationId;
    }

    public void setOilStationId(Long oilStationId) {
        this.oilStationId = oilStationId;
    }

    public Integer getOil0() {
        return oil0;
    }

    public void setOil0(Integer oil0) {
        this.oil0 = oil0;
    }

    public Integer getOil92() {
        return oil92;
    }

    public void setOil92(Integer oil92) {
        this.oil92 = oil92;
    }

    public Integer getOil95() {
        return oil95;
    }

    public void setOil95(Integer oil95) {
        this.oil95 = oil95;
    }

    public Integer getOil98() {
        return oil98;
    }

    public void setOil98(Integer oil98) {
        this.oil98 = oil98;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getGasType() {
        return gasType;
    }

    public void setGasType(Integer gasType) {
        this.gasType = gasType;
    }

    public Double getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(Double gasPrice) {
        this.gasPrice = gasPrice;
    }
}
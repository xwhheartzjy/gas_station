package org.codec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;

@TableName("gas_station")
public class GasStation {
    @TableId(type = IdType.AUTO)
    @TableField("station_id")
    private Long stationId; // 站点ID
    @TableField("name")
    private String name; // 油站名称
    @TableField("address")
    private String address; // 地址
    @TableField("longitude")
    private BigDecimal longitude;
    @TableField("latitude")// 经度
    private BigDecimal latitude; // 纬度
    @TableField("area_id")
    private Long areaId; // 区域ID
    @TableField("business_start_time")
    private Date businessStartTime; // 营业开始时间
    @TableField("business_end_time")
    private Date businessEndTime; // 营业结束时间
    @TableField("station_type")
    private String stationType; // 油站类型
    @TableField("car_wash")
    private Integer carWash; // 是否提供洗车服务（0=否,1=是）
    @TableField("create_by")
    private Long createBy; // 创建人
    @TableField("create_time")
    private Date createTime; // 创建时间
    @TableField("update_by")
    private Long updateBy; // 更新人
    @TableField("update_time")
    private Date updateTime; // 更新时间
    @TableField("del_flag")
    private Integer delFlag; // 删除标志（0代表存在 2代表删除）
    @TableField("user_id")
    private Long userId;

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Date getBusinessStartTime() {
        return businessStartTime;
    }

    public void setBusinessStartTime(Date businessStartTime) {
        this.businessStartTime = businessStartTime;
    }

    public Date getBusinessEndTime() {
        return businessEndTime;
    }

    public void setBusinessEndTime(Date businessEndTime) {
        this.businessEndTime = businessEndTime;
    }

    public String getStationType() {
        return stationType;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public Integer getCarWash() {
        return carWash;
    }

    public void setCarWash(Integer carWash) {
        this.carWash = carWash;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
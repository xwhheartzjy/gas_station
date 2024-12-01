package org.codec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@TableName("gas_station")
@Data
public class GasStationConsumer {
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
    private LocalTime businessStartTime; // 营业开始时间
    @TableField("business_end_time")
    private LocalTime businessEndTime; // 营业结束时间
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


}
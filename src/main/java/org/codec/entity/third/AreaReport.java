package org.codec.entity.third;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("area_report")
public class AreaReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("version")
    private Integer version;

    @TableField("message")
    private String message;

    @TableField("trace_id")
    private String traceId;

    @TableField("gas_station_name")
    private String gasStationName;
}

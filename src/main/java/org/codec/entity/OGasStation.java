package org.codec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("o_gas_station") // 对应数据库表名
public class OGasStation {

    @TableId
    private Integer id; // 主键

    @TableField("name")
    private String name; // 油站名称

    @TableField("address")
    private String address; // 地址

    @TableField("lng")
    private String lng; // 经度

    @TableField("lat")
    private String lat; // 纬度

    @TableField("ad_code")
    private String adCode; // 行政代码

    @TableField("city_code")
    private String cityCode; // 城市代码

    @TableField("opening_status")
    private Integer openingStatus; // 营业状态

    @TableField("source")
    private Integer source; // 来源

    @TableField("created_at")
    private LocalDateTime createdAt; // 创建时间

    @TableField("tenant_id")
    private String tenantId; // 租户 ID

    private String distance;
}

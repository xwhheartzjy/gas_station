package org.codec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gas_station_mapping") // 对应数据库表名
public class GasStationMapping {

    @TableId
    @TableField("mapping_id")
    private Long mappingId; // 映射ID

    @TableField("original_station_id")
    private Long originalStationId; // 原始油站ID

    @TableField("target_station_id")
    private Long targetStationId; // 映射/目标油站ID

    @TableField("create_dept")
    private Long createDept; // 创建部门

    @TableField("create_by")
    private Long createBy; // 创建人

    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_by")
    private Long updateBy; // 更新人

    @TableField("update_time")
    private LocalDateTime updateTime; // 更新时间

    @TableField("tenant_id")
    private String tenantId; // 租户号

    @TableField("del_flag")
    private String delFlag; // 删除标志 (0代表存在，2代表删除)
}

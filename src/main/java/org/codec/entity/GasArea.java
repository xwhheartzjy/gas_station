package org.codec.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("gas_area")
@Data
public class GasArea {

    @TableId(value = "area_id")  // 主键字段，映射数据库的 area_id
    private Long areaId;          // 区域id

    @TableField(value = "area_code") // 映射数据库的 area_code
    private String areaCode;      // 区域编码

    @TableField(value = "area_name") // 映射数据库的 area_name
    private String areaName;      // 区域名称

    @TableField(value = "parent_id") // 映射数据库的 parent_id
    private Long parentId;        // 父节点

    @TableField(value = "level") // 映射数据库的 level
    private String level;         // 区域层级

    @TableField(value = "create_dept") // 映射数据库的 create_dept
    private Long createDept;      // 创建部门

    @TableField(value = "create_by") // 映射数据库的 create_by
    private Long createBy;        // 创建人

    @TableField(value = "create_time") // 映射数据库的 create_time
    private LocalDateTime createTime; // 创建时间

    @TableField(value = "update_by") // 映射数据库的 update_by
    private Long updateBy;        // 更新人

    @TableField(value = "update_time") // 映射数据库的 update_time
    private LocalDateTime updateTime; // 更新时间

    @TableField(value = "del_flag") // 映射数据库的 del_flag
    private String delFlag;       // 删除标志

    @TableField(value = "tenant_id") // 映射数据库的 tenant_id
    private String tenantId;      // 租户号
}

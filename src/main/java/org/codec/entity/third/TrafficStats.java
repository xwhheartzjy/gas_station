package org.codec.entity.third;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("traffic_stats")
public class TrafficStats {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("area_report_id")
    private Long areaReportId;

    @TableField("bus_station")
    private Integer busStation;

    @TableField("subway")
    private Integer subway;

    @TableField("coach_station")
    private Integer coachStation;

    @TableField("airport")
    private Integer airport;
}

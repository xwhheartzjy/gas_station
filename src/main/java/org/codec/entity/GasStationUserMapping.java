package org.codec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class GasStationUserMapping {
    @TableId(type = IdType.ASSIGN_UUID)
    @TableField("id")
    private String Id; // 站点ID

    @TableField("station_id")
    private Long stationId;

    @TableField("station_user_id")
    private String stationUserId;

}

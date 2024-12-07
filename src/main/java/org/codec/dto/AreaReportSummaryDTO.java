package org.codec.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AreaReportSummaryDTO {
    private Long uptownCount;             // 住宅区个数
    private Integer businessCircleNumber; // 商圈数
    private Long officeBuildingCount;     // 写字楼数
    private BigDecimal averageRoomRate;   // 房价均价
    private BigDecimal averageRent;       // 租金均价
    private Long totalHouseHold;          // 总户数
}

package org.codec.mapper.thrid;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.codec.dto.AreaReportSummaryDTO;
import org.codec.entity.third.AreaReport;

public interface AreaReportMapper extends BaseMapper<AreaReport> {

    /**
     * 查询特定地区的统计数据
     *
     * @param areaReportId 地区的主键 ID
     * @return 查询结果封装对象
     */
    @Select("SELECT " +
            "  infra.uptown_count AS uptownCount, " +
            "  infra.average_room_rate AS averageRoomRate, " +
            "  infra.average_rent AS averageRent, " +
            "  infra.office_building_count AS officeBuildingCount, " +
            "  bus.business_circle_number AS businessCircleNumber, " +
            "  COALESCE(dw.totalHouseHold, 0) AS totalHouseHold " +
            "FROM infrastructure_stats infra " +
            "LEFT JOIN business_stats bus ON infra.area_report_id = bus.area_report_id " +
            "LEFT JOIN ( " +
            "  SELECT area_report_id, SUM(COALESCE(houseHold, 0)) AS totalHouseHold " +
            "  FROM dwelling_detail " +
            "  GROUP BY area_report_id " +
            ") dw ON infra.area_report_id = dw.area_report_id " +
            "WHERE infra.area_report_id = #{areaReportId}")
    AreaReportSummaryDTO queryAreaSummary(@Param("areaReportId") Long areaReportId);




}

package org.codec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.codec.entity.GasGdPricingDaily;
import org.codec.entity.GasStationConsumer;

import java.util.List;

public interface GasGdPricingDailyMapper extends BaseMapper<GasGdPricingDaily> {
    @Select("<script>" +
            "SELECT " +
            "    oil_station_id, " +
            "    source," +
            "    MIN(CASE " +
            "        WHEN #{gasType} = 0 THEN oil_0 " +
            "        WHEN #{gasType} = 92 THEN oil_92 " +
            "        WHEN #{gasType} = 95 THEN oil_95 " +
            "        WHEN #{gasType} = 98 THEN oil_98 " +
            "    END) AS min_oil_price ," +
            "    ANY_VALUE(oil_0) AS oil_0, " +
            "    ANY_VALUE(oil_92) AS oil_92, " +
            "    ANY_VALUE(oil_95) AS oil_95, " +
            "    ANY_VALUE(oil_98) AS oil_98 " +
            "FROM o_gas_gd_pricing_daily " +
            "WHERE oil_station_id IN " +
            "    <foreach item='item' index='index' collection='oilStationIds' open='(' separator=',' close=')'>" +
            "        #{item} " +
            "    </foreach> " +
            "    AND pricing_date = #{pricingDate} " +
            "GROUP BY oil_station_id, source " +
            "ORDER BY min_oil_price ${sort} "+
            "</script>")
    Page<GasGdPricingDaily> selectPricingByStationAndType(Page page,
                                                          @Param("gasType") Integer gasType,
                                                          @Param("oilStationIds") List<Integer> oilStationIds,
                                                          @Param("pricingDate") String pricingDate,
                                                          @Param("sort") String sort);
}

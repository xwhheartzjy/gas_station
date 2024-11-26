package org.codec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.codec.entity.GasPrice;
import org.codec.entity.GasStation;

import java.util.List;

public interface GasPriceMapper extends BaseMapper<GasPrice> {

    // 根据油站 ID 获取油品价格
    @Select("SELECT * FROM o_gas_gd_pricing_daily WHERE oil_station_id = #{stationId} AND pricing_date = CURDATE()")
    List<GasPrice> getPricesByStationId(@Param("stationId") Long stationId);
}
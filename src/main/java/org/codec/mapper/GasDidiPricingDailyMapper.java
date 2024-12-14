package org.codec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.codec.dto.GasDidiPricingDailyDTO;
import org.codec.entity.GasArea;
import org.codec.entity.GasDidiPricingDaily;

import java.time.LocalDate;

public interface GasDidiPricingDailyMapper extends BaseMapper<GasDidiPricingDaily> {
    @Select(
            "SELECT oil_station_id AS oilStationId, pricing_date AS pricingDate, "+
            "LEAST(oil_0_store_price, oil_0_vip_price, oil_0_city_price, oil_0_card_price) AS minOil0Price, "+
            "LEAST(oil_92_store_price, oil_92_vip_price, oil_92_city_price, oil_92_card_price) AS minOil92Price, "+
            "LEAST(oil_95_store_price, oil_95_vip_price, oil_95_city_price, oil_95_card_price) AS minOil95Price, "+
            "LEAST(oil_98_store_price, oil_98_vip_price, oil_98_city_price, oil_98_card_price) AS minOil98Price "+
            "FROM "+
            "o_gas_didi_pricing_daily "+
            "WHERE "+
            "pricing_date = #{pricingDate} "+
            "AND oil_station_id = #{oilStationId} ")
    GasDidiPricingDailyDTO getDiDiMinPrices(@Param("oilStationId") String oilStationId,@Param("pricingDate") LocalDate pricingDate);

}

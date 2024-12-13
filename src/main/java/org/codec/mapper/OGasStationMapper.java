package org.codec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.codec.entity.GasStation;
import org.codec.entity.OGasStation;

import java.util.List;

public interface OGasStationMapper extends BaseMapper<OGasStation> {
    // 可自定义查询方法
    @Select(
            "SELECT id, name, address, lng, lat, ad_code, city_code, opening_status, source, created_at, tenant_id, " +
            "(6371 * acos(cos(radians(#{targetLat})) * cos(radians(lat)) * cos(radians(lng) - radians(#{targetLng})) + " +
            "sin(radians(#{targetLat})) * sin(radians(lat)))) AS distance " +
            "FROM o_gas_station " +
            "HAVING distance <= #{maxDistance} "+
            "ORDER BY distance ${sortOrder} ")
    Page<OGasStation> findStationsWithinDistance(Page page,
                                                  @Param("targetLat") Double targetLat,
                                                  @Param("targetLng") Double targetLng,
                                                  @Param("maxDistance") Double maxDistance,
                                                  @Param("sortOrder") String sortOrder);

    @Select(
            "SELECT id, name, address, lng, lat, ad_code, city_code, opening_status, source, created_at, tenant_id, " +
                    "(6371 * acos(cos(radians(#{targetLat})) * cos(radians(lat)) * cos(radians(lng) - radians(#{targetLng})) + " +
                    "sin(radians(#{targetLat})) * sin(radians(lat)))) AS distance " +
                    "FROM o_gas_station " +
                    "HAVING distance <= #{maxDistance} ")
    List<OGasStation> findStationsWithDistance(
                                                 @Param("targetLat") Double targetLat,
                                                 @Param("targetLng") Double targetLng,
                                                 @Param("maxDistance") Double maxDistance);

    @Select(
            "SELECT id, name, address, lng, lat, ad_code, city_code, opening_status, source, created_at, tenant_id, " +
                    "(6371 * acos(cos(radians(#{targetLat})) * cos(radians(lat)) * cos(radians(lng) - radians(#{targetLng})) + " +
                    "sin(radians(#{targetLat})) * sin(radians(lat)))) AS distance " +
                    "FROM o_gas_station " +
                    "HAVING distance <= #{maxDistance} ")
    Page<OGasStation> findStationsWithDistancePage(Page page,
            @Param("targetLat") Double targetLat,
            @Param("targetLng") Double targetLng,
            @Param("maxDistance") Double maxDistance);

    @Select(
            "SELECT id, name, address, lng, lat, ad_code, city_code, opening_status, source, created_at, tenant_id, " +
                    "ST_Distance_Sphere(POINT(lng, lat), POINT(#{targetLng}, #{targetLat})) AS distance " +
                    "FROM o_gas_station " +
                    "HAVING distance <= #{maxDistance} ")
    Page<OGasStation> findStationsWithDistance2Page(Page page,
                                                   @Param("targetLat") Double targetLat,
                                                   @Param("targetLng") Double targetLng,
                                                   @Param("maxDistance") Double maxDistance);
}
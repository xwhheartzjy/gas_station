package org.codec.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.codec.entity.GasPrice;
import org.codec.entity.GasStation;
import org.codec.mapper.GasPriceMapper;
import org.codec.mapper.GasStationMapper;
import org.codec.util.HaversineUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GasStationService extends ServiceImpl<GasStationMapper, GasStation> {

    @Autowired
    private GasStationMapper gasStationMapper;

    public GasStation getStations() {
        GasStation targetStation = gasStationMapper.selectById(1);
        return targetStation;
    }

    public List<GasStation> listStationByUser(String userId) {
        QueryWrapper<GasStation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId).eq("del_flag",0);
        return gasStationMapper.selectList(queryWrapper);
    }
//
//    @Autowired
//    private GasPriceMapper gasPriceMapper;
//
//    // 获取加油站信息及油价
//    public List<GasStation> getGasStationsList(Integer PageNo,Integer size,String stationId, Integer gasolineType, Integer distanceType,String orderBy) {
//        // 1. 获取目标加油站的经纬度
//        GasStation targetStation = gasStationMapper.selectById(stationId);
//        if (targetStation == null) {
//            return new ArrayList<>();
//        }
//
//        double targetLat = targetStation.getLatitude().doubleValue();
//        double targetLon = targetStation.getLongitude().doubleValue();
//
//        Page<GasStation> page = new Page<>(PageNo, size);
//        // 2. 获取与目标站点在指定距离范围内的其他加油站
//        QueryWrapper<GasStation> queryWrapper = new QueryWrapper<>();
//        queryWrapper.ne("station_id", stationId); // 排除自己
//        List<GasStation> nearbyStations = gasStationMapper.selectList(queryWrapper);
//
//        // 3. 筛选出距离符合要求的加油站
//        List<GasStation> result = new ArrayList<>();
//        for (GasStation station : nearbyStations) {
//            double stationLat = station.getLatitude().doubleValue();
//            double stationLon = station.getLongitude().doubleValue();
//            double dist = HaversineUtil.getDistance(targetLat, targetLon, stationLat, stationLon);
//
//            if (distanceType == 0 && dist <= 3) {
//
//            }
//
//            // 根据距离类型筛选：3公里、5公里、10公里
//            if ((distanceType == 0 && dist <= 3) ||
//                    (distanceType == 1 && dist <= 5) ||
//                    (distanceType == 2 && dist <= 10)) {
//
//                // 获取该加油站的油品价格
//                List<GasPrice> gasPrices = gasPriceMapper.getPricesByStationId(station.getStationId(), gasolineType);
//                station.setGasStationNearbyPrice(gasPrices);
//                result.add(station);
//            }
//        }
//
//        return result;
//    }
}
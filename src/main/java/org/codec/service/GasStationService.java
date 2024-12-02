package org.codec.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.codec.common.RequestContext;
import org.codec.dto.GasAreaDTO;
import org.codec.dto.GasStationDTO;
import org.codec.entity.GasArea;
import org.codec.entity.GasPrice;
import org.codec.entity.GasStation;
import org.codec.entity.GasStationConsumer;
import org.codec.mapper.*;
import org.codec.mapper.map_struct.GasAreaMapStructMapper;
import org.codec.mapper.map_struct.GasStationMapStructMapper;
import org.codec.request.AddGasStationRequest;
import org.codec.util.HaversineUtil;
import org.codec.util.JwtTokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class GasStationService extends ServiceImpl<GasStationMapper, GasStation> {

    @Autowired
    private GasStationMapper gasStationMapper;
    @Autowired
    private GasStationConsumerMapper gasStationConsumerMapper;
    @Autowired
    private GasStationUserMappingMapper gasStationUserMappingMapper;

    @Autowired
    private GasAreaMapper gasAreaMapper;

    public List<GasStationDTO> listStationByUser(String userId) {
        QueryWrapper<GasStationConsumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("del_flag", 0);
        List<GasStationConsumer> gasStationConsumers = gasStationConsumerMapper.selectList(queryWrapper);
        List<GasStationDTO> result = new ArrayList<>();
        for (GasStationConsumer gasStationConsumer : gasStationConsumers) {
            result.add(GasStationMapStructMapper.INSTANCE.toDTO(gasStationConsumer));
        }
        return result;
    }

    public void addGasStation(AddGasStationRequest request) {
        GasStationConsumer gasStation = new GasStationConsumer();
        gasStation.setBusinessEndTime(request.getBusinessEndTime());
        gasStation.setName(request.getGasStationName());
        gasStation.setCarWash(request.getCarWash());
        gasStation.setStationType(request.getStationType());
        gasStation.setBusinessStartTime(request.getBusinessStartTime());
        gasStation.setLatitude(request.getLatitude());
        gasStation.setLongitude(request.getLongitude());
        gasStation.setCreateBy(RequestContext.getCurrentUser().getUserId());
        gasStation.setCreateTime(DateUtil.date());
        gasStation.setUserId(request.getUserId());
        gasStation.setAddress(request.getAddress());
        gasStation.setAreaId(request.getAreaId());
        gasStationConsumerMapper.insert(gasStation);
        QueryWrapper<GasStation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("longitude",request.getLongitude());
        queryWrapper.eq("latitude",request.getLatitude());
        queryWrapper.eq("del_flag",0);

        List<GasStation> gasStations = gasStationMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(gasStations)) {

        }
    }

    public void deleteGasStation(Long station_id) {
        gasStationConsumerMapper.deleteById(station_id);
    }

    public void updateGasStation(AddGasStationRequest request) {
        GasStationConsumer gasStation = new GasStationConsumer();
        gasStation.setBusinessEndTime(request.getBusinessEndTime());
        gasStation.setName(request.getGasStationName());
        gasStation.setCarWash(request.getCarWash());
        gasStation.setStationType(request.getStationType());
        gasStation.setBusinessStartTime(request.getBusinessStartTime());
        gasStation.setLatitude(request.getLatitude());
        gasStation.setLongitude(request.getLongitude());
        gasStation.setAddress(request.getAddress());
        gasStation.setAreaId(request.getAreaId());
        gasStation.setUpdateBy(RequestContext.getCurrentUser().getUserId());
        gasStation.setUpdateTime(DateUtil.date());
        gasStation.setStationId(request.getStationId());
        gasStationConsumerMapper.updateById(gasStation);
    }

    public GasStationDTO getStationDetail(Long stationId) {
        GasStationConsumer gasStationConsumer = gasStationConsumerMapper.selectById(stationId);
        LinkedList<GasAreaDTO> gasAreaDTOS = new LinkedList<>();
        buildHierarchy(gasStationConsumer.getAreaId(),gasAreaDTOS);
        GasStationDTO gasStationDTO = new GasStationDTO();
        gasStationDTO = GasStationMapStructMapper.INSTANCE.toDTO(gasStationConsumer);
        gasStationDTO.setAreas(gasAreaDTOS);
        return gasStationDTO;
    }

    private void buildHierarchy(Long areaId, List<GasAreaDTO> result) {
        // 查询当前区域信息
        GasArea currentArea = gasAreaMapper.selectById(areaId);
        if (currentArea != null) {
            // 添加当前区域到结果列表
            GasAreaDTO gasAreaDTO = new GasAreaDTO();
            gasAreaDTO.setArea_name(currentArea.getAreaName());
            gasAreaDTO.setArea_id(currentArea.getAreaId());

            result.add(gasAreaDTO);

            // 如果还有父节点，递归调用
            if (currentArea.getParentId() != null) {
                buildHierarchy(currentArea.getParentId(), result);
            }
        }
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
package org.codec.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.codec.common.RequestContext;
import org.codec.dto.GasAreaDTO;
import org.codec.dto.GasStationDTO;
import org.codec.dto.OGasStationDTO;
import org.codec.entity.*;
import org.codec.mapper.*;
import org.codec.mapper.map_struct.GasAreaMapStructMapper;
import org.codec.mapper.map_struct.GasStationMapStructMapper;
import org.codec.request.AddGasStationRequest;
import org.codec.util.HaversineUtil;
import org.codec.util.JwtTokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GasStationService extends ServiceImpl<GasStationMapper, GasStation> {
    @Autowired
    private GasGdPricingDailyMapper gasGdPricingDailyMapper;
    @Autowired
    private GasStationMapper gasStationMapper;
    @Autowired
    private GasStationConsumerMapper gasStationConsumerMapper;

    @Autowired
    private GasAreaMapper gasAreaMapper;
    @Autowired
    private OGasStationMapper oGasStationMapper;
    @Autowired
    private GasStationMappingMapper gasStationMappingMapper;

    public List<GasStationDTO> listStationByUser(String userId) {
        QueryWrapper<GasStationConsumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("del_flag", 0);
        List<GasStationConsumer> gasStationConsumers = gasStationConsumerMapper.selectList(queryWrapper);
        List<GasStationDTO> result = new ArrayList<>();
        for (GasStationConsumer gasStationConsumer : gasStationConsumers) {
            GasStationDTO gasStationDTO = GasStationMapStructMapper.INSTANCE.toDTO(gasStationConsumer);
            LinkedList<GasAreaDTO> gasAreaDTOS = new LinkedList<>();
            buildHierarchy(gasStationConsumer.getAreaId(),gasAreaDTOS);
            gasStationDTO.setAreas(gasAreaDTOS);
            result.add(gasStationDTO);

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
        QueryWrapper<GasStation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("longitude",request.getLongitude());
        queryWrapper.eq("latitude",request.getLatitude());
        queryWrapper.eq("del_flag",0);
        List<GasStation> gasStations = gasStationMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(gasStations)) {
            gasStation.setOriginStation(gasStations.get(0).getStationId());
        }
        gasStationConsumerMapper.insert(gasStation);
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
        gasStation.setId(request.getStationId());
        gasStationConsumerMapper.updateById(gasStation);
    }

    public GasStationDTO getStationDetail(String stationId) {
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



    // 获取加油站信息及油价
    public List<GasStation> getGasStationsList(Integer PageNo,Integer size,
                                               String stationId, Integer gasolineType,
                                               Integer distanceType,String orderBy,String sort) {
        double distance;
        if (distanceType == 0) {
            distance = 3;
        } else if (distanceType == 1){
            distance =5;
        }else if (distanceType == 2) {
            distance = 10;
        } else {
            distance = 0;
        }
        // 1. 获取目标加油站的经纬度
        GasStationConsumer targetStation = gasStationConsumerMapper.selectById(stationId);
        if (targetStation == null) {
            return new ArrayList<>();
        }
        if (targetStation.getOriginStation() == null) {
            return new ArrayList<>();
        }

        double targetLat = targetStation.getLatitude().doubleValue();
        double targetLon = targetStation.getLongitude().doubleValue();
        QueryWrapper<GasStationMapping> gasStationMappingQueryWrapper = new QueryWrapper<>();
        gasStationMappingQueryWrapper.eq("original_station_id",targetStation.getOriginStation());
        gasStationMappingQueryWrapper.eq("del_flag",0);
        GasStationMapping originGasStationMapping = gasStationMappingMapper.selectOne(gasStationMappingQueryWrapper);

        // 2. 获取与目标站点在指定距离范围内的其他加油站
        QueryWrapper<OGasStation> queryWrapper = new QueryWrapper<>();
        // 排除自己
        queryWrapper.ne("id", originGasStationMapping.getTargetStationId());
        List<OGasStation> nearbyStations = oGasStationMapper.selectList(queryWrapper);
        // 2. 根据半径过滤油站
        nearbyStations = nearbyStations.stream()
                .filter(station -> HaversineUtil.getDistance(
                        targetLat, targetLon,
                        Double.parseDouble(station.getLat()), Double.parseDouble(station.getLng())
                ) <= distance)
                .collect(Collectors.toList());
        //获取价格数据
        List<GasGdPricingDaily> gasGdPricingDailies = filterPricingList(nearbyStations);
        // 按oil_station_id分组
        Map<Long, List<GasGdPricingDaily>> groupedByStations = gasGdPricingDailies.stream()
                .collect(Collectors.groupingBy(GasGdPricingDaily::getOilStationId));
        List<GasGdPricingDaily> handledPricingData = new ArrayList<>();
        if (gasolineType == 0) {

        }

        // 3. 根据油品类型找出最低价格的油站，并放到返回数组的最上面
        nearbyStations = prioritizeByGasolineType(nearbyStations, gasolineType);


        // 3. 筛选出距离符合要求的加油站
        List<GasStation> result = new ArrayList<>();
        for (OGasStation station : nearbyStations) {
            double stationLat = Double.parseDouble(station.getLat());
            double stationLon = Double.parseDouble(station.getLng());
            double dist = HaversineUtil.getDistance(targetLat, targetLon, stationLat, stationLon);

            // 根据距离类型筛选：3公里、5公里、10公里
            if ((distanceType == 0 && dist <= 3) ||
                    (distanceType == 1 && dist <= 5) ||
                    (distanceType == 2 && dist <= 10)) {

                // 获取该加油站的油品价格
                QueryWrapper<GasGdPricingDaily> gasGdPricingDailyQueryWrapper = new QueryWrapper<>();
                gasGdPricingDailyQueryWrapper.eq("oil_station_id",station.getId());
                gasGdPricingDailyQueryWrapper.eq("pricing_date",DateUtil.date().toDateStr());
                List<GasGdPricingDaily> pricingList = gasGdPricingDailyMapper.selectList(gasGdPricingDailyQueryWrapper);
                if (pricingList.isEmpty()) {
                    return Collections.emptyList(); // 没有数据
                }
            }
        }

        return result;
    }
    private List<GasGdPricingDaily> filterPricingList(List<OGasStation> stations) {
        QueryWrapper<GasGdPricingDaily> queryWrapper = new QueryWrapper<>();
        List<Integer> stationIds = stations.stream()
                .map(OGasStation::getId)
                .collect(Collectors.toList());
        queryWrapper.in("oil_station_id",stationIds);
        queryWrapper.eq("pricing_date",DateUtil.date().toString("yyyy-MM-dd"));
        return gasGdPricingDailyMapper.selectList(queryWrapper);

    }

    public List<OGasStation> filterStationsByDistance(List<OGasStation> gasStations, Integer distanceType, double targetLat, double targetLon) {
        List<OGasStation> result = new ArrayList<>();
        for (OGasStation station : gasStations) {
            double stationLat = Double.parseDouble(station.getLat());
            double stationLon = Double.parseDouble(station.getLng());
            double dist = HaversineUtil.getDistance(targetLat, targetLon, stationLat, stationLon);
            if ((distanceType == 0 && dist <= 3) ||
                    (distanceType == 1 && dist <= 5) ||
                    (distanceType == 2 && dist <= 10)) {
                result.add(station);
            }
        }
        return result;
    }
}
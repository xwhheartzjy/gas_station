package org.codec.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.codec.common.RequestContext;
import org.codec.dto.*;
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
            buildHierarchy(gasStationConsumer.getAreaId(), gasAreaDTOS);
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
        queryWrapper.eq("longitude", request.getLongitude());
        queryWrapper.eq("latitude", request.getLatitude());
        queryWrapper.eq("del_flag", 0);
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
        buildHierarchy(gasStationConsumer.getAreaId(), gasAreaDTOS);
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
    public Page<GasPriceDTO> getGasStationsList(Integer pageNo, Integer size,
                                                String stationId, Integer gasolineType,
                                                Integer distanceType, String orderBy, String sort) {
        double distance;
        if (distanceType == 0) {
            distance = 3;
        } else if (distanceType == 1) {
            distance = 5;
        } else if (distanceType == 2) {
            distance = 10;
        } else {
            distance = 0;
        }
        // 1. 获取目标加油站的经纬度
        GasStationConsumer targetStation = gasStationConsumerMapper.selectById(stationId);
        if (targetStation == null) {
            return new Page<>(pageNo,size);
        }
        if (targetStation.getOriginStation() == null) {
            return new Page<>(pageNo,size);
        }

        double targetLat = targetStation.getLatitude().doubleValue();
        double targetLon = targetStation.getLongitude().doubleValue();
        QueryWrapper<GasStationMapping> gasStationMappingQueryWrapper = new QueryWrapper<>();
        gasStationMappingQueryWrapper.eq("original_station_id", targetStation.getOriginStation());
        gasStationMappingQueryWrapper.eq("del_flag", 0);
        GasStationMapping originGasStationMapping = gasStationMappingMapper.selectOne(gasStationMappingQueryWrapper);

        // 2. 获取与目标站点在指定距离范围内的其他加油站
        QueryWrapper<OGasStation> queryWrapper = new QueryWrapper<>();
        // 排除自己
        queryWrapper.ne("id", originGasStationMapping.getTargetStationId());
        List<OGasStation> nearbyStations = oGasStationMapper.selectList(queryWrapper);
        List<OGasStation> nearbyStationsFiltered = new ArrayList<>();
        List<GasPriceDTO> result = new ArrayList<>();
        for (OGasStation oGasStation : nearbyStations) {
            GasPriceDTO gasPriceDTO = new GasPriceDTO();
            double dis = HaversineUtil.getDistance(
                    targetLat, targetLon,
                    Double.parseDouble(oGasStation.getLat()), Double.parseDouble(oGasStation.getLng()));
            if (dis <= distance*1000) {
                gasPriceDTO.setGasLocation(oGasStation.getAddress());
                gasPriceDTO.setGasStationName(oGasStation.getName());
                gasPriceDTO.setGasStationId(oGasStation.getId());
                gasPriceDTO.setGasStationType("normal");
                gasPriceDTO.setDistance(dis);
                result.add(gasPriceDTO);
                nearbyStationsFiltered.add(oGasStation);
            }

        }
        if (CollectionUtil.isEmpty(nearbyStationsFiltered)) {
            return new Page<>(pageNo,size);
        }
        //获取价格数据
        List<GasGdPricingDaily> gasGdPricingDailies = filterPricingList(nearbyStationsFiltered);
        // 按oil_station_id分组
        Map<Long, List<GasGdPricingDaily>> groupedByStations = gasGdPricingDailies.stream()
                .collect(Collectors.groupingBy(GasGdPricingDaily::getOilStationId));
        List<GasGdPricingDaily> handledPricingData = new ArrayList<>();

        for (Map.Entry<Long, List<GasGdPricingDaily>> entry : groupedByStations.entrySet()) {
            List<GasGdPricingDaily> pricingList = entry.getValue();
            if (gasolineType == 0) {
                Optional<GasGdPricingDaily> minOil0Record = pricingList.stream()
                        .min(Comparator.comparingDouble(GasGdPricingDaily::getOil0));
                handledPricingData.add(minOil0Record.get());
            } else if (gasolineType == 1) {
                Optional<GasGdPricingDaily> minOil92Record = pricingList.stream()
                        .min(Comparator.comparingDouble(GasGdPricingDaily::getOil92));
                handledPricingData.add(minOil92Record.get());
            } else if (gasolineType == 2) {
                Optional<GasGdPricingDaily> minOil95Record = pricingList.stream()
                        .min(Comparator.comparingDouble(GasGdPricingDaily::getOil95));
                handledPricingData.add(minOil95Record.get());
            } else if (gasolineType == 3) {
                Optional<GasGdPricingDaily> minOil98Record = pricingList.stream()
                        .min(Comparator.comparingDouble(GasGdPricingDaily::getOil98));
                handledPricingData.add(minOil98Record.get());
            }
        }

        if ("price".equals(orderBy)) {
            List<GasGdPricingDaily> sortedList = sortPricing(gasolineType, handledPricingData, sort);
            Map<Integer, List<GasGdPricingDaily>> collect = sortedList.stream().collect(Collectors.groupingBy(GasGdPricingDaily::getId));
            List<GasPriceDTO> l = new ArrayList<>();
            Map<Integer, List<GasPriceDTO>> collect1 = result.stream().collect(Collectors.groupingBy(GasPriceDTO::getGasStationId));
            for (GasGdPricingDaily gasGdPricingDaily : sortedList) {
                List<GasPriceDTO> gasPriceDTOS = collect1.get(gasGdPricingDaily.getOilStationId().intValue());
                if (gasPriceDTOS == null) {
                    continue;
                }
                List<GasInfoDTO> infoDTOS = new ArrayList<>();
                GasInfoDTO gasInfo0 = new GasInfoDTO();
                gasInfo0.setGasPrice(gasGdPricingDaily.getOil0());
                gasInfo0.setGasType(0);
                infoDTOS.add(gasInfo0);
                GasInfoDTO gasInfo92 = new GasInfoDTO();
                gasInfo92.setGasPrice(gasGdPricingDaily.getOil92());
                gasInfo92.setGasType(92);
                infoDTOS.add(gasInfo92);
                GasInfoDTO gasInfo95 = new GasInfoDTO();
                gasInfo95.setGasPrice(gasGdPricingDaily.getOil95());
                gasInfo95.setGasType(95);
                infoDTOS.add(gasInfo95);
                GasInfoDTO gasInfo98 = new GasInfoDTO();
                gasInfo98.setGasPrice(gasGdPricingDaily.getOil98());
                gasInfo98.setGasType(98);
                infoDTOS.add(gasInfo98);
                gasPriceDTOS.get(0).setGasStationNearbyPrice(infoDTOS);
                l.add(gasPriceDTOS.get(0));
            }
            for (GasPriceDTO gasPriceDTO : result) {
                for (GasGdPricingDaily gasGdPricingDaily :sortedList) {
                    if (gasPriceDTO.getGasStationId().equals(gasGdPricingDaily.getId())) {
                        continue;
                    }
                }
                l.add(gasPriceDTO);

            }
            // 计算分页的起始位置
            int fromIndex = (pageNo - 1) * size;
            int toIndex = Math.min(fromIndex + size, l.size());

            // 如果起始位置超过了数组大小，返回空列表
            if (fromIndex >= l.size()) {
                return new Page<>(pageNo, size, l.size());
            }

            // 分页后的结果
            List<GasPriceDTO> paginatedList = l.subList(fromIndex, toIndex);
            Page<GasPriceDTO> objectPage = new Page<>(pageNo, size, l.size());
            objectPage.setRecords(paginatedList);

            // 创建 Page 对象并返回
            return objectPage;
//            for (GasPriceDTO gasPriceDTO : result) {
//                List<GasGdPricingDaily> dailies = collect.get(gasPriceDTO.getGasStationId());
//                if (dailies == null) {
//                    continue;
//                }
//                List<GasInfoDTO> infoDTOS = new ArrayList<>();
//                GasInfoDTO gasInfo0 = new GasInfoDTO();
//                gasInfo0.setGasPrice(dailies.get(0).getOil0());
//                gasInfo0.setGasType(0);
//                infoDTOS.add(gasInfo0);
//                GasInfoDTO gasInfo92 = new GasInfoDTO();
//                gasInfo92.setGasPrice(dailies.get(0).getOil92());
//                gasInfo92.setGasType(92);
//                infoDTOS.add(gasInfo92);
//                GasInfoDTO gasInfo95 = new GasInfoDTO();
//                gasInfo95.setGasPrice(dailies.get(0).getOil95());
//                gasInfo95.setGasType(95);
//                infoDTOS.add(gasInfo95);
//                GasInfoDTO gasInfo98 = new GasInfoDTO();
//                gasInfo98.setGasPrice(dailies.get(0).getOil98());
//                gasInfo98.setGasType(98);
//                infoDTOS.add(gasInfo98);
//                gasPriceDTO.setGasStationNearbyPrice(infoDTOS);
//            }
        }else if ("distance".equals(orderBy)) {
            List<GasPriceDTO> sortedData = new ArrayList<>();
            if ("asc".equals(sort)) {
                sortedData = result.stream()
                        .sorted(Comparator.comparingDouble(GasPriceDTO::getDistance))  // 升序排序
                        .collect(Collectors.toList());
            }else {
                sortedData = result.stream()
                        .sorted(Comparator.comparingDouble(GasPriceDTO::getDistance).reversed())  // 升序排序
                        .collect(Collectors.toList());
            }
            Map<Integer, List<GasGdPricingDaily>> collect = handledPricingData.stream().collect(Collectors.groupingBy(GasGdPricingDaily::getId));
            for (GasPriceDTO gasPriceDTO : sortedData) {
                GasInfoDTO gasInfo0 = new GasInfoDTO();
                List<GasGdPricingDaily> dailies = collect.get(gasPriceDTO.getGasStationId());
                List<GasInfoDTO> infoDTOS = new ArrayList<>();
                gasInfo0.setGasPrice(dailies.get(0).getOil0());
                gasInfo0.setGasType(0);
                infoDTOS.add(gasInfo0);
                GasInfoDTO gasInfo92 = new GasInfoDTO();
                gasInfo92.setGasPrice(dailies.get(0).getOil92());
                gasInfo92.setGasType(92);
                infoDTOS.add(gasInfo92);
                GasInfoDTO gasInfo95 = new GasInfoDTO();
                gasInfo95.setGasPrice(dailies.get(0).getOil95());
                gasInfo95.setGasType(95);
                infoDTOS.add(gasInfo95);
                GasInfoDTO gasInfo98 = new GasInfoDTO();
                gasInfo98.setGasPrice(dailies.get(0).getOil98());
                gasInfo98.setGasType(98);
                infoDTOS.add(gasInfo98);
                gasPriceDTO.setGasStationNearbyPrice(infoDTOS);
            }
            // 计算分页的起始位置
            int fromIndex = (pageNo - 1) * size;
            int toIndex = Math.min(fromIndex + size, sortedData.size());

            // 如果起始位置超过了数组大小，返回空列表
            if (fromIndex >= sortedData.size()) {
                return new Page<>(pageNo, size, sortedData.size());
            }

            // 分页后的结果
            List<GasPriceDTO> paginatedList = sortedData.subList(fromIndex, toIndex);
            Page<GasPriceDTO> page = new Page<>(pageNo,size, sortedData.size());
            page.setRecords(paginatedList);

            return page;

        }



        return new Page<>(pageNo,size);
    }



    private List<GasGdPricingDaily> sortPricing(Integer gasolineType,List<GasGdPricingDaily> pricingDailies,String sort) {
        if (gasolineType == 0) {
            Optional<GasGdPricingDaily> minOil0Record = pricingDailies.stream()
                    .min(Comparator.comparingDouble(GasGdPricingDaily::getOil0));
            // 根据sortOrder进行排序
            List<GasGdPricingDaily> sortedList = pricingDailies.stream()
                    .sorted(getComparatorForSortOrder(sort))
                    .collect(Collectors.toList());
            sortedList.add(minOil0Record.get());
            return sortedList;
        } else if (gasolineType == 1) {
            Optional<GasGdPricingDaily> minOil92Record = pricingDailies.stream()
                    .min(Comparator.comparingDouble(GasGdPricingDaily::getOil92));
            // 根据sortOrder进行排序
            List<GasGdPricingDaily> sortedList = pricingDailies.stream()
                    .sorted(getComparatorForSortOrder(sort))
                    .collect(Collectors.toList());
            sortedList.add(minOil92Record.get());
            return sortedList;
        } else if (gasolineType == 2) {
            Optional<GasGdPricingDaily> minOil95Record = pricingDailies.stream()
                    .min(Comparator.comparingDouble(GasGdPricingDaily::getOil95));
            // 根据sortOrder进行排序
            List<GasGdPricingDaily> sortedList = pricingDailies.stream()
                    .sorted(getComparatorForSortOrder(sort))
                    .collect(Collectors.toList());
            sortedList.add(minOil95Record.get());
            return sortedList;
        } else if (gasolineType == 3) {
            Optional<GasGdPricingDaily> minOil98Record = pricingDailies.stream()
                    .min(Comparator.comparingDouble(GasGdPricingDaily::getOil98));
            // 根据sortOrder进行排序
            List<GasGdPricingDaily> sortedList = pricingDailies.stream()
                    .sorted(getComparatorForSortOrder(sort))
                    .collect(Collectors.toList());
            sortedList.add(minOil98Record.get());
            return sortedList;
        }
        return new ArrayList<>();
    }

    private static Comparator<GasGdPricingDaily> getComparatorForSortOrder(String sortOrder) {
        if ("desc".equalsIgnoreCase(sortOrder)) {
            return Comparator.comparingDouble(GasGdPricingDaily::getOil92).reversed();  // 降序
        } else {
            return Comparator.comparingDouble(GasGdPricingDaily::getOil92);  // 升序
        }
    }



    private static String getFieldForGasolineType(int gasolineType) {
        switch (gasolineType) {
            case 0:
                return "oil_0";
            case 1:
                return "oil_92";
            case 2:
                return "oil_95";
            case 3:
                return "oil_98";
            default:
                throw new IllegalArgumentException("Invalid gasolineType");
        }
    }

    private List<GasGdPricingDaily> filterPricingList(List<OGasStation> stations) {
        QueryWrapper<GasGdPricingDaily> queryWrapper = new QueryWrapper<>();
        List<Integer> stationIds = stations.stream()
                .map(OGasStation::getId)
                .collect(Collectors.toList());
        queryWrapper.in("oil_station_id", stationIds);
//        queryWrapper.eq("pricing_date", DateUtil.date().toString("yyyy-MM-dd"));
        queryWrapper.eq("pricing_date", "2024-08-23");
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
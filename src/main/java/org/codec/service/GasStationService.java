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
import org.codec.mapper.map_struct.GasStationMapStructMapper;
import org.codec.request.AddGasStationRequest;
import org.codec.request.GasStationFlowRequest;
import org.codec.util.HaversineUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Autowired
    private GasStationFlowMapper gasStationFlowMapper;

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
            gasAreaDTO.setLevel(currentArea.getLevel());

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
            return new Page<>(pageNo, size);
        }
        if (targetStation.getOriginStation() == null) {
            return new Page<>(pageNo, size);
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
            if (dis <= distance * 1000) {
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
            return new Page<>(pageNo, size);
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
                for (GasGdPricingDaily gasGdPricingDaily : sortedList) {
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
        } else if ("distance".equals(orderBy)) {
            List<GasPriceDTO> sortedData = new ArrayList<>();
            if ("asc".equals(sort)) {
                sortedData = result.stream()
                        .sorted(Comparator.comparingDouble(GasPriceDTO::getDistance))  // 升序排序
                        .collect(Collectors.toList());
            } else {
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
            Page<GasPriceDTO> page = new Page<>(pageNo, size, sortedData.size());
            page.setRecords(paginatedList);

            return page;

        }


        return new Page<>(pageNo, size);
    }


    private List<GasGdPricingDaily> sortPricing(Integer gasolineType, List<GasGdPricingDaily> pricingDailies, String sort) {
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


    private List<GasGdPricingDaily> filterPricingList(List<OGasStation> stations) {
        QueryWrapper<GasGdPricingDaily> queryWrapper = new QueryWrapper<>();
        List<Integer> stationIds = stations.stream()
                .map(OGasStation::getId)
                .collect(Collectors.toList());
        queryWrapper.in("oil_station_id", stationIds);
//        queryWrapper.eq("pricing_date", DateUtil.date().toString("yyyy-MM-dd"));
        queryWrapper.eq("pricing_date", "2024-10-07");
        return gasGdPricingDailyMapper.selectList(queryWrapper);

    }

    private List<GasGdPricingDaily> filterPricingListByPrice(List<OGasStation> stations, String sort, Integer gasType) {
        QueryWrapper<GasGdPricingDaily> queryWrapper = new QueryWrapper<>();
        List<Integer> stationIds = stations.stream()
                .map(OGasStation::getId)
                .collect(Collectors.toList());
        queryWrapper.in("oil_station_id", stationIds);
//        queryWrapper.eq("pricing_date", DateUtil.date().toString("yyyy-MM-dd"));
        queryWrapper.eq("pricing_date", "2024-10-07");

        return gasGdPricingDailyMapper.selectList(queryWrapper);

    }

    public Page<GasPriceDTO> getGasStationsList2(Integer pageNo, Integer size,
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
            return new Page<>(pageNo, size);
        }
        if (targetStation.getOriginStation() == null) {
            return new Page<>(pageNo, size);
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

        if ("distance".equals(orderBy)) {
            List<GasPriceDTO> gasPriceDTOS = generateHandleData(pageNo, size, gasolineType, sort, targetLat, targetLon, distance);
            Page<GasPriceDTO> r = new Page<>(pageNo, size);
            r.setRecords(gasPriceDTOS);
            return r;
        }
        //根据price排序
        //获取价格数据
        Page<GasGdPricingDaily> page = new Page<>(pageNo, size);
        List<OGasStation> stationsWithDistance = oGasStationMapper.findStationsWithDistance(targetLat, targetLon, distance * 1000);
        List<Integer> stationIds = stationsWithDistance.stream()
                .map(OGasStation::getId)
                .collect(Collectors.toList());
        Map<Integer, List<OGasStation>> collectStation = stationsWithDistance.stream().collect(Collectors.groupingBy(OGasStation::getId));
        Page<GasGdPricingDaily> gasGdPricingDailyIPage = gasGdPricingDailyMapper.selectPricingByStationAndType(page,
                gasolineType, stationIds, "2024-10-07", sort);
        List<GasPriceDTO> result = new ArrayList<>();
        for (GasGdPricingDaily gasGdPricingDaily : gasGdPricingDailyIPage.getRecords()) {
            GasPriceDTO gasPriceDTO = new GasPriceDTO();
            List<OGasStation> oGasStations = collectStation.get(gasGdPricingDaily.getOilStationId().intValue());
            if (CollectionUtil.isEmpty(oGasStations)) {
                continue;
            }
            gasPriceDTO.setGasStationId(gasGdPricingDaily.getOilStationId().intValue());
            gasPriceDTO.setGasLocation(oGasStations.get(0).getAddress());
            gasPriceDTO.setGasStationName(oGasStations.get(0).getName());
            gasPriceDTO.setDistance(HaversineUtil.getDistance(targetLat, targetLon,
                    Double.valueOf(oGasStations.get(0).getLat()), Double.valueOf(oGasStations.get(0).getLng())));
            gasPriceDTO.setGasStationType("normal");

            List<GasInfoDTO> infoDTOS = new ArrayList<>();
            GasInfoDTO gasInfo0 = new GasInfoDTO();
            gasInfo0.setGasType(0);
            GasInfoDTO gasInfo92 = new GasInfoDTO();
            gasInfo92.setGasType(92);
            GasInfoDTO gasInfo95 = new GasInfoDTO();
            gasInfo95.setGasType(95);
            GasInfoDTO gasInfo98 = new GasInfoDTO();
            gasInfo98.setGasType(98);
//            if (CollectionUtil.isNotEmpty(dailies)){
            gasInfo0.setGasPrice(gasGdPricingDaily.getOil0());
            gasInfo92.setGasPrice(gasGdPricingDaily.getOil92());
            gasInfo95.setGasPrice(gasGdPricingDaily.getOil95());
            gasInfo98.setGasPrice(gasGdPricingDaily.getOil98());

//            }
            infoDTOS.add(gasInfo0);
            infoDTOS.add(gasInfo92);
            infoDTOS.add(gasInfo95);
            infoDTOS.add(gasInfo98);
            gasPriceDTO.setGasStationNearbyPrice(infoDTOS);
            result.add(gasPriceDTO);
        }
        Page<GasPriceDTO> r = new Page<>(pageNo, size);
        r.setRecords(result);
        return r;
    }

//    private List<GasPriceDTO> handleLowestPrice(Integer gasolineType,List<GasPriceDTO> list) {
//        if (gasolineType == 0) {
//
//        } else if (gasolineType == 1) {
//
//        } else if (gasolineType == 2) {
//
//        } else if (gasolineType == 3) {
//
//        }
//    }

    private List<GasPriceDTO> generateHandleData(Integer pageNo, Integer size, Integer gasolineType,
                                                 String sort, double targetLat, double targetLon,
                                                 double distance) {
        Page<OGasStation> page = new Page<>(pageNo, size);
        Page<OGasStation> stationsWithinDistance = oGasStationMapper.findStationsWithinDistance(page, targetLat, targetLon, distance * 3000, sort);
        if (CollectionUtil.isEmpty(stationsWithinDistance.getRecords())) {
            return new ArrayList<>();
        }
        //获取价格数据
        List<GasGdPricingDaily> gasGdPricingDailies = filterPricingList(stationsWithinDistance.getRecords());
        // 按oil_station_id分组
        Map<Long, List<GasGdPricingDaily>> groupedByStations = gasGdPricingDailies.stream()
                .collect(Collectors.groupingBy(GasGdPricingDaily::getOilStationId));
        List<GasGdPricingDaily> stationLowestPriceList = new ArrayList<>();
        for (Map.Entry<Long, List<GasGdPricingDaily>> entry : groupedByStations.entrySet()) {
            List<GasGdPricingDaily> pricingList = entry.getValue();
            if (gasolineType == 0) {
                Optional<GasGdPricingDaily> minOil0Record = pricingList.stream()
                        .min(Comparator.comparingDouble(GasGdPricingDaily::getOil0));
                stationLowestPriceList.add(minOil0Record.get());
            } else if (gasolineType == 1) {
                Optional<GasGdPricingDaily> minOil92Record = pricingList.stream()
                        .min(Comparator.comparingDouble(GasGdPricingDaily::getOil92));
                stationLowestPriceList.add(minOil92Record.get());
            } else if (gasolineType == 2) {
                Optional<GasGdPricingDaily> minOil95Record = pricingList.stream()
                        .min(Comparator.comparingDouble(GasGdPricingDaily::getOil95));
                stationLowestPriceList.add(minOil95Record.get());
            } else if (gasolineType == 3) {
                Optional<GasGdPricingDaily> minOil98Record = pricingList.stream()
                        .min(Comparator.comparingDouble(GasGdPricingDaily::getOil98));
                stationLowestPriceList.add(minOil98Record.get());
            }
        }
        Map<Long, List<GasGdPricingDaily>> collect = stationLowestPriceList.stream().collect(Collectors.groupingBy(GasGdPricingDaily::getOilStationId));
        List<GasPriceDTO> r = new ArrayList<>();
        for (OGasStation oGasStation : stationsWithinDistance.getRecords()) {
            GasPriceDTO gasPriceDTO = new GasPriceDTO();
            gasPriceDTO.setGasStationId(oGasStation.getId());
            gasPriceDTO.setGasLocation(oGasStation.getAddress());
            gasPriceDTO.setGasStationName(oGasStation.getName());
            gasPriceDTO.setDistance(HaversineUtil.getDistance(targetLat, targetLon, Double.valueOf(oGasStation.getLat()), Double.valueOf(oGasStation.getLng())));
            gasPriceDTO.setGasStationType("normal");
            GasInfoDTO gasInfo0 = new GasInfoDTO();
            List<GasGdPricingDaily> dailies = collect.get(Long.valueOf(gasPriceDTO.getGasStationId()));
            List<GasInfoDTO> infoDTOS = new ArrayList<>();
            gasInfo0.setGasType(0);
            GasInfoDTO gasInfo92 = new GasInfoDTO();
            gasInfo92.setGasType(92);
            GasInfoDTO gasInfo95 = new GasInfoDTO();
            gasInfo95.setGasType(95);
            GasInfoDTO gasInfo98 = new GasInfoDTO();
            gasInfo98.setGasType(98);
            if (CollectionUtil.isNotEmpty(dailies)) {
                gasInfo0.setGasPrice(dailies.get(0).getOil0());
                gasInfo92.setGasPrice(dailies.get(0).getOil92());
                gasInfo95.setGasPrice(dailies.get(0).getOil95());
                gasInfo98.setGasPrice(dailies.get(0).getOil98());

            }
            infoDTOS.add(gasInfo0);
            infoDTOS.add(gasInfo92);
            infoDTOS.add(gasInfo95);
            infoDTOS.add(gasInfo98);
            gasPriceDTO.setGasStationNearbyPrice(infoDTOS);
            r.add(gasPriceDTO);
        }
        return r;
    }

    public Page<GasPriceDTO> getStationFlowList(Integer pageNo, Integer size, String keyWord, String userId, String stationId) {
        Page<GasStationFlow> page = new Page<>(pageNo, size);
        Page<GasPriceDTO> pageResult = new Page<>(pageNo, size);
        QueryWrapper<GasStationFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (!Objects.isNull(keyWord) && !keyWord.isEmpty()) {
            queryWrapper.like("gas_station_name", keyWord);
        }
        page = gasStationFlowMapper.selectPage(page, queryWrapper);

        GasStation gasStation = gasStationMapper.selectById(stationId);
        Long targetLatitude = gasStation.getLatitude().longValue();
        Long targetLongitude = gasStation.getLongitude().longValue();

        List<GasPriceDTO> result = new ArrayList<>();
        for (GasStationFlow gasStationFlow : page.getRecords()) {
            GasPriceDTO gasPriceDTO = new GasPriceDTO();
            gasPriceDTO.setGasStationType("normal");
            gasPriceDTO.setGasStationName(gasStationFlow.getGasStationName());
            gasPriceDTO.setGasLocation(gasStationFlow.getAddress());
            gasPriceDTO.setGasStationId(gasStationFlow.getGasStationId().intValue());
            gasPriceDTO.setDistance(HaversineUtil.getDistance(targetLatitude, targetLongitude,
                    Double.valueOf(gasStationFlow.getLat()), Double.valueOf(gasStationFlow.getLng())));

            GasInfoDTO gasInfo0 = new GasInfoDTO();
            List<GasInfoDTO> infoDTOS = new ArrayList<>();
            gasInfo0.setGasType(0);
            GasInfoDTO gasInfo92 = new GasInfoDTO();
            gasInfo92.setGasType(92);
            GasInfoDTO gasInfo95 = new GasInfoDTO();
            gasInfo95.setGasType(95);
            GasInfoDTO gasInfo98 = new GasInfoDTO();
            gasInfo98.setGasType(98);
            gasInfo0.setGasPrice(Double.valueOf(gasStationFlow.getOil0()));
            gasInfo92.setGasPrice(Double.valueOf(gasStationFlow.getOil92()));
            gasInfo95.setGasPrice(Double.valueOf(gasStationFlow.getOil95()));
            gasInfo98.setGasPrice(Double.valueOf(gasStationFlow.getOil98()));
            infoDTOS.add(gasInfo0);
            infoDTOS.add(gasInfo92);
            infoDTOS.add(gasInfo95);
            infoDTOS.add(gasInfo98);
            gasPriceDTO.setGasStationNearbyPrice(infoDTOS);
            result.add(gasPriceDTO);
        }
        pageResult.setRecords(result);

        return pageResult;
    }

    public void flow(GasStationFlowRequest gasStationFlowRequest) {
        GasStationFlow gasStationFlow = new GasStationFlow();
        BeanUtils.copyProperties(gasStationFlowRequest, gasStationFlow);
        gasStationFlowMapper.insert(gasStationFlow);
    }


    public GasStationDetailDTO getStationNormalDetail(String stationId, String normalStationId, Integer distanceType) {
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

        GasStationDetailDTO result = new GasStationDetailDTO();
        OGasStation oGasStation = oGasStationMapper.selectById(normalStationId);
        GasStation gasStation = gasStationMapper.selectById(stationId);
        result.setGasStationName(gasStation.getName());
        result.setDistance(HaversineUtil.getDistance(gasStation.getLatitude().doubleValue(), gasStation.getLongitude().doubleValue(),
                Double.valueOf(oGasStation.getLat()), Double.valueOf(oGasStation.getLng())));
        result.setNormalGasStationName(oGasStation.getName());

        List<OGasStation> stationsWithDistance = oGasStationMapper.findStationsWithDistance(gasStation.getLatitude().doubleValue(),
                gasStation.getLongitude().doubleValue(), distance * 1000);
        List<Integer> stationIds = stationsWithDistance.stream()
                .map(OGasStation::getId)
                .collect(Collectors.toList());
        QueryWrapper<GasGdPricingDaily> PriceQueryWrapper = new QueryWrapper<>();
        PriceQueryWrapper.in("oil_station_id", stationIds);
        List<GasGdPricingDaily> gasGdPricingDailiesWithStations = gasGdPricingDailyMapper.selectList(PriceQueryWrapper);
        Map<Long, List<GasGdPricingDaily>> collectByOStationId = gasGdPricingDailiesWithStations.stream().collect(Collectors.groupingBy(GasGdPricingDaily::getOilStationId));
        Double stationPrice0 = 0.0;
        Double stationPrice92 = 0.0;
        Double stationPrice95 = 0.0;
        Double stationPrice98 = 0.0;
        for (Map.Entry<Long, List<GasGdPricingDaily>> entry : collectByOStationId.entrySet()) {
            List<GasGdPricingDaily> values = entry.getValue();
            Double sourcePrices0 = 0.0;
            Double sourcePrices92 = 0.0;
            Double sourcePrices095 = 0.0;
            Double sourcePrices98 = 0.0;
            for (GasGdPricingDaily gasGdPricingDaily : values) {
                sourcePrices0 = sourcePrices0 + gasGdPricingDaily.getOil0();
                sourcePrices92 = sourcePrices92 + gasGdPricingDaily.getOil92();
                sourcePrices095 = sourcePrices095 + gasGdPricingDaily.getOil95();
                sourcePrices98 = sourcePrices98 + gasGdPricingDaily.getOil98();
            }
            stationPrice0 = stationPrice0 + (sourcePrices0 / values.size());
            stationPrice92 = stationPrice92 + (sourcePrices92 / values.size());
            stationPrice95 = stationPrice95 + (sourcePrices095 / values.size());
            stationPrice98 = stationPrice98 + (sourcePrices98 / values.size());
        }
        result.setOil0PriceAvg(String.valueOf(stationPrice0/collectByOStationId.size()));
        result.setOil92PriceAvg(String.valueOf(stationPrice92/collectByOStationId.size()));
        result.setOil95PriceAvg(String.valueOf(stationPrice95/collectByOStationId.size()));
        result.setOil98PriceAvg(String.valueOf(stationPrice98/collectByOStationId.size()));
        LocalDate today = LocalDate.now();

        // 获取 30 天前的日期
        LocalDate thirtyDaysAgo = today.minusDays(30);
        // 创建 QueryWrapper，用于构建查询条件
        QueryWrapper<GasGdPricingDaily> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("oil_station_id", normalStationId);
        queryWrapper.between("pricing_date", thirtyDaysAgo.minusDays(30), today.minusDays(30));
        queryWrapper.orderByDesc("pricing_date");

        List<GasGdPricingDaily> gasGdPricingDailies = gasGdPricingDailyMapper.selectList(queryWrapper);
        Map<Integer, List<GasGdPricingDaily>> collectBySource = gasGdPricingDailies.stream().collect(Collectors.groupingBy(GasGdPricingDaily::getSource));

        List<PlatformPriceDTO> platformPriceDTOS = new ArrayList<>();
        ChartDTO chartDTO = new ChartDTO();

        for (Map.Entry<Integer, List<GasGdPricingDaily>> entry : collectBySource.entrySet()) {
            Integer source = entry.getKey();
            List<GasGdPricingDaily> values = entry.getValue();

            if (source == 3) {
                List<Integer> xAxisDays = new ArrayList<>();
                List<Integer> datas0 = new ArrayList<>();
                List<Integer> datas92 = new ArrayList<>();
                List<Integer> datas95 = new ArrayList<>();
                List<Integer> datas98 = new ArrayList<>();
                List<ChartValue> chartValues = new ArrayList<>();
                ChartValue chartValue0 = new ChartValue();
                ChartValue chartValue92 = new ChartValue();
                ChartValue chartValue95 = new ChartValue();
                ChartValue chartValue98 = new ChartValue();
                chartValue0.setName("0#");
                chartValue92.setName("92#");
                chartValue95.setName("95#");
                chartValue98.setName("98#");
                ChartData chartData = new ChartData();
                for (GasGdPricingDaily gasGdPricingDaily : values) {
                    LocalDate pricingDate = gasGdPricingDaily.getPricingDate();
                    xAxisDays.add(pricingDate.getDayOfMonth());
                    datas0.add(gasGdPricingDaily.getOil0());
                    datas92.add(gasGdPricingDaily.getOil92());
                    datas95.add(gasGdPricingDaily.getOil95());
                    datas98.add(gasGdPricingDaily.getOil98());

                    if (pricingDate.equals(LocalDate.now())) {
                        PlatformPriceDTO platformPriceDTO = new PlatformPriceDTO();
                        platformPriceDTO.setTuanyouPrice(String.valueOf(gasGdPricingDaily.getOil0()));
                        platformPriceDTO.setPriceType("0#");
                        platformPriceDTO.setTuanyouPrice(String.valueOf(gasGdPricingDaily.getOil92()));
                        platformPriceDTO.setPriceType("92#");
                        platformPriceDTO.setTuanyouPrice(String.valueOf(gasGdPricingDaily.getOil95()));
                        platformPriceDTO.setPriceType("95#");
                        platformPriceDTO.setTuanyouPrice(String.valueOf(gasGdPricingDaily.getOil98()));
                        platformPriceDTO.setPriceType("98#");
                        platformPriceDTOS.add(platformPriceDTO);
                    }
                }
                chartValue0.setData(datas0);
                chartValue92.setData(datas92);
                chartValue95.setData(datas95);
                chartValue98.setData(datas98);
                chartValues.add(chartValue0);
                chartValues.add(chartValue92);
                chartValues.add(chartValue95);
                chartValues.add(chartValue98);
                chartData.setValue(chartValues);
                chartData.setXAxis(xAxisDays);
                chartDTO.setTuanyou(chartData);


            } else if (source == 2) {

            } else if (source == 1) {

            }
        }

        result.setChart(chartDTO);
        result.setPlatformPriceList(platformPriceDTOS);
        return result;

    }

}
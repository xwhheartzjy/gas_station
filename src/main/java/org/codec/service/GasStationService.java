package org.codec.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.codec.common.RequestContext;
import org.codec.dto.*;
import org.codec.entity.*;
import org.codec.entity.third.AreaReport;
import org.codec.enums.BizCodeEnum;
import org.codec.exception.BizException;
import org.codec.mapper.*;
import org.codec.mapper.map_struct.GasStationMapStructMapper;
import org.codec.mapper.thrid.AreaReportMapper;
import org.codec.request.AddGasStationRequest;
import org.codec.request.GasStationFlowRequest;
import org.codec.util.HaversineUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    @Autowired
    private AreaReportMapper areaReportMapper;

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


    private List<GasGdPricingDaily> filterPricingList(List<OGasStation> stations) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        QueryWrapper<GasGdPricingDaily> queryWrapper = new QueryWrapper<>();
        List<Integer> stationIds = stations.stream()
                .map(OGasStation::getId)
                .collect(Collectors.toList());
        queryWrapper.in("oil_station_id", stationIds);
        queryWrapper.eq("pricing_date", LocalDate.now().minusDays(2).format(formatter));
//        queryWrapper.eq("pricing_date", "2024-10-07");
        return gasGdPricingDailyMapper.selectList(queryWrapper);

    }

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

        List<GasPriceDTO> gasPriceDTOS = generateHandleData(pageNo, size, gasolineType, sort, targetLat, targetLon, distance, orderBy);
        Page<GasPriceDTO> r = new Page<>(pageNo, size);
        r.setRecords(handleLowestPrice(gasolineType, gasPriceDTOS));
        return r;

    }

    private List<GasPriceDTO> handleLowestPrice(Integer gasolineType, List<GasPriceDTO> list) {
        // 初始化变量
        GasPriceDTO minPriceOuterObject = null;
        double minPrice = Double.MAX_VALUE;

        // 遍历外层列表
        for (GasPriceDTO gasPriceDTO : list) {
            if (gasPriceDTO.getGasStationNearbyPrice() != null) {
                for (GasInfoDTO gasInfoDTO : gasPriceDTO.getGasStationNearbyPrice()) {
                    // 检查是否符合目标 type 并更新最小 price
                    if (gasInfoDTO.getGasType() == gasolineType && Double.valueOf(gasInfoDTO.getGasPrice()) < minPrice) {
                        minPrice = Double.valueOf(gasInfoDTO.getGasPrice());
                        minPriceOuterObject = gasPriceDTO;
                    }
                }
            }
        }

        // 如果找到符合条件的对象，将其移到最上面
        if (minPriceOuterObject != null) {
            list.remove(minPriceOuterObject);
            list.add(0, minPriceOuterObject);
        }
        return list;
    }

    private List<GasPriceDTO> generateHandleData(Integer pageNo, Integer size, Integer gasolineType,
                                                 String sort, double targetLat, double targetLon,
                                                 double distance, String orderBy) {
        Page<OGasStation> page = new Page<>(pageNo, size);
        Page<OGasStation> stationsWithinDistance = oGasStationMapper.findStationsWithDistancePage(page, targetLat, targetLon, distance);
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
            gasPriceDTO.setGasStationId(String.valueOf(oGasStation.getId()));
            gasPriceDTO.setGasLocation(oGasStation.getAddress());
            gasPriceDTO.setGasStationName(oGasStation.getName());
            gasPriceDTO.setDistance(String.format("%.1f", HaversineUtil.getDistance(targetLat, targetLon, Double.valueOf(oGasStation.getLat()), Double.valueOf(oGasStation.getLng()))));
            gasPriceDTO.setGasStationType("normal");
            gasPriceDTO.setLat(oGasStation.getLat());
            gasPriceDTO.setLng(oGasStation.getLng());
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
            if (CollectionUtil.isEmpty(dailies)) {
                continue;
            }
            gasInfo0.setGasPrice(String.format("%.2f", Double.valueOf(dailies.get(0).getOil0()) / 100.0));
            gasInfo92.setGasPrice(String.format("%.2f", Double.valueOf(dailies.get(0).getOil92()) / 100.0));
            gasInfo95.setGasPrice(String.format("%.2f", Double.valueOf(dailies.get(0).getOil95()) / 100.0));
            gasInfo98.setGasPrice(String.format("%.2f", Double.valueOf(dailies.get(0).getOil98()) / 100.0));

            infoDTOS.add(gasInfo0);
            infoDTOS.add(gasInfo92);
            infoDTOS.add(gasInfo95);
            infoDTOS.add(gasInfo98);
            gasPriceDTO.setGasStationNearbyPrice(infoDTOS);
            r.add(gasPriceDTO);
        }
        sortGasPriceList(r, orderBy, sort, gasolineType);
        return r;
    }


    private void sortGasPriceList(List<GasPriceDTO> gasPriceDTOList, String orderBy, String sort, Integer gasType) {
        // 根据排序顺序选择正序或倒序
        boolean isAscending = "asc".equalsIgnoreCase(sort);

        // 排序的主逻辑
        if ("distance".equalsIgnoreCase(orderBy)) {
            // 按照距离排序
            gasPriceDTOList.sort((dto1, dto2) -> {
                int comparison = Double.compare(Double.parseDouble(dto1.getDistance()), Double.parseDouble(dto2.getDistance()));
                return isAscending ? comparison : -comparison;
            });
        } else if ("price".equalsIgnoreCase(orderBy)) {
            // 按照油品价格排序，根据gasType来判断优先级
            gasPriceDTOList.sort((dto1, dto2) -> {
                // 获取每个加油站的目标油品（根据gasType进行筛选）
                GasInfoDTO price1 = getGasInfoByType(dto1, gasType); // 默认选择92类型的油品，您可以根据需要修改
                GasInfoDTO price2 = getGasInfoByType(dto2, gasType);

                if (price1 != null && price2 != null) {
                    int comparison = Double.compare(Double.parseDouble(price1.getGasPrice()), Double.parseDouble(price2.getGasPrice()));
                    return isAscending ? comparison : -comparison;
                }
                return 0; // 如果价格没有匹配的油品则认为相等
            });
        }
    }

    // 根据油品类型从GasPriceDTO中获取指定类型的GasInfoDTO
    private GasInfoDTO getGasInfoByType(GasPriceDTO dto, Integer gasType) {
        Integer type = 0;
        if (gasType == 0) {
            type = 0;
        } else if (gasType == 1) {
            type = 92;
        } else if (gasType == 2) {
            type = 95;
        } else if (gasType == 3) {
            type = 98;
        }
        for (GasInfoDTO gasInfoDTO : dto.getGasStationNearbyPrice()) {
            if (gasInfoDTO.getGasType() == type) {
                return gasInfoDTO;
            }
        }
        return null;
    }


    public Page<GasPriceDTO> getStationFlowList(Integer pageNo, Integer size, String keyWord, String userId, String stationId) {
        Page<GasStationFlow> page = new Page<>(pageNo, size);
        Page<GasPriceDTO> pageResult = new Page<>(pageNo, size);
        QueryWrapper<GasStationFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("flow", true);
        if (!Objects.isNull(keyWord) && !keyWord.isEmpty()) {
            queryWrapper.like("gas_station_name", keyWord);
        }
        page = gasStationFlowMapper.selectPage(page, queryWrapper);
        Double targetLatitude = 0.0;
        Double targetLongitude = 0.0;

        if (stationId != "") {
            GasStationConsumer gasStation = gasStationConsumerMapper.selectById(stationId);
            targetLatitude = gasStation.getLatitude().doubleValue();
            targetLongitude = gasStation.getLongitude().doubleValue();
        }


        List<GasPriceDTO> result = new ArrayList<>();
        for (GasStationFlow gasStationFlow : page.getRecords()) {
            GasPriceDTO gasPriceDTO = new GasPriceDTO();
            gasPriceDTO.setGasStationType("normal");
            gasPriceDTO.setGasStationName(gasStationFlow.getGasStationName());
            gasPriceDTO.setGasLocation(gasStationFlow.getAddress());
            gasPriceDTO.setGasStationId(String.valueOf(gasStationFlow.getGasStationId()));

            if (stationId != "") {
                gasPriceDTO.setDistance(String.format("%.1f", HaversineUtil.getDistance(targetLatitude, targetLongitude,
                        Double.valueOf(gasStationFlow.getLat()), Double.valueOf(gasStationFlow.getLng()))));
            }
            gasPriceDTO.setLat(gasStationFlow.getLat());
            gasPriceDTO.setLng(gasStationFlow.getLng());
            gasPriceDTO.setFollow(gasStationFlow.getFlow());

            GasInfoDTO gasInfo0 = new GasInfoDTO();
            List<GasInfoDTO> infoDTOS = new ArrayList<>();
            gasInfo0.setGasType(0);
            GasInfoDTO gasInfo92 = new GasInfoDTO();
            gasInfo92.setGasType(92);
            GasInfoDTO gasInfo95 = new GasInfoDTO();
            gasInfo95.setGasType(95);
            GasInfoDTO gasInfo98 = new GasInfoDTO();
            gasInfo98.setGasType(98);
            if (gasStationFlow.getOil0() != null) {
                gasInfo0.setGasPrice(String.format("%.2f", Double.valueOf(gasStationFlow.getOil0())));
            }
            if (gasStationFlow.getOil92() != null) {
                gasInfo92.setGasPrice(String.format("%.2f", Double.valueOf(gasStationFlow.getOil92())));
            }
            if (gasStationFlow.getOil95() != null) {
                gasInfo95.setGasPrice(String.format("%.2f", Double.valueOf(gasStationFlow.getOil95())));
            }
            if (gasStationFlow.getOil98() != null) {
                gasInfo98.setGasPrice(String.format("%.2f", Double.valueOf(gasStationFlow.getOil98())));
            }
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
        QueryWrapper<GasStationFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("gas_station_id", gasStationFlowRequest.getGasStationId());
        queryWrapper.eq("user_id", gasStationFlowRequest.getUserId());
        List<GasStationFlow> gasStationFlows = gasStationFlowMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(gasStationFlows)) {
            UpdateWrapper<GasStationFlow> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("gas_station_id", gasStationFlowRequest.getGasStationId());
            updateWrapper.eq("user_id", gasStationFlowRequest.getUserId());
            updateWrapper.set("flow", gasStationFlowRequest.getFlow());
            gasStationFlowMapper.update(null, updateWrapper);
            return;
        }
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
        GasStationConsumer gasStation = gasStationConsumerMapper.selectById(stationId);
        if (gasStation == null) {
            throw new BizException(BizCodeEnum.STATION_EMPTY_ERROR);
        }
        result.setGasStationName(gasStation.getName());
        result.setDistance(String.format("%.1f", HaversineUtil.getDistance(gasStation.getLatitude().doubleValue(), gasStation.getLongitude().doubleValue(),
                Double.valueOf(oGasStation.getLat()), Double.valueOf(oGasStation.getLng()))));
        result.setNormalGasStationName(oGasStation.getName());

        List<OGasStation> stationsWithDistance = oGasStationMapper.findStationsWithDistance(gasStation.getLatitude().doubleValue(),
                gasStation.getLongitude().doubleValue(), distance);
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
        result.setOil0PriceAvg(String.format("%.2f", (stationPrice0 / collectByOStationId.size()) / 100.0));
        result.setOil92PriceAvg(String.format("%.2f", (stationPrice92 / collectByOStationId.size()) / 100.0));
        result.setOil95PriceAvg(String.format("%.2f", (stationPrice95 / collectByOStationId.size()) / 100.0));
        result.setOil98PriceAvg(String.format("%.2f", (stationPrice98 / collectByOStationId.size()) / 100.0));
        LocalDate today = LocalDate.now();

        // 获取 30 天前的日期
        LocalDate thirtyDaysAgo = today.minusDays(30);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 创建 QueryWrapper，用于构建查询条件
        QueryWrapper<GasGdPricingDaily> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("oil_station_id", normalStationId);
        queryWrapper.between("pricing_date", thirtyDaysAgo.format(dateTimeFormatter), today.format(dateTimeFormatter));
//        queryWrapper.orderByDesc("pricing_date");
        List<GasGdPricingDaily> gasGdPricingDailies = gasGdPricingDailyMapper.selectList(queryWrapper);

        List<String> days = generateDaysList("yyyy-MM-dd");
        List<String> daysForDay = generateDaysList("dd");

        Map<Integer, List<GasGdPricingDaily>> collectBySource = gasGdPricingDailies.stream().collect(Collectors.groupingBy(GasGdPricingDaily::getSource));
        List<PlatformPriceDTO> platformPriceDTOS = new ArrayList<>();
        ChartDTO chartDTO = new ChartDTO();
        for (GasGdPricingDaily gasGdPricingDaily : gasGdPricingDailies) {
            if (!gasGdPricingDaily.getPricingDate().isEqual(LocalDate.now().minusDays(3))) {
                continue;
            }
            if (gasGdPricingDaily.getSource() == 3) {
                PlatformPriceDTO platformPriceDTO0 = new PlatformPriceDTO();
                platformPriceDTO0.setTuanyouPrice(String.format("%.2f", gasGdPricingDaily.getOil0() / 100.0));
                platformPriceDTO0.setPriceType("0#");
                platformPriceDTOS.add(platformPriceDTO0);
                PlatformPriceDTO platformPriceDTO92 = new PlatformPriceDTO();
                platformPriceDTO92.setTuanyouPrice(String.format("%.2f", gasGdPricingDaily.getOil92() / 100.0));
                platformPriceDTO92.setPriceType("92#");
                platformPriceDTOS.add(platformPriceDTO92);
                PlatformPriceDTO platformPriceDTO95 = new PlatformPriceDTO();
                platformPriceDTO95.setTuanyouPrice(String.format("%.2f", gasGdPricingDaily.getOil95() / 100.0));
                platformPriceDTO95.setPriceType("95#");
                platformPriceDTOS.add(platformPriceDTO95);
                PlatformPriceDTO platformPriceDTO98 = new PlatformPriceDTO();
                platformPriceDTO98.setTuanyouPrice(String.format("%.2f", gasGdPricingDaily.getOil98() / 100.0));
                platformPriceDTO98.setPriceType("98#");
                platformPriceDTOS.add(platformPriceDTO98);
            }
        }

        for (Map.Entry<Integer, List<GasGdPricingDaily>> entry : collectBySource.entrySet()) {
            Integer source = entry.getKey();
            buildChartData(daysForDay, source, days, gasGdPricingDailies, chartDTO);
        }
        QueryWrapper<AreaReport> areaReportQueryWrapper = new QueryWrapper<>();
        areaReportQueryWrapper.eq("gas_station_id", gasStation.getOriginStation());
        AreaReport areaReport = areaReportMapper.selectOne(areaReportQueryWrapper);
        if (areaReport != null) {
            AreaReportSummaryDTO areaReportSummaryDTO = areaReportMapper.queryAreaSummary(areaReport.getId());
            result.setBusinessCircleNumber(String.valueOf(areaReportSummaryDTO.getBusinessCircleNumber()));
            result.setAverageRoomRate(String.valueOf(areaReportSummaryDTO.getAverageRoomRate()));
            result.setUptownCount(String.valueOf(areaReportSummaryDTO.getUptownCount()));
            result.setOfficeCount(String.valueOf(areaReportSummaryDTO.getOfficeBuildingCount()));
            result.setTotalResidents(String.valueOf(areaReportSummaryDTO.getTotalHouseHold()));
            result.setRentPrice(String.valueOf(areaReportSummaryDTO.getAverageRent()));
        }

        result.setChart(chartDTO);
        result.setPlatformPriceList(platformPriceDTOS);
        return result;

    }

    private void buildChartData(List<String> daysForDay, Integer dataType, List<String> days, List<GasGdPricingDaily> dailies, ChartDTO chartDTO) {
        ChartData chartData = new ChartData();
        chartData.setXAxis(daysForDay);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<LocalDate, List<GasGdPricingDaily>> collectByDate = dailies.stream().collect(Collectors.groupingBy(GasGdPricingDaily::getPricingDate));

        List<ChartValue> chartValues = new ArrayList<>();
        ChartValue chartValue0 = new ChartValue();
        ChartValue chartValue92 = new ChartValue();
        ChartValue chartValue95 = new ChartValue();
        ChartValue chartValue98 = new ChartValue();
        List<String> datas0 = new ArrayList<>();
        List<String> datas92 = new ArrayList<>();
        List<String> datas95 = new ArrayList<>();
        List<String> datas98 = new ArrayList<>();
        chartValue0.setName("0#");
        chartValue92.setName("92#");
        chartValue95.setName("95#");
        chartValue98.setName("98#");
        for (String date : days) {
            for (Map.Entry<LocalDate, List<GasGdPricingDaily>> entry : collectByDate.entrySet()) {
                if (!entry.getKey().format(formatter).equals(date)) {
                    datas0.add("0.00");
                    datas92.add("0.00");
                    datas95.add("0.00");
                    datas98.add("0.00");
                    continue;
                }
                List<GasGdPricingDaily> values = entry.getValue();
                for (GasGdPricingDaily gasGdPricingDaily : values) {
                    if (gasGdPricingDaily.getSource().equals(dataType)) {
                        datas0.add(String.format("%.2f", gasGdPricingDaily.getOil0() / 100.0));
                        datas92.add(String.format("%.2f", gasGdPricingDaily.getOil92() / 100.0));
                        datas95.add(String.format("%.2f", gasGdPricingDaily.getOil95() / 100.0));
                        datas98.add(String.format("%.2f", gasGdPricingDaily.getOil98() / 100.0));
                    }
                }
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
        if (dataType == 3) {
            chartDTO.setTuanyou(chartData);
        } else if (dataType == 0) {
            chartDTO.setDouyin(chartData);
        } else {
            chartDTO.setDidi(chartData);
        }

    }

    private List<String> generateDaysList(String format) {
        List<String> dateList = new ArrayList<>();
        LocalDate currentDate = LocalDate.now(); // 当前日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        // 从当前日期往前推30天
        for (int i = 30; i >= 1; i--) {
            LocalDate date = currentDate.minusDays(i);
            dateList.add(date.format(formatter)); // 格式化为 yyyy-MM-dd 并添加到列表
        }

        return dateList;
    }


    public void init(String userId) {
        QueryWrapper<GasStation> gasStationQueryWrapper = new QueryWrapper<>();
        gasStationQueryWrapper.eq("del_flag", 0);
        List<GasStation> gasStations = gasStationMapper.selectList(gasStationQueryWrapper);
        for (GasStation gasStation : gasStations) {
            GasStationConsumer gasStationConsumer = new GasStationConsumer();
            BeanUtils.copyProperties(gasStation, gasStationConsumer);
            gasStationConsumer.setOriginStation(gasStation.getStationId());
            gasStationConsumer.setUserId(Long.valueOf(userId));
            gasStationConsumerMapper.insert(gasStationConsumer);
        }

    }

}
package org.codec.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.codec.entity.GasStation;
import org.codec.enums.BizCodeEnum;
import org.codec.request.AddGasStationRequest;
import org.codec.request.DeleteGasStationRequest;
import org.codec.request.GasStationFlowRequest;
import org.codec.service.GasStationService;
import org.codec.util.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gas_station")
public class GasStationController {
    private static final Logger logger = LoggerFactory.getLogger(GasStationController.class);

    @Autowired
    private GasStationService gasStationService;

    @GetMapping("/list")
    public JsonData getGasStationList(
            @RequestParam(required = false, name = "page_no", defaultValue = "1") int pageNo,
            @RequestParam(required = false, name = "size", defaultValue = "10") Integer size,
            @RequestParam(required = false, name = "station_id") String stationId,
            @RequestParam(required = false, name = "gasoline_type", defaultValue = "1") Integer gasolineType,
            @RequestParam(required = false, name = "distance_type", defaultValue = "0") Integer distanceType,
            @RequestParam(required = false, name = "order_by", defaultValue = "price") String orderBy,
            @RequestParam(required = false, name = "sort", defaultValue = "desc") String sort) {

        // 调用服务层获取分页数据
        return JsonData.buildSuccess(gasStationService.getGasStationsList(pageNo, size, stationId,
                gasolineType, distanceType, orderBy, sort));
    }

    @GetMapping("/list_by_user")
    public JsonData listStationByUser(@RequestParam(required = true, name = "user_id") String userId) {
        return JsonData.buildSuccess(gasStationService.listStationByUser(userId));
    }

    @PostMapping("/add")
    public JsonData add(@RequestBody AddGasStationRequest request) {
        gasStationService.addGasStation(request);
        return JsonData.buildSuccess();
    }

    @PostMapping("/delete")
    public JsonData add(@RequestBody DeleteGasStationRequest request) {
        gasStationService.deleteGasStation(request.getStationId());
        return JsonData.buildSuccess();
    }

    @PostMapping("/update")
    public JsonData update(@RequestBody AddGasStationRequest request) {
        gasStationService.updateGasStation(request);
        return JsonData.buildSuccess();
    }

    @GetMapping("/detail")
    public JsonData detail(@RequestParam(name = "station_id") String stationId) {
        return JsonData.buildSuccess(gasStationService.getStationDetail(stationId));
    }

    @GetMapping("/flow/list")
    public JsonData getFlowList(@RequestParam(name = "page_no", defaultValue = "1") Integer pageNo,
                                @RequestParam(name = "size") Integer size,
                                @RequestParam(name = "key_word",required = false) String keyWord,
                                @RequestParam(name = "user_id") String userId,
                                @RequestParam(name = "station_id",required = false) String stationId) {
        return JsonData.buildSuccess(gasStationService.getStationFlowList(pageNo,size,keyWord,userId,stationId));
    }

    @PostMapping("/flow")
    public JsonData flow(@RequestBody GasStationFlowRequest gasStationFlowRequest) {
        gasStationService.flow(gasStationFlowRequest);
        return JsonData.buildSuccess();
    }

    @GetMapping("/normal/detail")
    public JsonData normalDetail(@RequestParam(name = "station_id",required = true) String stationId,
                                 @RequestParam(name = "normal_station_id") String normalStationId,
                                 @RequestParam(name = "distance_type") Integer distanceType) {
        return JsonData.buildSuccess(gasStationService.getStationNormalDetail(stationId,normalStationId,distanceType));
    }


    @GetMapping("/init")
    public JsonData init(@RequestParam(name = "user_id")String userId) {
        gasStationService.init(userId);
        return JsonData.buildSuccess();
    }

}

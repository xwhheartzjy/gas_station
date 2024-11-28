package org.codec.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.codec.entity.GasStation;
import org.codec.request.AddGasStationRequest;
import org.codec.service.GasStationService;
import org.codec.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gas_station")
public class GasStationController {

    @Autowired
    private GasStationService gasStationService;

    // 获取加油站列表，支持分页、过滤、排序
//    @GetMapping("/list")
//    public Page<GasStation> getGasStationList(
//            @RequestParam int page_no,
//            @RequestParam int size,
//            @RequestParam String station_id,
//            @RequestParam(required = false) Integer gasoline_type,
//            @RequestParam(required = false) Integer distance_type,
//            @RequestParam(required = false) String order_by) {
//
//        // 调用服务层获取分页数据
//        return JsonData.buildSuccess(gasStationService.getGasStationsList(page_no, size, station_id, gasoline_type, distance_type, order_by));
//    }

    @GetMapping("/list_by_user")
    public JsonData listStationByUser(@RequestParam(required = true,name = "user_id") String userId) {
        return JsonData.buildSuccess(gasStationService.listStationByUser(userId));
    }

    @PostMapping("/add")
    public JsonData add(@RequestBody AddGasStationRequest request) {

        return JsonData.buildSuccess();

    }
}

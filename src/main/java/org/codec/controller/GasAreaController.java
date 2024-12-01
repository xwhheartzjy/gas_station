package org.codec.controller;

import org.codec.request.AddGasStationRequest;
import org.codec.service.GasAreaService;
import org.codec.service.GasStationService;
import org.codec.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gas_area")
public class GasAreaController {

    @Autowired
    private GasAreaService gasAreaService;

    @GetMapping("/list_by_level")
    public JsonData listStationByUser(@RequestParam(required = true, name = "level") String level,
                                      @RequestParam(required = false,name="parent_id") Long parentId) {
        return JsonData.buildSuccess(gasAreaService.getGasAreaListByLevel(level,parentId));
    }


}

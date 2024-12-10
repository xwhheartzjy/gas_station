package org.codec.controller;

import org.codec.dto.GasUserDTO;
import org.codec.entity.SysUser;
import org.codec.enums.BizCodeEnum;
import org.codec.model.LoginRequest;
import org.codec.service.GasStationService;
import org.codec.service.SysUserService;
import org.codec.util.JsonData;
import org.codec.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Autowired
    private GasStationService gasStationService;

    @GetMapping("/check")
    public JsonData check(@RequestParam(name = "gas_station_id",required = true) String gasStationId) {

        return JsonData.buildSuccess(gasStationService.checkPricing(gasStationId));
    }
}
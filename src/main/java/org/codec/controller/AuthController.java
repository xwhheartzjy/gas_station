package org.codec.controller;
import org.codec.dto.GasUserDTO;
import org.codec.entity.SysUser;
import org.codec.enums.BizCodeEnum;
import org.codec.model.LoginRequest;
import org.codec.service.SysUserService;
import org.codec.util.JsonData;
import org.codec.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping
public class AuthController {

    private final JwtTokenUtils jwtTokenUtils;

    public AuthController(JwtTokenUtils jwtTokenUtils) {
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public JsonData login(@RequestBody LoginRequest request) {

        SysUser user = sysUserService.getUserByUsernameAndPassword(request.getUsername(), request.getPassword());
        if (user == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
        }

        Map map = new HashMap();
        map.put("user",request.getUsername());
        map.put("password",request.getPassword());
        map.put("userId",user.getUserId());
        GasUserDTO gasUserDTO = new GasUserDTO();
        gasUserDTO.setToken(jwtTokenUtils.createToken(map));
        gasUserDTO.setUsername(request.getUsername());
        gasUserDTO.setUserId(String.valueOf(user.getUserId()));
        return JsonData.buildSuccess(gasUserDTO);
    }
}
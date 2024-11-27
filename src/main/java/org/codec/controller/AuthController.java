package org.codec.controller;
import org.codec.entity.SysUser;
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

//        SysUser user = sysUserService.getUserByUsernameAndPassword(request.getUsername(), request.getPassword());
//        if (user == null) {
//            return ResponseEntity.status(403).body("Invalid credentials");
//        }
//
//        String token = JwtUtil.generateToken(request.getUsername());
//        return ResponseEntity.ok(token);

        Map map = new HashMap();
        map.put("user",request.getUsername());
        map.put("password",request.getPassword());
        return JsonData.buildSuccess(jwtTokenUtils.createToken(map));

    }
}
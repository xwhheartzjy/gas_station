package org.codec.controller;
import org.codec.entity.SysUser;
import org.codec.model.LoginRequest;
import org.codec.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.codec.util.JwtUtil;

@RestController
@RequestMapping
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        SysUser user = sysUserService.getUserByUsernameAndPassword(request.getUsername(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(403).body("Invalid credentials");
        }

        String token = JwtUtil.generateToken(request.getUsername());
        return ResponseEntity.ok(token);

    }
}
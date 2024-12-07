package org.codec.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.codec.entity.SysUser;
import org.codec.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {
    @Autowired
    private SysUserMapper sysUserMapper;

    public SysUser getUserByUsernameAndPassword(String username, String password) {
        String gensalt = BCrypt.gensalt();
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", username);
        queryWrapper.eq("password", BCrypt.hashpw(password,gensalt));
        queryWrapper.eq("del_flag",0);
        return sysUserMapper.selectOne(queryWrapper);
    }
}
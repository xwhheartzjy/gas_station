package org.codec.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.codec.entity.SysUser;
import org.codec.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.dev33.satoken.secure.BCrypt;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {
    @Autowired
    private SysUserMapper sysUserMapper;

    public SysUser getUserByUsernameAndPassword(String username, String password) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", username);
        queryWrapper.eq("del_flag",0);
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        if (BCrypt.checkpw(password,sysUser.getPassword())) {
            return sysUser;
        }
        return null;

    }
}
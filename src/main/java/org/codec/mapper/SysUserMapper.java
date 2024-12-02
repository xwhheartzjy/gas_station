package org.codec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.codec.entity.SysUser;
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
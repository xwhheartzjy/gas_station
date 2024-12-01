package org.codec.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.codec.dto.GasAreaDTO;
import org.codec.entity.GasArea;
import org.codec.mapper.GasAreaMapper;
import org.codec.mapper.map_struct.GasAreaMapStructMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GasAreaService extends ServiceImpl<GasAreaMapper, GasArea> {

    @Autowired
    private GasAreaMapper gasAreaMapper;


    public List<GasAreaDTO> getGasAreaListByLevel(String level, Long parentId) {
        QueryWrapper<GasArea> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("level", level);
        queryWrapper.eq("del_flag", 0);
        if (parentId != null && parentId > 0) {
            queryWrapper.eq("parent_id", parentId);
        }
        List<GasArea> gasAreas = gasAreaMapper.selectList(queryWrapper);
        List<GasAreaDTO> result = new ArrayList<>();
        for (GasArea gasArea : gasAreas) {
            result.add(GasAreaMapStructMapper.INSTANCE.toDTO(gasArea));
        }
        return result;
    }
}

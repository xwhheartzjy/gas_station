package org.codec.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.codec.entity.GasArea;
@DS("gas_station")
public interface GasAreaMapper extends BaseMapper<GasArea> {
}

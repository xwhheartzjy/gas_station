package org.codec.mapper.map_struct;

import org.codec.dto.GasAreaDTO;
import org.codec.dto.GasStationDTO;
import org.codec.entity.GasArea;
import org.codec.entity.GasStationConsumer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GasAreaMapStructMapper {

    GasAreaMapStructMapper INSTANCE = Mappers.getMapper(GasAreaMapStructMapper.class);
    @Mapping(source = "areaId", target = "area_id")
    @Mapping(source = "areaName", target = "area_name")
    GasAreaDTO toDTO(GasArea gasArea);
}

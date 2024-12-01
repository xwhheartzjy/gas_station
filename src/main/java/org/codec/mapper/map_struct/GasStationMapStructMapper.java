package org.codec.mapper.map_struct;

import org.codec.dto.GasStationDTO;
import org.codec.entity.GasStationConsumer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
@Mapper(componentModel = "spring")
public interface GasStationMapStructMapper {

    GasStationMapStructMapper INSTANCE = Mappers.getMapper(GasStationMapStructMapper.class);
    @Mapping(source = "stationId", target = "station_id")
    GasStationDTO toDTO(GasStationConsumer gasStationConsumer);
}

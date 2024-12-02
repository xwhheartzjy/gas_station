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
    @Mapping(source = "businessStartTime", target = "business_start_time")
    @Mapping(source = "businessEndTime", target = "business_end_time")
    @Mapping(source = "stationType", target = "station_type")
    @Mapping(source = "carWash", target = "car_wash")
    GasStationDTO toDTO(GasStationConsumer gasStationConsumer);
}

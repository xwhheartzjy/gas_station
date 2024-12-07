package org.codec.dto;

import lombok.Data;

import java.util.List;

@Data
public class GasPriceDTO {
    private String gasStationName;
    private String gasStationId;
    private String normalStationId;
    private String gasLocation;
    private boolean isFollow;
    private double distance;
    private List<GasInfoDTO> gasStationNearbyPrice;
    private String gasStationType;

    private String lat;
    private String lng;
}

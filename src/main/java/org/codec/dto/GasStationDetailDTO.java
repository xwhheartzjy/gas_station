package org.codec.dto;

import lombok.Data;

import java.util.List;

@Data
public class GasStationDetailDTO {
    private String normalGasStationName;
    private String gasStationName;
    private Double distance;
    private List<PlatformPriceDTO> platformPriceList;
    private ChartDTO chart;

}

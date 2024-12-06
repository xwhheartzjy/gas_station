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
    private String oil0PriceAvg;
    private String oil92PriceAvg;
    private String oil95PriceAvg;
    private String oil98PriceAvg;
    private String livingCount;
    private String officeCount;
    private String businessCount;
    private String housePrice;
    private String rentPrice;
    private String totalResidents;

}

package org.codec.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChartData {
    private List<String> xAxis;
    private List<ChartValue> value;

}
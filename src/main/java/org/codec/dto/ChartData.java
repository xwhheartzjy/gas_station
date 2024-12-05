package org.codec.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChartData {
    private List<Integer> xAxis;
    private List<ChartValue> value;

}
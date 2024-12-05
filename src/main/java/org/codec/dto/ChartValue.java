package org.codec.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChartValue {
    private String name;
    private List<Integer> data;

}
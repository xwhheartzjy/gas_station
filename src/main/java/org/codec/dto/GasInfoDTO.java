package org.codec.dto;

import lombok.Data;

@Data
public class GasInfoDTO {

    private int gasType;  //油品类型，例如：0表示油品类型0，92表示油品类型92
    private String gasPrice;  //油品价格
}

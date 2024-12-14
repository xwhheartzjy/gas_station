package org.codec.dto;

import lombok.Data;

/**
 * DTO class for gas station pricing minimum values.
 */
@Data
public class GasDidiPricingDailyDTO {

    /** 油站ID */
    private Long oilStationId;

    /** 定价日期 */
    private String pricingDate;

    /** 0号柴油的最低价格 */
    private Integer minOil0Price;

    /** 92号汽油的最低价格 */
    private Integer minOil92Price;
    /** 95号汽油的最低价格 */
    private Integer minOil95Price;
    /** 98号汽油的最低价格 */
    private Integer minOil98Price;
}

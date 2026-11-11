package kz.kimep.ticketing.catalog.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SectionPriceResponse {
    private Long sectionId;
    private String sectionName;
    private String priceTier;
    private BigDecimal price;
    private String currency;
}
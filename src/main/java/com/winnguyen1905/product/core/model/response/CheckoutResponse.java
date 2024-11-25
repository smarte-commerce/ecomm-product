package com.winnguyen1905.product.core.model.response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.product.core.model.AbstractModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutResponse extends AbstractModel {
    private PriceStatisticsResponse priceStatistics;
    private List<CheckoutItemReponse> checkoutItems = new ArrayList<>();

    @Getter
    @Setter
    public static class CheckoutItemReponse extends AbstractModel {
        private UUID cartId;
        private PriceStatisticsResponse priceStatistics;
    }
}

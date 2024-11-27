package com.winnguyen1905.product.util;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.winnguyen1905.product.exception.BadRequestException;
import com.winnguyen1905.product.persistance.entity.EDiscount;
import com.winnguyen1905.product.persistance.entity.EUserDiscount;
 
public class DiscountUtils {
    public static Boolean isUsable(EDiscount eDiscount) {
        // long timeUsed = eDiscount.get;
        
        // if(eDiscount.getMaxUsesPerUser() <= timeUsed) throw new BadRequestException("Use count reach to maximum");
        if(eDiscount.getEndDate().isBefore(Instant.now())) throw new BadRequestException("Discount has been expired");
        if(eDiscount.getUsesCount() >= eDiscount.getMaxUses()) throw new BadRequestException("Discount be used maximum");
        return true;
    }

    public static Double totalPriceOfAllProducInDiscountProgramFromCart(EDiscount eDiscount) {
        // cart.getCartItems().stream();
        // return cart.getCartItems().stream()
        //     .filter(item -> item.getIsSelected())
        //     .collect(Collectors.summingDouble(
        //         cartItem ->
        //             eDiscount.getProducts().contains(cartItem.getProductVariation().getProduct()) || eDiscount.getAppliesTo().equals(ApplyDiscountType.ALL) 
        //                 ? cartItem.getQuantity() * cartItem.getProductVariation().getPrice() 
        //                 : 0.0)
        //     );
        return null;
    }
}

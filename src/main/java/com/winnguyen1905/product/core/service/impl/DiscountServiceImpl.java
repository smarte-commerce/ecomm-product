package com.winnguyen1905.product.core.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.product.common.ApplyDiscountStatus;
import com.winnguyen1905.product.common.ApplyDiscountType;
import com.winnguyen1905.product.core.model.Discount;
import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.product.core.model.response.PriceStatisticsResponse;
import com.winnguyen1905.product.core.service.DiscountService;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EDiscount;
import com.winnguyen1905.product.persistance.entity.EProduct;
import com.winnguyen1905.product.persistance.entity.EUserDiscount;
import com.winnguyen1905.product.persistance.repository.DiscountRepository;
import com.winnguyen1905.product.persistance.repository.ProductRepository;
import com.winnguyen1905.product.persistance.repository.UserDiscountRepository;
import com.winnguyen1905.product.util.DiscountUtils;
import com.winnguyen1905.product.util.OptionalExtractor;
import com.winnguyen1905.product.util.PaginationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

  private final ModelMapper modelMapper;
  private final ProductRepository productRepository;
  private final DiscountRepository discountRepository;
  private final UserDiscountRepository userDiscountRepository;

  @Override
  public Discount handleCreateDiscountCode(Discount discount, UUID shopId) {
    List<UUID> productIds = Optional.ofNullable(discount.getProducts())
        .map(products -> products.stream().map(item -> item.getId()).collect(Collectors.toList()))
        .orElse(new ArrayList<>());

    discount.setProducts(null);
    EDiscount eDiscount = this.modelMapper.map(discount, EDiscount.class);

    if (discount.getAppliesTo().equals(ApplyDiscountType.SPECIFIC)) {
      List<EProduct> eProducts = this.productRepository.findByIdInAndShopId(productIds, shopId);
      if (eProducts.size() != productIds.size()) {
        throw new ResourceNotFoundException("Just found " + eProducts.size() + " / " + productIds.size() + " product");
      }
      for (EProduct eProduct : eProducts) {
        eProduct.getDiscounts().add(eDiscount);
        eDiscount.getProducts().add(eProduct);
      }
    }

    eDiscount.setShopId(shopId);
    eDiscount = this.discountRepository.save(eDiscount);
    return this.modelMapper.map(discount, Discount.class);
  }

  @Override
  public Discount handleGetDiscount(UUID id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleGetDiscount'");
  }

  @Override
  public Discount handleGetAllDiscountCodesByShop(UUID shopId, Pageable pageable) {
    Page<EDiscount> discount = this.discountRepository.findAllByShopIdAndIsActiveTrue(shopId, pageable);
    discount.getContent().forEach(item -> item.setProducts(null));
    return this.modelMapper.map(discount, Discount.class);
  }

  @Override
  public Boolean handleVerifyDiscountCode(UUID id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleVerifyDiscountCode'");
  }

  @Override
  public void handleDeleteDiscountCode(UUID id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleDeleteDiscountCode'");
  }

  @Override
  public void handleCancelDiscountCode(UUID id, String username) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleCancelDiscountCode'");
  }

  @Override
  public Discount handleGetAllProductsRelateDiscountCode(Discount discount, Pageable pageable) {
    EDiscount eDiscount = this.discountRepository.findByIdAndIsActiveTrue(discount.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Not found discount id "));

    Product product;

    if (discount.getAppliesTo().equals(ApplyDiscountType.ALL)) {
      Page<EProduct> productPages = this.productRepository.findAllByShopIdAndIsPublishedTrue(eDiscount.getShopId(),
          pageable);
      product = this.modelMapper.map(productPages, Product.class);
    } else {
      Page<EProduct> productPages = PaginationUtils
          .createPaginationFromDataListAndPageable(List.copyOf(eDiscount.getProducts()), pageable);
      product = this.modelMapper.map(productPages, Product.class);
    }

    eDiscount.setProducts(null);
    discount = this.modelMapper.map(discount, Discount.class);
    discount.setProductList(product);

    return discount;
  }

  @Override
  @Transactional
  public PriceStatisticsResponse handleApplyDiscountCodeForCart(UUID customerId, ApplyDiscountRequest applyDiscountRequest, ApplyDiscountStatus applyDiscountStatus) {
    // EDiscount eDiscount = OptionalExtractor.extractFromResource(this.discountRepository.findByIdAndIsActiveTrue(applyDiscountRequest.getDiscountId()));
    // EUserDiscount eUserDiscount = OptionalExtractor.extractFromResource(this.userDiscountRepository.findByUserIdAndDiscountId(customerId, discount.getId()));
    
    // Boolean isUsable = DiscountUtils.isUsable(eDiscount, eUserDiscount);
    // if (!isUsable) throw new ResourceNotFoundException("Cannot use discount, some error happen");
    
    // CartEntity cart = this.cartRepository.findById(applyDiscountRequest.getCartId())
    //     .orElseThrow(() -> new ResourceNotFoundException("Not found cart"));
    // if (!cart.getCustomer().getId().equals(customerId)) {
    //   throw new ResourceNotFoundException("Cannot use discount, some error happen");
    // }

    // // Get total price of all product in the discount program
    // Double totalPriceOfAllProducVariationtInDiscountProgram = DiscountUtils
    //     .totalPriceOfAllProducInDiscountProgramFromCart(discount, cart);
    // Double totalPriceOfAllProduct = CartUtils.getPriceOfAllProductSelectedInCart(cart);

    // // Reach to the Lowest price possible to be discount
    // if (discount.getMinOrderValue() > totalPriceOfAllProducVariationtInDiscountProgram) {
    //   throw new ResourceNotFoundException("The total price of product selected in this cart insufficient");
    // }

    // // Update data before sending response
    // if (applyDiscountStatus.equals(ApplyDiscountStatus.COMMIT)) {
    //   discount.getCustomer().add(customer);
    //   discount.setUsesCount(discount.getUsesCount() + 1);
    //   this.discountRepository.save(discount);
    // }

    // // Get the final total price
    // Double finalPrice = discount.getDiscountType().equals(DiscountType.FIXED_AMOUNT) ? discount.getValue()
    //     : totalPriceOfAllProduct - totalPriceOfAllProducVariationtInDiscountProgram * discount.getValue() / 100;

    // return PriceStatisticsResponse.builder().finalPrice(finalPrice).totalPrice(totalPriceOfAllProduct)
    //     .discountId(applyDiscountRequest.getDiscountId()).amountProductReduced(totalPriceOfAllProduct - finalPrice)
    //     .build();
    return null;
  }

  @Override
  public void handleCancelDiscountForCart(Discount discount, UUID customerId) {
    EDiscount eDiscount = OptionalExtractor.extractFromResource(this.discountRepository.findByIdAndIsActiveTrue(discount.getId()));
    // We can add relationship between CART and DISCOUNT for easy to analyze when
    // toggle cart page

    // CartEntity cart =
    // this.cartRepository.findByShopIdAndCustomerId(discount.getShop().getId(),
    // customer.getId()).orElseThrow(() -> new ResourceNotFoundException("Not found
    // cart"));

    // Boolean res = discount.getCustomer().remove(customer);
    // if (!res) {
    //   throw new ResourceNotFoundException("Cancel discount for cart failed");
    // }
    // discount.setUsesCount(discount.getUsesCount() - 1);

    // discountRepository.save(discount);
  }

}

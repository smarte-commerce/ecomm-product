package com.winnguyen1905.product.core.controller;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.product.common.SystemConstant;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.core.model.request.SearchProductRequest;
import com.winnguyen1905.product.core.model.response.ProductDetail;
import com.winnguyen1905.product.core.service.vendor.VendorProductService;
import com.winnguyen1905.product.util.MetaMessage;
import com.winnguyen1905.product.util.ExtractorUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("products")
public class CartController {
  private final VendorProductService vendorProductService;
}

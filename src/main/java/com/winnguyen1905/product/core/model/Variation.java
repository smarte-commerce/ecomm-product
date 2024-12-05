package com.winnguyen1905.product.core.model;

import java.util.List; 

public record Variation(String detail, List<Inventory> inventories) {}

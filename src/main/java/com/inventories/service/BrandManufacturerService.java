package com.inventories.service;

import com.inventories.model.ProductEntity;

import java.util.List;

public interface BrandManufacturerService {
    List<ProductEntity> getAllBrandManufacturer();
    ProductEntity addBrandManufacturer(ProductEntity brandManufacturer);
}

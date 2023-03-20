package com.inventories.service;

import com.inventories.model.OrderEntity;
import org.springframework.data.domain.Page;

public interface BrandService {
    OrderEntity findAllByBrandNameIsLike(String name);
    Page<OrderEntity> getAllByBrandName(int page, int size, String sort);
    OrderEntity addBrand(OrderEntity brand);
    OrderEntity findById(int id);
    OrderEntity updateBrand(OrderEntity orderEntity);
}

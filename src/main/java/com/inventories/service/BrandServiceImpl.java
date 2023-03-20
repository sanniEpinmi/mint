package com.inventories.service;

import com.inventories.model.OrderEntity;
import com.inventories.repo.BrandRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("BrandService")
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandRepo brandRepo;

    public OrderEntity findAllByBrandNameIsLike(String name) {
        return brandRepo.findAllByBrandNameIsLike(name);
    }

    public Page<OrderEntity> getAllByBrandName(int page, int size, String sort){
        String[] sortSplit = sort.split(",");
        return brandRepo.findAll(new PageRequest(page, size, (sortSplit[1].toUpperCase().equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC), sortSplit[0]));
    }

    public OrderEntity findById(int id){
        return brandRepo.findById(id);
    }

    @Transactional
    public OrderEntity addBrand(OrderEntity brand) {
        brand.setId(brandRepo.getNextSeriesId().intValue());
        return brandRepo.save(brand);
    }

    @Transactional
    public OrderEntity updateBrand(OrderEntity brand) {
        return brandRepo.save(brand);
    }
}

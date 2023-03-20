package com.inventories.repo;

import com.inventories.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;

public interface BrandRepo extends JpaRepository<OrderEntity, Integer> {
    OrderEntity findAllByBrandNameIsLike(String name);
    OrderEntity getAllByBrandName(Pageable pageable);
    OrderEntity findById(int id);

    @Query(value = "SELECT nextval('brand_id_seq')", nativeQuery = true)
    Long getNextSeriesId();
}

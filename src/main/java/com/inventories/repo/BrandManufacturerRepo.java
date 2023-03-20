package com.inventories.repo;

import com.inventories.model.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/api/brand/manufacturer")
public interface BrandManufacturerRepo extends JpaRepository<ProductEntity, Integer> {
    @Query(value = "SELECT nextval('brand_manufacturer_id_seq')", nativeQuery = true)
    Long getNextSeriesId();
}

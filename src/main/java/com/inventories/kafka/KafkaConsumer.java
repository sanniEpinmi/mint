package com.inventories.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventories.model.*;
import com.inventories.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import javax.transaction.Transactional;

@Component
@Transactional
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    BrandService brandService;

    @Autowired
    BrandManufacturerService brandManufacturerService;

    private OrderEntity orderEntityFromKafka = new OrderEntity();

    @KafkaListener(topics = "4igc0qsg-inventories.kafka.post.brand", groupId = "inventories")
    public void processPostBrand(String brandJSON){
        logger.info("received content = '{}'", brandJSON);
        try{
            ObjectMapper mapper = new ObjectMapper();
            OrderEntity orderEntity = mapper.readValue(brandJSON, OrderEntity.class);
            OrderEntity brand = brandService.addBrand(orderEntity);
            logger.info("Success process brand '{}' with topic '{}'", brand.getProductName(), "inventories.kafka.post.brand");
        } catch (Exception e){
            logger.error("An error occurred! '{}'", e.getMessage());
        }
    }

    @KafkaListener(topics = "4igc0qsg-inventories.kafka.put.brand" , groupId = "inventories")
    public void processPutBrand(String brandJSON){
        logger.info("received content = '{}'", brandJSON);
        try{
            ObjectMapper mapper = new ObjectMapper();
            OrderEntity brand = mapper.readValue(brandJSON, OrderEntity.class);
            orderEntityFromKafka = brand;
            logger.info("Success process brand '{}' with topic '{}'", brand.getProductName(), "inventories.kafka.put.brand");
        } catch (Exception e){
            logger.error("An error occurred! '{}'", e.getMessage());
        }
    }

    public OrderEntity getBrandEntityFromKafka(int id){
        return orderEntityFromKafka;
    }


    @KafkaListener(topics = "4igc0qsg-inventories.kafka.patch.brand", groupId = "inventories")
    public void processPatchBrand(String brandJSON){
        logger.info("received content = '{}'", brandJSON);
        try{
            ObjectMapper mapper = new ObjectMapper();
            OrderEntity orderEntity = mapper.readValue(brandJSON, OrderEntity.class);
            OrderEntity brand = brandService.updateBrand(orderEntity);
            logger.info("Success process brand '{}' with topic '{}'", brand.getProductName(), "inventories.kafka.patch.brand");
        } catch (Exception e){
            logger.error("An error occurred! '{}'", e.getMessage());
        }
    }

    @KafkaListener(topics = "4igc0qsg-inventories.kafka.post.brand.manufacturer", groupId = "inventories")
    public void processPostBrandManufacturer(String brandManufacturerJSON){
        logger.info("received content = '{}'", brandManufacturerJSON);
        try{
            ObjectMapper mapper = new ObjectMapper();
            ProductEntity productEntity = mapper.readValue(brandManufacturerJSON, ProductEntity.class);
            ProductEntity brandManufacturer = brandManufacturerService.addBrandManufacturer(productEntity);
            logger.info("Success process brand manufacturer '{}' with topic '{}'", brandManufacturer.getProductName(), "inventories.kafka.post.brand");
        } catch (Exception e){
            logger.error("An error occurred! '{}'", e.getMessage());
        }
    }
}
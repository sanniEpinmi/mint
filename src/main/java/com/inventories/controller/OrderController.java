package com.inventories.controller;

import com.inventories.kafka.KafkaConsumer;
import com.inventories.kafka.KafkaProducer;
import com.inventories.model.OrderEntity;
import com.inventories.util.ArrayListCustomMessage;
import com.inventories.resource.MultiResource;
import com.inventories.model.CustomMessage;
import com.inventories.service.BrandService;
import com.inventories.util.CustomErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/api/brand")
public class OrderController {
    public static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    BrandService brandService;

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    KafkaConsumer kafkaConsumer;

    @Value("${spring.kafka.consumer.group-id}")
    String kafkaGroupId;

    @Value("${inventories.kafka.post.brand}")
    String postBrandTopic;

    @Value("${inventories.kafka.put.brand}")
    String putBrandTopic;

    @Value("${inventories.kafka.patch.brand}")
    String patchBrandTopic;


    @GetMapping(value="")
    public ResponseEntity<?> getAllByBrandName(@RequestParam("page") int page, @RequestParam("size") int size, @RequestParam(value = "sort", defaultValue = "brandName,asc") String sort, PagedResourcesAssembler pagedResourcesAssembler, @RequestHeader("User-Agent") String userAgent){
        logger.info("Fetching all brands");
        Page<OrderEntity> brand = null;
        try {
            brand = brandService.getAllByBrandName(page, size, sort);
        } catch (Exception e){
            logger.error("An error occurred! {}", e.getMessage());
            CustomErrorType.returnResponsEntityError(e.getMessage());
        }
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.put(HttpHeaders.USER_AGENT, Arrays.asList(userAgent));
        PagedResources<MultiResource> pagedResources = pagedResourcesAssembler.toResource(brand);
        return new ResponseEntity<PagedResources>(pagedResources, headers, HttpStatus.OK);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<?> getBrandById(@PathVariable("id") int id){
        logger.info("Fetching brand with ID {}", id);
        OrderEntity brand = null;
        try{
            brand = kafkaConsumer.getBrandEntityFromKafka(id);
            if (brand.getId() == 0) brand = brandService.findById(id);
        } catch (Exception e){
            logger.error("An error occurred! {}", e.getMessage());
            CustomErrorType.returnResponsEntityError(e.getMessage());
        }
        return new ResponseEntity<OrderEntity>(brand, HttpStatus.OK);
    }

    @PostMapping(value = "", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> addBrand(@RequestBody OrderEntity orderEntity){
        logger.info(("Process add new brand"));
        Resources<CustomMessage> res = null;
        try {
            brandService.addBrand(orderEntity);
            List<CustomMessage> customMessageList = ArrayListCustomMessage.setMessage("Created new brand", HttpStatus.CREATED);
            res = new Resources<>(customMessageList);
            res.add(linkTo(OrderController.class).withSelfRel());
            res.add(linkTo(ProductController.class).withRel("brand_manufacturer"));
        } catch (Exception e){
            logger.error("An error occurred! {}", e.getMessage());
            CustomErrorType.returnResponsEntityError(e.getMessage());
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping(value = "/createProduct", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> addLayeredBrand(@RequestBody OrderEntity orderEntity){
        logger.info((" add new brand by consuming"));
        Resources<CustomMessage> res = null;
        try {
            kafkaProducer.postBrand(postBrandTopic, kafkaGroupId, orderEntity);
            List<CustomMessage> customMessageList = ArrayListCustomMessage.setMessage("Created new brand layered", HttpStatus.ACCEPTED);
            res = new Resources<>(customMessageList);
            res.add(linkTo(OrderController.class).withSelfRel());
            res.add(linkTo(ProductController.class).withRel("brand_manufacturer"));
        } catch (Exception e){
            logger.error("An error occurred! {}", e.getMessage());
            CustomErrorType.returnResponsEntityError(e.getMessage());
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private ResponseEntity<Resources> putAndPatch(OrderEntity orderEntity, int id, int mode){
        logger.info("Process '{}' brand", (mode == 0 ? "put" : "patch"));
        Resources<CustomMessage> res = null;
        try {
            List<CustomMessage> customMessageList = null;
            OrderEntity brand = brandService.findById(id);
            if (brand != null) {
                customMessageList = ArrayListCustomMessage.setMessage((mode == 0 ? "Put" : "Patch" ) + " brand process", HttpStatus.OK);
                orderEntity.setId(id);
                if (mode != 0) orderEntity.setProductCode(brand.getProductCode());
                kafkaProducer.postBrand((mode == 0 ? putBrandTopic : patchBrandTopic), kafkaGroupId, orderEntity);
            } else {
                customMessageList = ArrayListCustomMessage.setMessage("Brand Id" + id + " Not Found!", HttpStatus.BAD_REQUEST);
                res = new Resources<>(customMessageList);
                res.add(linkTo(OrderController.class).withSelfRel());
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
            res = new Resources<>(customMessageList);
            res.add(linkTo(OrderController.class).slash(id).withSelfRel());
            res.add(linkTo(ProductController.class).withRel("brand_manufacturer"));
        } catch (Exception e) {
            logger.error("An error occurred! {}", e.getMessage());
            CustomErrorType.returnResponsEntityError(e.getMessage());
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> putBrand(@PathVariable("id") int id, @RequestBody OrderEntity orderEntity){
        return putAndPatch(orderEntity, id, 0);
    }

    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> updateBrand(@PathVariable("id") int id, @RequestBody OrderEntity orderEntity){
        return putAndPatch(orderEntity, id, 1);
    }
}

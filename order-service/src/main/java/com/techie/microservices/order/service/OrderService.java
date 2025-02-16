package com.techie.microservices.order.service;

import com.techie.microservices.order.client.InventoryClient;
import com.techie.microservices.order.dto.OrderRequest;
import com.techie.microservices.order.event.OrderPlaceEvent;
import com.techie.microservices.order.model.Order;
import com.techie.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    private final KafkaTemplate<String,OrderPlaceEvent> kafkaTemplate;

    public void placeOrder(OrderRequest orderRequest) {
            boolean isProductInStock  = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

            if(!isProductInStock) {
                log.error("Product with skuCode {} is out of stock", orderRequest.skuCode());
                throw new RuntimeException("Product with skuCode " + orderRequest.skuCode() + " is out of stock");
            }
            else{
                Order order = new Order();
                order.setOrderNumber(UUID.randomUUID().toString());
                order.setPrice(orderRequest.price().multiply(BigDecimal.valueOf(orderRequest.quantity())));
                order.setSkuCode(orderRequest.skuCode());
                order.setQuantity(orderRequest.quantity());
                orderRepository.save(order);

                //send a messege to kafka topic
                // orderNumber, Email

                OrderPlaceEvent orderPlaceEvent = new OrderPlaceEvent(order.getOrderNumber(), orderRequest.userDetails().email());
                log.info("Start - sending order placed event to kafka topic");
                kafkaTemplate.send("order-placed-topic", orderPlaceEvent);
                String traceId = MDC.get("traceId");
                log.info("Received message: {}, traceId: {}", traceId);
                log.info("End - sending order placed event to kafka topic");

            }


    }
}

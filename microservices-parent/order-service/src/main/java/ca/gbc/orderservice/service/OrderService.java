package ca.gbc.orderservice.service;


import ca.gbc.orderservice.dto.OrderRequest;

public interface OrderService {

    void placeOrder(OrderRequest orderRequest);

}


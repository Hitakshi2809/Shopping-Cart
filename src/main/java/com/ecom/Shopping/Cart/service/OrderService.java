package com.ecom.Shopping.Cart.service;

import com.ecom.Shopping.Cart.model.OrderRequest;
import com.ecom.Shopping.Cart.model.ProductOrder;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    public void saveOrder(Integer userId, OrderRequest orderRequest);

    public List<ProductOrder> getOrdersByUser(Integer userId);

    public Boolean updateOrderStatus(Integer id,String status);

    public List<ProductOrder> getAllOrders();
    public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize);

    public ProductOrder getOrderByOrderId(String orderId);
}

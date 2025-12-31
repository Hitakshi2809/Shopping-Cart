package com.ecom.Shopping.Cart.service.impl;

import com.ecom.Shopping.Cart.model.Cart;
import com.ecom.Shopping.Cart.model.OrderAddress;
import com.ecom.Shopping.Cart.model.OrderRequest;
import com.ecom.Shopping.Cart.model.ProductOrder;
import com.ecom.Shopping.Cart.repository.CartRepository;
import com.ecom.Shopping.Cart.repository.ProductOrderRepository;
import com.ecom.Shopping.Cart.service.OrderService;
import com.ecom.Shopping.Cart.util.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImple implements OrderService {
    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private CartRepository cartRepository;
    @Transactional
    public void saveOrder(Integer userId, OrderRequest orderRequest){

        List<Cart> carts = cartRepository.findByUserId(userId);


        for( Cart cart:carts){

            ProductOrder order =  new ProductOrder();
            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderDate(LocalDate.now());
            order.setProduct(cart.getProduct());
            order.setPrice(cart.getProduct().getDiscountPrice());
            order.setQuantity(cart.getQuantity());
            order.setUser(cart.getUser());
            order.setStatus(OrderStatus.IN_PROGRESS.getName());
            order.setPaymentType(orderRequest.getPaymentType());

            OrderAddress address = new OrderAddress();
            address.setFirstName(orderRequest.getFirstName());
            address.setLastName(orderRequest.getLastName());
            address.setEmail(orderRequest.getEmail());
            address.setMobileNo(orderRequest.getMobileNo());
            address.setAddress(orderRequest.getAddress());
            address.setCity(orderRequest.getCity());
            address.setState(orderRequest.getState());
            address.setPincode(orderRequest.getPincode());


            order.setOrderAddress(address);


            productOrderRepository.save(order);

        }


    }

    /**
     * @param userId
     * @return
     */
    @Override
    public List<ProductOrder> getOrdersByUser(Integer userId) {
        List<ProductOrder> orders = productOrderRepository.findByUserId(userId);
        return orders;
    }

    /**
     * @param id
     * @param status
     * @return
     */
    @Override
    public Boolean updateOrderStatus(Integer id, String status) {
        Optional<ProductOrder>findById = productOrderRepository.findById(id);
        if(findById.isPresent()){
           ProductOrder productOrder=findById.get();
           productOrder.setStatus((status));
           productOrderRepository.save(productOrder);
           return true;
       }
        return false;
    }

    /**
     * @return
     */
    @Override
    public List<ProductOrder> getAllOrders() {
        return  productOrderRepository.findAll();
    }

    /**
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo,pageSize);

        return productOrderRepository.findAll(pageable);
    }

    /**
     * @param orderId
     * @return
     */
    @Override
    public ProductOrder getOrderByOrderId(String orderId) {
      return  productOrderRepository.findByOrderId(orderId);
    }
}

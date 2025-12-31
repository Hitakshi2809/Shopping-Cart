package com.ecom.Shopping.Cart.repository;

import com.ecom.Shopping.Cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart,Integer> {

    public Cart findByProductIdAndUserId(Integer productId,Integer userId);


    public int countByUserId(Integer userId);

    public List<Cart> findByUserId(Integer userId);
}

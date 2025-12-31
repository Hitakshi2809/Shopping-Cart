package com.ecom.Shopping.Cart.service;

import com.ecom.Shopping.Cart.model.Cart;

import java.util.List;

public interface CartService {

    public Cart saveCart(Integer productId,Integer userId);

    public List<Cart> getCartsByUser(Integer userId);

    public Integer getCountCart(Integer userId);

    void updateQuantity(String sy, Integer cid);
}

package com.ecom.Shopping.Cart.service.impl;

import com.ecom.Shopping.Cart.model.Cart;
import com.ecom.Shopping.Cart.model.Product;
import com.ecom.Shopping.Cart.model.UserDtls;
import com.ecom.Shopping.Cart.repository.CartRepository;
import com.ecom.Shopping.Cart.repository.ProductRepository;
import com.ecom.Shopping.Cart.repository.UserRepository;
import com.ecom.Shopping.Cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;
    /**
     * @param productId
     * @param userId
     * @return
     */
    @Override
    public Cart saveCart(Integer productId, Integer userId) {

        UserDtls userDtls = userRepository.findById(userId).get();
        Product product = productRepository.findById(productId).get();


        Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);

        Cart cart =null;

        if(ObjectUtils.isEmpty(cartStatus)){
            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(userDtls);
            cart.setQuantity(1);
            cart.setTotalPrice(1*product.getDiscountPrice());


        }
        else{
              cart = cartStatus;
                cart.setQuantity(cart.getQuantity()+1);
                cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());


            }
        Cart saveCart = cartRepository.save(cart);
        return saveCart;

        }

    /**
     * @param userId
     * @return
     */
    @Override
    public List<Cart> getCartsByUser(Integer userId) {
        List<Cart> cart = cartRepository.findByUserId(userId);

        Double totalOrderPrice = 0.0;
        List<Cart> updateCart = new ArrayList<>();
        for( Cart c : cart){
            Double totalPrice = (c.getProduct().getDiscountPrice()*c.getQuantity());
            c.setTotalPrice(totalPrice);

            totalOrderPrice += totalPrice;
            c.setTotalOrderAmount(totalOrderPrice);
            updateCart.add(c);

        }




        return updateCart;
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public Integer getCountCart(Integer userId) {

        int countByUserId = cartRepository.countByUserId(userId);
        return countByUserId;
    }

    /**
     * @param sy
     * @param cid
     * @return
     */
    @Override
    public void updateQuantity(String sy, Integer cid) {

        Cart cart = cartRepository.findById(cid).get();
        Integer updateQuantity;
        if(sy.equalsIgnoreCase("de")){
             updateQuantity=   cart.getQuantity()-1;
          if(updateQuantity<=0){
              cartRepository.delete(cart);
              return ;

          }
          else{
              cart.setQuantity(updateQuantity);
              cartRepository.save(cart);
          }


        }
        else{
           updateQuantity=   cart.getQuantity()+1;

        }
        cart.setQuantity(updateQuantity);
        Cart update = cartRepository.save(cart);

    }


}

    /**
     * @param userId
     * @return
     */
//    @Override
//    public List<Cart> getCartsByUser(Integer userId) {
//        return List.of();
//    }

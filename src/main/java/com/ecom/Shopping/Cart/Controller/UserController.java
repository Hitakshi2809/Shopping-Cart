package com.ecom.Shopping.Cart.Controller;


import com.ecom.Shopping.Cart.model.*;
import com.ecom.Shopping.Cart.service.CartService;
import com.ecom.Shopping.Cart.service.CategoryService;
import com.ecom.Shopping.Cart.service.OrderService;
import com.ecom.Shopping.Cart.service.UserService;
import com.ecom.Shopping.Cart.util.OrderStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/")
    public  String home(){
        return "user/home";
    }
    @ModelAttribute
    public void getUserDetails(Principal p, Model m){
        if(p!=null){
            String email = p.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            m.addAttribute("user", userDtls);

            Integer countCart = cartService.getCountCart(userDtls.getId());
            m.addAttribute("countCart",countCart);
        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        m.addAttribute("categorys",allActiveCategory);
    }

      @GetMapping("/addToCart")
    public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session){

         Cart saveCart= cartService.saveCart(pid,uid);
          if(ObjectUtils.isEmpty(saveCart)){
              session.setAttribute("errorMsg", "Product add to cart failed");
          }
          else{
              session.setAttribute("succMsg", "Product add to cart successfully");
          }



        return "redirect:/product/"+pid;
    }
    @GetMapping("/cart")
    public String loadCartPage(Principal p,Model m) {

        UserDtls userDtls =  getLoggedInUserDetails( p);
        List<Cart> carts = cartService.getCartsByUser(userDtls.getId());
        m.addAttribute("carts",carts);

        double totalOrderPrice = carts.stream()
                .mapToDouble(cart -> cart.getProduct().getDiscountPrice() * cart.getQuantity())
                .sum();
        m.addAttribute("totalOrderPrice", totalOrderPrice);


        return "user/cart";
    }
    @GetMapping("/cartQuantityUpdate")
    public String updateCartQuantity(@RequestParam String sy,@RequestParam Integer cid){

        cartService.updateQuantity(sy,cid);
        return "redirect:/user/cart";
    }
    @GetMapping("/orders")
    public String orderPage(Principal p ,Model m){
        UserDtls userDtls =  getLoggedInUserDetails( p);
        List<Cart> carts = cartService.getCartsByUser(userDtls.getId());
        m.addAttribute("carts",carts);

       if(carts.size()>0){
           Double orderPrice = carts.get(carts.size()-1).getTotalOrderAmount();
           Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderAmount()+250+100;
           m.addAttribute("orderPrice", orderPrice);
           m.addAttribute("totalOrderPrice", totalOrderPrice);
       }



        return "/user/order";
    }


    @PostMapping("/save-order")
    public String saveOrder(@ModelAttribute OrderRequest request,Principal p){
        UserDtls user = getLoggedInUserDetails(p);
        orderService.saveOrder(user.getId(),request);


        return "redirect:/user/success";
    }
    @GetMapping("/success")
    public String loadSuccess(){
        return "/user/success";
    }

    @GetMapping("/user-orders")
    public String myOrder(Model m,Principal p ){
        List<ProductOrder> orders= orderService.getOrdersByUser(getLoggedInUserDetails(p).getId());
        m.addAttribute("orders",orders);
        return "/user/my_orders";

    }
    @GetMapping("/update-status")
    public String updateOrderStatus(@RequestParam Integer id,@RequestParam Integer st,HttpSession session){
        OrderStatus[] values = OrderStatus.values();
        String status = null;

        for(OrderStatus orderSt:values){
            if(orderSt.getId().equals(st)){
                status = orderSt.getName();
            }
        }
        Boolean updateOrder = orderService.updateOrderStatus(id, status);
        if(updateOrder){
            session.setAttribute("succMsg","Status Updated");

        }
        else{
            session.setAttribute("errorMsg","Something went wrong");
        }

        return "redirect:/user/user-orders";
    }
    @GetMapping("/profile")
    public String profile(){
        return "/user/profile";
    }
    @PostMapping("/update-profile")
    public  String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img,HttpSession session){
       UserDtls updateUserProfile =userService.updateUserProfile(user,img);
        if(ObjectUtils.isEmpty(updateUserProfile)){
            session.setAttribute("errorMsg","Something went wrong");
        }
        else{
            session.setAttribute("succMsg","Profile is  Updated");
        }
        return "redirect:/user/profile";
    }
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword,@RequestParam String currentPassword,Principal p
    ,HttpSession session){
        UserDtls loggedInUserDetails = getLoggedInUserDetails(p);
        Boolean matches =passwordEncoder.matches(currentPassword,loggedInUserDetails.getPassword());

        if(matches){
            String encodePassword = passwordEncoder.encode(newPassword);
            loggedInUserDetails.setPassword(encodePassword);
            UserDtls updateUser = userService.updateUser(loggedInUserDetails);
            if(ObjectUtils.isEmpty(updateUser)){
                session.setAttribute("errorMsg","Password not updated!! Error in server");
            }
            else{
                session.setAttribute("succMsg","Password  is  Updated successfully");
            }
        }
        else{
            session.setAttribute("errorMsg","Current Password is incorrect");
        }
        return  "redirect:/user/profile";
    }



    private UserDtls getLoggedInUserDetails(Principal p) {

        String email = p.getName();
        UserDtls userDtls = userService.getUserByEmail(email);

        return userDtls;
    }

}

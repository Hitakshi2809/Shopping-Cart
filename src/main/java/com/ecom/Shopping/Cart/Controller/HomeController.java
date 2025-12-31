package com.ecom.Shopping.Cart.Controller;

import com.ecom.Shopping.Cart.model.Category;
import com.ecom.Shopping.Cart.model.Product;
import com.ecom.Shopping.Cart.model.UserDtls;
import com.ecom.Shopping.Cart.repository.ProductRepository;
import com.ecom.Shopping.Cart.service.CartService;
import com.ecom.Shopping.Cart.service.CategoryService;
import com.ecom.Shopping.Cart.service.ProductService;
import com.ecom.Shopping.Cart.service.UserService;
import com.ecom.Shopping.Cart.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {
      @Autowired
    private CategoryService categoryService;


      @Autowired
      private ProductService productService;


     @Autowired
     private UserService userService;


    @Autowired
     private CartService cartService;

    @Autowired
    private  CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

     @ModelAttribute
     public void getUserDetails(Principal p,Model m){
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

    @GetMapping("/")
   public String index(Model m ){

        List<Category> activeCategory= categoryService.getAllActiveCategory().stream()
                .sorted((c1,c2)->c2.getId().compareTo(c1.getId())).limit(6).toList();
        List<Product> activeProducts= productService.getAllActiveProducts(null).stream()
                        .sorted((p1,p2)->p2.getId().compareTo(p1.getId())).

                limit(8).toList();
        m.addAttribute("category",activeCategory);
        m.addAttribute("products",activeProducts);

        return "index";
    }

    @GetMapping("/signin")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/products")
    public String products(Model m, @RequestParam (value = "category",defaultValue = "") String category,
                           @RequestParam (name="pageNo",defaultValue = "0")Integer pageNo,
                           @RequestParam(name = "pageSize",defaultValue = "10")Integer pageSize){
        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("paramValue",category);
        m.addAttribute("categories",categories);


//        List<Product> products = productService.getAllActiveProducts(category);
//        m.addAttribute("products",products);

        Page<Product> page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
         List<Product>products = page.getContent();
        m.addAttribute("products",products);
        m.addAttribute("productsSize",products.size());
        m.addAttribute("pageNo",page.getNumber());
        m.addAttribute("pageSize",pageSize);
        m.addAttribute("totalElements",page.getTotalElements());
        m.addAttribute("totalPages",page.getTotalPages());
        m.addAttribute("isFirst",page.isFirst());
        m.addAttribute("isLast",page.isLast());

        return "product";
    }
    @GetMapping("/product/{id}")
    public String product(@PathVariable int id,Model m){

        Product productById = productService.getProductById(id);
        m.addAttribute("product", productById);
        return "view_product";
    }
    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute UserDtls user, @RequestParam("profile_Image") MultipartFile file, HttpSession session) throws IOException {
        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        Boolean existsEmail = userService.existsEmail(user.getEmail());
        if(existsEmail){
            session.setAttribute("errorMsg", "Email ID Already exists");
        }
        else {


            user.setProfileImage(imageName);

            UserDtls saveUser = userService.saveUser(user);


            if (!ObjectUtils.isEmpty(saveUser)) {
                if (!file.isEmpty()) {

                    File saveFile = new ClassPathResource("static/img").getFile();

                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());

                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                }
                session.setAttribute("succMsg", "Registered Successfully");

            } else {
                session.setAttribute("errorMsg", "Something went wrong");
            }
        }
        return "redirect:/register";
    }

    @GetMapping("/search")
    public String searchProduct(
            @RequestParam String ch,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Model m) {

        Page<Product> page = productService.searchProductPagination( pageNo, pageSize,ch);

        m.addAttribute("products", page.getContent());
        m.addAttribute("productsSize", page.getContent().size());
        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());
        m.addAttribute("categories", categoryService.getAllActiveCategory());
        m.addAttribute("ch", ch);

        return "product";
    }


    // Forgot Password code
    @GetMapping("/forgot-password")
    public String showForgotPassword(){
         return "forgot_password";

    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        HttpSession session, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        UserDtls userByEmail = userService.getUserByEmail(email);
        if(ObjectUtils.isEmpty(userByEmail)){
            session.setAttribute("errorMsg", "Invalid email");
        }
        else {
            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(email,resetToken);

            //Generate URL: http://localhodt:8080/reset-password?token=efdvcdfbgfgrffgrff

           String url= commonUtil.generateUrl(request)+"/reset-password?token="+resetToken;


            Boolean sendMail =   commonUtil.sendMail(url,email);

          if(sendMail){
              session.setAttribute("succMsg", "Please check your email .. Password Reset link sent");
          }
          else{
              session.setAttribute("errorMsg", "Something wrong on server mail not send");
          }
        }


        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token,HttpSession session,Model m){

        UserDtls userByToken = userService.getUserByToken(token);
            if(ObjectUtils.isEmpty(userByToken)){
                m.addAttribute("msg","Your link is invalid or expired!!");
               return "message";
            }
            m.addAttribute("token",token);

        return "reset_password";

    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,@RequestParam String password, HttpSession session,Model m){

        UserDtls userByToken = userService.getUserByToken(token);
        if(ObjectUtils.isEmpty(userByToken)){
            m.addAttribute("msg","Your link is invalid or expired!!");
            return "message";
        }
        else{
            userByToken.setPassword(bCryptPasswordEncoder.encode(password));
            userByToken.setResetToken(null);
            userService.updateUser(userByToken);
            session.setAttribute("succMsg","Password change successfully.");
            m.addAttribute("msg","Password change successfully.");
            return "message";
        }



    }


}

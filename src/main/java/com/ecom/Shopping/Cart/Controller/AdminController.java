package com.ecom.Shopping.Cart.Controller;

import com.ecom.Shopping.Cart.model.Category;
import com.ecom.Shopping.Cart.model.Product;
import com.ecom.Shopping.Cart.model.ProductOrder;
import com.ecom.Shopping.Cart.model.UserDtls;
import com.ecom.Shopping.Cart.service.*;
import com.ecom.Shopping.Cart.util.OrderStatus;
import jakarta.servlet.http.HttpSession;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
   private OrderService orderService;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
    @GetMapping("/")
    public String index(){
        return "admin/index";
    }
    @GetMapping("/loadAddProduct")
        public String loadAddProduct(Model m ){

        List<Category> categories = categoryService.getAllCategory();
        m.addAttribute("categories",categories);



        return "admin/add_product";
    }
    @GetMapping("/category")
    public String category(Model m,@RequestParam (name="pageNo",defaultValue = "0")Integer pageNo,
                           @RequestParam (name="pageSize",defaultValue = "10") Integer pageSize){
//        m.addAttribute("categorys",categoryService.getAllCategory());
        Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
        List<Category> categorys =page.getContent();
        m.addAttribute("categorys",categorys );
        m.addAttribute("productsSize", page.getContent().size());
        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());



        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file , HttpSession session)
    throws IOException {
    String imageName= file!=null?file.getOriginalFilename():"default.jpg";
     category.setImageName(imageName);
        if(categoryService.existCategory(category.getName())){
            session.setAttribute("errorMsg", "Category Name already exists");
        }
        else{
          Category saveCategory=  categoryService.saveCategory(category);
            if(ObjectUtils.isEmpty(saveCategory)){
                session.setAttribute("errorMsg", "internal Server error");
            }
            else{

                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());
                System.out.println(path);
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                session.setAttribute("succMsg", "Saved Successfully");
            }
        }

        return "redirect:/admin/category";
    }
@GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id,HttpSession session){
        Boolean deleteCategory = categoryService.deleteCategory(id);
        if(deleteCategory){
            session.setAttribute("succMsg", "Category delete success");

        }
        else{

                session.setAttribute("errorMsg", "Something  went wrong");


        }
    return "redirect:/admin/category";
}
    @GetMapping("/loadEditCategory/{id}")
public String loadEditCategory(@PathVariable int id,Model m){
        m.addAttribute("category",categoryService.getCategoryById(id));

        return "admin/edit_category";
}

    @PostMapping("/updateCategory")
    public String  updateCategory(@ModelAttribute Category category,@RequestParam ("file") MultipartFile file,HttpSession session)throws IOException{


        Category oldCategory = categoryService.getCategoryById(category.getId());
        String imageName = !file.isEmpty() ? file.getOriginalFilename() : oldCategory.getImageName();
        if(!ObjectUtils.isEmpty(category)) {
            oldCategory.setName(category.getName());
            oldCategory.setIsActive(category.getIsActive());
            oldCategory.setImageName(imageName);

        }
     Category   updateCategory= categoryService.saveCategory(oldCategory);
        if(ObjectUtils.isEmpty(updateCategory)){
            session.setAttribute("errorMsg", "internal Server error");
        }
        else{
            if(!file.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());
                System.out.println(path);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            session.setAttribute("succMsg", "Category Updated Successfully");
        }



            return "redirect:/admin/loadEditCategory/"+category.getId();

    }
    @PostMapping("/saveProduct")
    public String saveProduct (@ModelAttribute Product product, @RequestParam("file") MultipartFile image, HttpSession session) throws IOException{

        String imageName = image.isEmpty()?"default.jpg":image.getOriginalFilename();
        product.setImage(imageName);
        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());
        Product saveProduct =productService.saveProduct(product);


        if(ObjectUtils.isEmpty(saveProduct)){
            session.setAttribute("errorMsg", "internal Server error");
        }
        else {
            if (!image.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator + image.getOriginalFilename());
                System.out.println(path);
                Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
            session.setAttribute("succMsg", "Product successfully Registered");
        }
        return "redirect:/admin/loadAddProduct";
    }

    @GetMapping("/products")
    public String loadViewProduct(Model m,@RequestParam(defaultValue = "") String ch,
                                  @RequestParam (name="pageNo",defaultValue = "0")Integer pageNo,
                                  @RequestParam (name="pageSize",defaultValue = "10") Integer pageSize ){
        Page<Product>  page =null;
        if(ch!=null&&ch.length()>0){
            page = productService.searchProductPagination(pageNo,pageSize,ch);
        }
        else{
            page = productService.getAllProductsPagination(pageNo,pageSize);
        }


        m.addAttribute("products",page.getContent());

        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());
        return "admin/products";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id,HttpSession session ){
        Boolean deleteProduct = productService.deleteProduct(id);

        if(deleteProduct){
            session.setAttribute("succMsg", "Product delete successfully");
        }
        else{
            session.setAttribute("errorMsg", "internal Server error");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/edit_product/{id}")
    public String  editProduct(@PathVariable int id,Model m ){

        m.addAttribute("product",productService.getProductById(id));
        m.addAttribute("categories",categoryService.getAllCategory());


        return "admin/edit_product";

    }

    @PostMapping("/updateProduct")
    public String updateProduct (@ModelAttribute Product product, @RequestParam("file") MultipartFile image, HttpSession session) throws IOException {
        if (product.getDiscount() < 0 || product.getDiscount() > 100) {
            session.setAttribute("errorMsg", "invalid discount");
        } else {
            Product updateProduct = productService.updateProduct(product, image);


            if (ObjectUtils.isEmpty(updateProduct)) {
                session.setAttribute("errorMsg", "internal Server error");
            } else {

                session.setAttribute("succMsg", "Product successfully Registered");
            }
        }
            return "redirect:/admin/edit_product/" + product.getId();
        }
           @GetMapping("/users")
            public String getAllUsers(Model m){
               List<UserDtls> users = userService.getUsers("ROLE_USER");
               m.addAttribute("users",users);
           return "/admin/users";
            }
            @GetMapping("/updateStatus")
            public String updateAccountStatus(@RequestParam Boolean status,@RequestParam  Integer id,HttpSession session){

             Boolean f = userService.updateAccountStatus(id,status);
             if(f){

                 session.setAttribute("succMsg","Account Status Updated");
             }
             else{
                 session.setAttribute("errorMsg", "Something went wrong");
             }
            return "redirect:/admin/users";
            }

    @GetMapping("/orders")
    public String  getAllOrders(Model m, @RequestParam (name="pageNo",defaultValue = "0")Integer pageNo,
                                @RequestParam (name="pageSize",defaultValue = "10") Integer pageSize){
        Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo,pageSize);

        m.addAttribute("orders",page.getContent());
        m.addAttribute("srch",false);

        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());




        return "/admin/orders";

    }

    @PostMapping("/update-order-status")
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

        return "redirect:/admin/orders";
    }

    @GetMapping("/search-order")
    public String searchProduct(@RequestParam String orderId,Model m,HttpSession session, @RequestParam (name="pageNo",defaultValue = "0")Integer pageNo,
                                @RequestParam (name="pageSize",defaultValue = "10") Integer pageSize){
        if (orderId != null &&orderId.length()>0) {


            ProductOrder order = orderService.getOrderByOrderId(orderId.trim());
            if (ObjectUtils.isEmpty(order)) {
                session.setAttribute("errorMsg", "Incorrect OrderId");
                m.addAttribute("orderDtls", null);
            } else {
                m.addAttribute("orderDtls", order);
            }
            m.addAttribute("srch", true);
        }
        else{
            Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);

            m.addAttribute("orders",page.getContent());
            m.addAttribute("srch",false);



            m.addAttribute("pageNo", page.getNumber());
            m.addAttribute("pageSize", pageSize);
            m.addAttribute("totalElements", page.getTotalElements());
            m.addAttribute("totalPages", page.getTotalPages());
            m.addAttribute("isFirst", page.isFirst());
            m.addAttribute("isLast", page.isLast());

        }
        return "/admin/orders";

    }
    @GetMapping("/add-admin")
    public String loadAdminAdd(){
        return "/admin/add_admin";
    }

    @PostMapping("/save-admin")
    public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("profile_Image") MultipartFile file, HttpSession session) throws IOException {
        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
        user.setProfileImage(imageName);

        UserDtls saveUser =  userService.saveAdmin(user);

        if(!ObjectUtils.isEmpty(saveUser)){
            if(!file.isEmpty()){

                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);

            }
            session.setAttribute("succMsg", "Registered Successfully");

        }
        else{
            session.setAttribute("errorMsg", "Something went wrong");
        }
        return "redirect:/admin/add-admin";
    }
    @GetMapping("/profile")
    public String profile(){
        return "/admin/profile";
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
        return "redirect:/admin/profile";
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
        return  "redirect:/admin/profile";
    }


    private UserDtls getLoggedInUserDetails(Principal p) {

        String email = p.getName();
        UserDtls userDtls = userService.getUserByEmail(email);

        return userDtls;
    }


}

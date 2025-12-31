package com.ecom.Shopping.Cart.service.impl;

import com.ecom.Shopping.Cart.model.Product;
import com.ecom.Shopping.Cart.repository.ProductRepository;
import com.ecom.Shopping.Cart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;


import org.springframework.data.domain.Pageable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();

    }

    /**
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable =  PageRequest.of(pageNo, pageSize);

        return productRepository.findAll(pageable);
    }

    @Override
    public Boolean deleteProduct(Integer id) {
        Product product=productRepository.findById(id).orElse(null);
        if(!ObjectUtils.isEmpty(product)){
            productRepository.delete(product);
            return true;
        }
        return false;
    }

    @Override
    public Product getProductById(int id) {
        Product product = productRepository.findById(id).orElse(null);
        return product;
    }

    @Override
    public Product updateProduct(Product product, MultipartFile image) {
        Product dbProduct = getProductById((product.getId()));
        String imageName = image.isEmpty()?dbProduct.getImage():image.getOriginalFilename();
        dbProduct.setImage(imageName);
        dbProduct.setTitle(product.getTitle());
        dbProduct.setDescription(product.getDescription());
        dbProduct.setCategory(product.getCategory());
        dbProduct.setIsActive(product.getIsActive());
        dbProduct.setPrice(product.getPrice());
        dbProduct.setStock(product.getStock());
        dbProduct.setDiscount(product.getDiscount());
        double discounts = product.getPrice() * (product.getDiscount() / 100.0);
         double discount = product.getPrice()-discounts;
        dbProduct.setDiscountPrice(discount);
        Product updateProduct = productRepository.save(dbProduct);

        if(!ObjectUtils.isEmpty(updateProduct)) {
            if (!image.isEmpty()) {
                try {
                    File saveFile = new ClassPathResource("static/img").getFile();

                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator + image.getOriginalFilename());
                    System.out.println(path);
                    Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

            }
            return updateProduct;
        }
        return null;
    }

    @Override
    public List<Product> getAllActiveProducts(String category) {
        List<Product> products =null;
        if(ObjectUtils.isEmpty(category)){
            products= productRepository.findByIsActiveTrue();
        }
        else{
            products=  productRepository.findByCategory(category);
        }

        return products;
    }

    @Override
    public List<Product> searchProduct(String ch) {
    return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,ch);
    }

    /**
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize,String category) {

        Page<Product> pageProduct=null;
        Pageable pageable =  PageRequest.of(pageNo, pageSize);
        if(ObjectUtils.isEmpty(category)){
            pageProduct= productRepository.findByIsActiveTrue(pageable);
        }
        else{
            pageProduct=  productRepository.findByCategory(pageable,category);
        }


        return pageProduct;
    }

    /**
     * @param ch
     * @paramof
     * @return
     */
    @Override

    public Page<Product> searchProductPagination(Integer pageNo ,Integer pageSize,String ch) {
        Pageable pageable = PageRequest.of(pageNo,pageSize);
        return productRepository
                .findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        ch, ch, pageable);
    }

}

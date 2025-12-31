package com.ecom.Shopping.Cart.repository;

import com.ecom.Shopping.Cart.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable ;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer> {
  public List<Product> findByIsActiveTrue();
  public Page<Product> findByIsActiveTrue(Pageable pageable);

  public List<Product> findByCategory( String category);

  public List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch,String ch2);

  public Page<Product> findByCategory(Pageable pageable, String category);
  public Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(
          String title, String category, Pageable pageable);
}

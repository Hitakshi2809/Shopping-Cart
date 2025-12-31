package com.ecom.Shopping.Cart.service;

import com.ecom.Shopping.Cart.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService {


    public  Boolean existCategory(String name);
    public Category saveCategory(Category category);


    public List<Category> getAllCategory();
    public Page<Category> getAllCategoryPagination(Integer pageNo,Integer pageSize);

    public Boolean deleteCategory(int id);
    public Category getCategoryById(int id);

    public List<Category> getAllActiveCategory();
}

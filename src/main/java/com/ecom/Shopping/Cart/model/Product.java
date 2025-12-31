package com.ecom.Shopping.Cart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 500)
    private String title;
    @Column(length = 5000)
    private String description;

    private String category;

    private Double price;

    private int discount;

    private Double discountPrice;

    private int stock;

    private String image;

    private Boolean isActive;





}

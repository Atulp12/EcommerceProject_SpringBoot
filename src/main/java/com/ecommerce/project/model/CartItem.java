package com.ecommerce.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    private double discount;

    private double productPrice;

    private Integer quantity;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "cart_id")
    private Cart cart;


    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

}

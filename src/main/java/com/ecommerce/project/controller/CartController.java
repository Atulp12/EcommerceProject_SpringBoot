package com.ecommerce.project.controller;

import com.ecommerce.project.dto.CartDto;
import com.ecommerce.project.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDto> addProductToCart(@PathVariable Long productId,@PathVariable Integer quantity){
        CartDto cartDto = cartService.addProductToCart(productId,quantity);
        return  new ResponseEntity<>(cartDto, HttpStatus.CREATED);
    }
}

package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartDto;

import java.util.List;

public interface CartService {
    CartDto addProductToCart(Long productId, Integer quantity);

    List<CartDto> getAllCarts();

    CartDto getCart(String email, Long cartId);

    CartDto updateProductQuantityInCart(Long productId,Integer quantity);

    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCarts(Long cartId, Long productId);
}

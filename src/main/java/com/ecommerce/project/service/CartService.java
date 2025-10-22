package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartDto;

public interface CartService {
    CartDto addProductToCart(Long productId, Integer quantity);
}

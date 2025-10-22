package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartDto;
import com.ecommerce.project.dto.ProductDto;
import com.ecommerce.project.exception.ApiException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public CartDto addProductToCart(Long productId, Integer quantity) {
        //Finding existing cart or create one
        Cart cart = createCart();

        //Retrieve the product details
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        //Validations
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(),productId);

        if(cartItem != null){
            throw new ApiException("Product " +product.getProductName()+" already exists!!");
        }

        if(product.getQuantity() == 0){
            throw new ApiException(product.getProductName()+" is not available.");
        }

        if(product.getQuantity() < quantity){
            throw new ApiException("Please make an order of the "
                    +product.getProductName() +" less than or equal to the quantity "+product.getQuantity() + ".");
        }

        //Create cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setQuantity(quantity);
        newCartItem.setProductPrice(product.getSpecialPrice());

        //Save cart item
        cartItemRepository.save(newCartItem);
        product.setQuantity(product.getQuantity() /* - quantity (to update the stock) */);
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice()*quantity));

        //Return update cartDto
        cartRepository.save(cart);
        cart.getCartItems().add(newCartItem);
        CartDto cartDto = modelMapper.map(cart,CartDto.class);

        List<CartItem> cartItemList = cart.getCartItems();
        Stream<ProductDto> productDtoStream = cartItemList.stream().map(item ->{
            ProductDto map = modelMapper.map(item.getProduct(),ProductDto.class);
            map.setQuantity(item.getQuantity());
            return map;
        });
        cartDto.setProducts(productDtoStream.toList());
        return cartDto;
    }

    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        Cart savedCart = cartRepository.save(cart);
        return savedCart;
    }
}

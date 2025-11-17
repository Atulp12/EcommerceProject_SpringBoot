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
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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

    @Override
    public List<CartDto> getAllCarts() {

        List<Cart> cartList = cartRepository.findAll();

        if(cartList.isEmpty()){
            throw new ApiException("Cart not exists!!");
        }

        List<CartDto> cartDtos = cartList.stream().map(
                cart -> {
                    CartDto cartDto = modelMapper.map(cart,CartDto.class);
                    List<ProductDto> productDtos = cart.getCartItems().stream().map(
                            p ->
                                modelMapper.map(p.getProduct(),ProductDto.class)
                    ).toList();
                    cartDto.setProducts(productDtos);
                    return cartDto;
                }
        ).toList();

        return cartDtos;
    }

    @Override
    public CartDto getCart(String email, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(email,cartId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart","cartId",cartId);
        }
        CartDto cartDto = modelMapper.map(cart,CartDto.class);
        cart.getCartItems().forEach(c->c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDto> productDtos = cart.getCartItems()
                                            .stream()
                                            .map(p -> modelMapper.map(p.getProduct(),ProductDto.class))
                                            .toList();

        cartDto.setProducts(productDtos);
        return cartDto;
    }

    @Transactional
    @Override
    public CartDto updateProductQuantityInCart(Long productId, Integer quantity) {
        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId  = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0) {
            throw new ApiException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new ApiException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId);

        if (cartItem == null) {
            throw new ApiException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        int newQuantity =  cartItem.getQuantity() + quantity;

        if(newQuantity < 0){
            throw new ApiException("The resulting quantity cannot be negative!!");
        }

        if(newQuantity == 0){
            deleteProductFromCart(cartId,productId);
        }else {

            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }
        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }


        CartDto cartDTO = modelMapper.map(cart, CartDto.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDto> productStream = cartItems.stream().map(item -> {
            ProductDto prd = modelMapper.map(item.getProduct(), ProductDto.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });


        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId);

        if (cartItem == null) {
            throw new ApiException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice
                + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
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

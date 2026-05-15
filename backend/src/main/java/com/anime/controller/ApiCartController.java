package com.anime.controller;

import com.anime.common.Result;
import com.anime.dto.CartAddRequest;
import com.anime.dto.CartSelectedRequest;
import com.anime.dto.CartUpdateRequest;
import com.anime.entity.Cart;
import com.anime.entity.Product;
import com.anime.security.AuthContext;
import com.anime.service.CartService;
import com.anime.service.ProductService;
import com.anime.vo.CartVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class ApiCartController {

    private final CartService cartService;
    private final ProductService productService;

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CartAddRequest request) {
        Long userId = AuthContext.getUserId();
        Cart exist = cartService.getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, request.getProductId()));
        if (exist == null) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(request.getProductId());
            cart.setQuantity(request.getQuantity());
            cart.setSelected(1);
            cartService.save(cart);
        } else {
            exist.setQuantity(exist.getQuantity() + request.getQuantity());
            cartService.updateById(exist);
        }
        return Result.success();
    }

    @GetMapping
    public Result<List<CartVO>> list() {
        Long userId = AuthContext.getUserId();
        List<Cart> carts = cartService.list(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        List<CartVO> cartVOs = carts.stream().map(cart -> {
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setUserId(cart.getUserId());
            vo.setProductId(cart.getProductId());
            vo.setQuantity(cart.getQuantity());
            vo.setSelected(cart.getSelected());
            vo.setCreateTime(cart.getCreateTime());
            vo.setUpdateTime(cart.getUpdateTime());
            
            Product product = productService.getById(cart.getProductId());
            if (product != null) {
                vo.setName(product.getName());
                vo.setImageUrl(product.getMainImage());
                vo.setPrice(product.getPrice());
                vo.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
                System.out.println("Cart item: " + cart.getProductId() + ", Product price: " + product.getPrice());
            } else {
                System.out.println("Product not found for ID: " + cart.getProductId());
            }
            
            return vo;
        }).collect(Collectors.toList());
        return Result.success(cartVOs);
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CartUpdateRequest request) {
        Long userId = AuthContext.getUserId();
        Cart cart = new Cart();
        cart.setId(request.getId());
        cart.setQuantity(request.getQuantity());
        if (request.getSelected() != null) {
            cart.setSelected(request.getSelected());
        }
        cartService.update(cart, new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getId, request.getId())
                .eq(Cart::getUserId, userId));
        return Result.success();
    }

    @DeleteMapping("/remove")
    public Result<Void> remove(@RequestParam Long id) {
        Long userId = AuthContext.getUserId();
        cartService.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getId, id).eq(Cart::getUserId, userId));
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        Long userId = AuthContext.getUserId();
        cartService.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        return Result.success();
    }

    @PostMapping("/selected")
    public Result<Void> selected(@Valid @RequestBody CartSelectedRequest request) {
        Long userId = AuthContext.getUserId();
        cartService.update(new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getId, request.getId())
                .eq(Cart::getUserId, userId)
                .set(Cart::getSelected, request.getSelected() == null ? 1 : request.getSelected()));
        return Result.success();
    }
}


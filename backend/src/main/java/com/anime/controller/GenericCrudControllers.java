package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.*;
import com.anime.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GenericCrudControllers {

    private final AddressService addressService;
    private final AnimeIpService animeIpService;
    private final ArticleService articleService;
    private final ArticleCommentService articleCommentService;
    private final CartService cartService;
    private final CharacterEntityService characterEntityService;
    private final FavoriteService favoriteService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final ProductImageService productImageService;
    private final ReviewService reviewService;
    private final UserBehaviorService userBehaviorService;

    @GetMapping("/address")
    public Result<List<Address>> listAddress() {
        return Result.success(addressService.list());
    }

    @GetMapping("/address/{id}")
    public Result<Address> getAddress(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }

    @PostMapping("/address")
    public Result<Void> createAddress(@RequestBody Address address) {
        addressService.save(address);
        return Result.success();
    }

    @PutMapping("/address")
    public Result<Void> updateAddress(@RequestBody Address address) {
        addressService.updateById(address);
        return Result.success();
    }

    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        addressService.removeById(id);
        return Result.success();
    }

    @GetMapping("/anime-ip")
    public Result<List<AnimeIp>> listAnimeIp() {
        return Result.success(animeIpService.list());
    }

    @GetMapping("/anime-ip/{id}")
    public Result<AnimeIp> getAnimeIp(@PathVariable Long id) {
        return Result.success(animeIpService.getById(id));
    }

    @PostMapping("/anime-ip")
    public Result<Void> createAnimeIp(@RequestBody AnimeIp animeIp) {
        animeIpService.save(animeIp);
        return Result.success();
    }

    @PutMapping("/anime-ip")
    public Result<Void> updateAnimeIp(@RequestBody AnimeIp animeIp) {
        animeIpService.updateById(animeIp);
        return Result.success();
    }

    @DeleteMapping("/anime-ip/{id}")
    public Result<Void> deleteAnimeIp(@PathVariable Long id) {
        animeIpService.removeById(id);
        return Result.success();
    }

    @GetMapping("/article")
    public Result<List<Article>> listArticle() {
        return Result.success(articleService.list());
    }

    @GetMapping("/article/{id}")
    public Result<Article> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }

    @PostMapping("/article")
    public Result<Void> createArticle(@RequestBody Article article) {
        articleService.save(article);
        return Result.success();
    }

    @PutMapping("/article")
    public Result<Void> updateArticle(@RequestBody Article article) {
        articleService.updateById(article);
        return Result.success();
    }

    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.removeById(id);
        return Result.success();
    }

    @GetMapping("/article-comment")
    public Result<List<ArticleComment>> listArticleComment() {
        return Result.success(articleCommentService.list());
    }

    @GetMapping("/article-comment/{id}")
    public Result<ArticleComment> getArticleComment(@PathVariable Long id) {
        return Result.success(articleCommentService.getById(id));
    }

    @PostMapping("/article-comment")
    public Result<Void> createArticleComment(@RequestBody ArticleComment articleComment) {
        articleCommentService.save(articleComment);
        return Result.success();
    }

    @PutMapping("/article-comment")
    public Result<Void> updateArticleComment(@RequestBody ArticleComment articleComment) {
        articleCommentService.updateById(articleComment);
        return Result.success();
    }

    @DeleteMapping("/article-comment/{id}")
    public Result<Void> deleteArticleComment(@PathVariable Long id) {
        articleCommentService.removeById(id);
        return Result.success();
    }

    @GetMapping("/cart")
    public Result<List<Cart>> listCart() {
        return Result.success(cartService.list());
    }

    @GetMapping("/cart/{id}")
    public Result<Cart> getCart(@PathVariable Long id) {
        return Result.success(cartService.getById(id));
    }

    @PostMapping("/cart")
    public Result<Void> createCart(@RequestBody Cart cart) {
        cartService.save(cart);
        return Result.success();
    }

    @PutMapping("/cart")
    public Result<Void> updateCart(@RequestBody Cart cart) {
        cartService.updateById(cart);
        return Result.success();
    }

    @DeleteMapping("/cart/{id}")
    public Result<Void> deleteCart(@PathVariable Long id) {
        cartService.removeById(id);
        return Result.success();
    }

    @GetMapping("/character")
    public Result<List<CharacterEntity>> listCharacter() {
        return Result.success(characterEntityService.list());
    }

    @GetMapping("/character/{id}")
    public Result<CharacterEntity> getCharacter(@PathVariable Long id) {
        return Result.success(characterEntityService.getById(id));
    }

    @PostMapping("/character")
    public Result<Void> createCharacter(@RequestBody CharacterEntity character) {
        characterEntityService.save(character);
        return Result.success();
    }

    @PutMapping("/character")
    public Result<Void> updateCharacter(@RequestBody CharacterEntity character) {
        characterEntityService.updateById(character);
        return Result.success();
    }

    @DeleteMapping("/character/{id}")
    public Result<Void> deleteCharacter(@PathVariable Long id) {
        characterEntityService.removeById(id);
        return Result.success();
    }

    @GetMapping("/favorite")
    public Result<List<Favorite>> listFavorite() {
        return Result.success(favoriteService.list());
    }

    @GetMapping("/favorite/{id}")
    public Result<Favorite> getFavorite(@PathVariable Long id) {
        return Result.success(favoriteService.getById(id));
    }

    @PostMapping("/favorite")
    public Result<Void> createFavorite(@RequestBody Favorite favorite) {
        favoriteService.save(favorite);
        return Result.success();
    }

    @PutMapping("/favorite")
    public Result<Void> updateFavorite(@RequestBody Favorite favorite) {
        favoriteService.updateById(favorite);
        return Result.success();
    }

    @DeleteMapping("/favorite/{id}")
    public Result<Void> deleteFavorite(@PathVariable Long id) {
        favoriteService.removeById(id);
        return Result.success();
    }

    @GetMapping("/order")
    public Result<List<Order>> listOrder() {
        return Result.success(orderService.list());
    }

    @GetMapping("/order/{id}")
    public Result<Order> getOrder(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @PostMapping("/order")
    public Result<Void> createOrder(@RequestBody Order order) {
        orderService.save(order);
        return Result.success();
    }

    @PutMapping("/order")
    public Result<Void> updateOrder(@RequestBody Order order) {
        orderService.updateById(order);
        return Result.success();
    }

    @DeleteMapping("/order/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderService.removeById(id);
        return Result.success();
    }

    @GetMapping("/order-item")
    public Result<List<OrderItem>> listOrderItem() {
        return Result.success(orderItemService.list());
    }

    @GetMapping("/order-item/{id}")
    public Result<OrderItem> getOrderItem(@PathVariable Long id) {
        return Result.success(orderItemService.getById(id));
    }

    @PostMapping("/order-item")
    public Result<Void> createOrderItem(@RequestBody OrderItem orderItem) {
        orderItemService.save(orderItem);
        return Result.success();
    }

    @PutMapping("/order-item")
    public Result<Void> updateOrderItem(@RequestBody OrderItem orderItem) {
        orderItemService.updateById(orderItem);
        return Result.success();
    }

    @DeleteMapping("/order-item/{id}")
    public Result<Void> deleteOrderItem(@PathVariable Long id) {
        orderItemService.removeById(id);
        return Result.success();
    }

    @GetMapping("/product")
    public Result<List<Product>> listProduct() {
        return Result.success(productService.list());
    }

    @GetMapping("/product/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping("/product")
    public Result<Void> createProduct(@RequestBody Product product) {
        productService.save(product);
        return Result.success();
    }

    @PutMapping("/product")
    public Result<Void> updateProduct(@RequestBody Product product) {
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/product/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }

    @GetMapping("/product-category")
    public Result<List<ProductCategory>> listProductCategory() {
        return Result.success(productCategoryService.list());
    }

    @GetMapping("/product-category/{id}")
    public Result<ProductCategory> getProductCategory(@PathVariable Long id) {
        return Result.success(productCategoryService.getById(id));
    }

    @PostMapping("/product-category")
    public Result<Void> createProductCategory(@RequestBody ProductCategory productCategory) {
        productCategoryService.save(productCategory);
        return Result.success();
    }

    @PutMapping("/product-category")
    public Result<Void> updateProductCategory(@RequestBody ProductCategory productCategory) {
        productCategoryService.updateById(productCategory);
        return Result.success();
    }

    @DeleteMapping("/product-category/{id}")
    public Result<Void> deleteProductCategory(@PathVariable Long id) {
        productCategoryService.removeById(id);
        return Result.success();
    }

    @GetMapping("/product-image")
    public Result<List<ProductImage>> listProductImage() {
        return Result.success(productImageService.list());
    }

    @GetMapping("/product-image/{id}")
    public Result<ProductImage> getProductImage(@PathVariable Long id) {
        return Result.success(productImageService.getById(id));
    }

    @PostMapping("/product-image")
    public Result<Void> createProductImage(@RequestBody ProductImage productImage) {
        productImageService.save(productImage);
        return Result.success();
    }

    @PutMapping("/product-image")
    public Result<Void> updateProductImage(@RequestBody ProductImage productImage) {
        productImageService.updateById(productImage);
        return Result.success();
    }

    @DeleteMapping("/product-image/{id}")
    public Result<Void> deleteProductImage(@PathVariable Long id) {
        productImageService.removeById(id);
        return Result.success();
    }

    @GetMapping("/review")
    public Result<List<Review>> listReview() {
        return Result.success(reviewService.list());
    }

    @GetMapping("/review/{id}")
    public Result<Review> getReview(@PathVariable Long id) {
        return Result.success(reviewService.getById(id));
    }

    @PostMapping("/review")
    public Result<Void> createReview(@RequestBody Review review) {
        reviewService.save(review);
        return Result.success();
    }

    @PutMapping("/review")
    public Result<Void> updateReview(@RequestBody Review review) {
        reviewService.updateById(review);
        return Result.success();
    }

    @DeleteMapping("/review/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        reviewService.removeById(id);
        return Result.success();
    }

    @GetMapping("/user-behavior")
    public Result<List<UserBehavior>> listUserBehavior() {
        return Result.success(userBehaviorService.list());
    }

    @GetMapping("/user-behavior/{id}")
    public Result<UserBehavior> getUserBehavior(@PathVariable Long id) {
        return Result.success(userBehaviorService.getById(id));
    }

    @PostMapping("/user-behavior")
    public Result<Void> createUserBehavior(@RequestBody UserBehavior userBehavior) {
        userBehaviorService.save(userBehavior);
        return Result.success();
    }

    @PutMapping("/user-behavior")
    public Result<Void> updateUserBehavior(@RequestBody UserBehavior userBehavior) {
        userBehaviorService.updateById(userBehavior);
        return Result.success();
    }

    @DeleteMapping("/user-behavior/{id}")
    public Result<Void> deleteUserBehavior(@PathVariable Long id) {
        userBehaviorService.removeById(id);
        return Result.success();
    }
}


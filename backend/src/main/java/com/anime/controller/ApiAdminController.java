package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.*;
import com.anime.service.*;
import com.anime.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ApiAdminController {

    private final UserService userService;
    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private final AnimeIpService animeIpService;
    private final CharacterEntityService characterEntityService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final AddressService addressService;
    private final ArticleService articleService;
    private final UserBehaviorService userBehaviorService;
    private final DataSource dataSource;

    @GetMapping("/users")
    public Result<Page<User>> users(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) String keyword) {
        var wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getPhone, keyword).or().like(User::getEmail, keyword));
        }
        return Result.success(userService.page(new Page<>(page, size), wrapper));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.update(new LambdaUpdateWrapper<User>().eq(User::getId, id).set(User::getStatus, status));
        return Result.success();
    }

    @GetMapping("/products")
    public Result<Page<Product>> products(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(productService.page(new Page<>(page, size), new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreateTime)));
    }

    @PostMapping("/products")
    public Result<Void> createProduct(@RequestBody Product product) {
        productService.save(product);
        return Result.success();
    }

    @PutMapping("/products")
    public Result<Void> updateProduct(@RequestBody Product product) {
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }

    @PutMapping("/products/{id}/status")
    public Result<Void> productStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.update(new LambdaUpdateWrapper<Product>().eq(Product::getId, id).set(Product::getStatus, status));
        return Result.success();
    }

    @GetMapping("/categories/tree")
    public Result<List<ProductCategory>> categories() {
        return Result.success(productCategoryService.list(new LambdaQueryWrapper<ProductCategory>().orderByAsc(ProductCategory::getSortOrder)));
    }

    @PostMapping("/categories")
    public Result<Void> createCategory(@RequestBody ProductCategory c) {
        productCategoryService.save(c);
        return Result.success();
    }

    @PutMapping("/categories")
    public Result<Void> updateCategory(@RequestBody ProductCategory c) {
        productCategoryService.updateById(c);
        return Result.success();
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        productCategoryService.removeById(id);
        return Result.success();
    }

    @GetMapping("/anime-ips")
    public Result<List<AnimeIp>> animeIps() {
        return Result.success(animeIpService.list());
    }

    @PostMapping("/anime-ips")
    public Result<Void> createAnimeIp(@RequestBody AnimeIp x) {
        animeIpService.save(x);
        return Result.success();
    }

    @PutMapping("/anime-ips")
    public Result<Void> updateAnimeIp(@RequestBody AnimeIp x) {
        animeIpService.updateById(x);
        return Result.success();
    }

    @DeleteMapping("/anime-ips/{id}")
    public Result<Void> deleteAnimeIp(@PathVariable Long id) {
        animeIpService.removeById(id);
        return Result.success();
    }

    @GetMapping("/characters")
    public Result<List<CharacterEntity>> characters() {
        return Result.success(characterEntityService.list());
    }

    @PostMapping("/characters")
    public Result<Void> createCharacter(@RequestBody CharacterEntity c) {
        characterEntityService.save(c);
        return Result.success();
    }

    @PutMapping("/characters")
    public Result<Void> updateCharacter(@RequestBody CharacterEntity c) {
        characterEntityService.updateById(c);
        return Result.success();
    }

    @DeleteMapping("/characters/{id}")
    public Result<Void> deleteCharacter(@PathVariable Long id) {
        characterEntityService.removeById(id);
        return Result.success();
    }

    @GetMapping("/orders")
    public Result<Page<OrderVO>> adminOrders(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) Long userId) {
        var wrapper = new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime);
        if (status != null) wrapper.eq(Order::getStatus, status);
        if (userId != null) wrapper.eq(Order::getUserId, userId);
        Page<Order> orderPage = orderService.page(new Page<>(page, size), wrapper);
        
        Page<OrderVO> voPage = new Page<>(page, size, orderPage.getTotal());
        List<OrderVO> voList = orderPage.getRecords().stream().map(order -> {
            OrderVO vo = new OrderVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setUserId(order.getUserId());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setPayAmount(order.getPayAmount());
            vo.setFreightAmount(order.getFreightAmount());
            vo.setDiscountAmount(order.getDiscountAmount());
            vo.setPayType(order.getPayType());
            vo.setStatus(order.getStatus());
            vo.setPaymentTime(order.getPaymentTime());
            vo.setDeliveryTime(order.getDeliveryTime());
            vo.setReceiveTime(order.getReceiveTime());
            vo.setAddressId(order.getAddressId());
            vo.setRemark(order.getRemark());
            vo.setCreateTime(order.getCreateTime());
            vo.setUpdateTime(order.getUpdateTime());
            
            User user = userService.getById(order.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setUserPhone(user.getPhone());
            }
            
            Address address = addressService.getById(order.getAddressId());
            if (address != null) {
                vo.setReceiverName(address.getReceiverName());
                vo.setReceiverPhone(address.getReceiverPhone());
                vo.setFullAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
            }
            
            List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            vo.setItems(items);
            
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return Result.success(voPage);
    }

    @PutMapping("/orders/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id, @RequestParam String logisticsNo) {
        Order order = orderService.getById(id);
        if (order == null) return Result.fail("order not found");
        order.setStatus(3); // 待收货
        order.setRemark((order.getRemark() == null ? "" : order.getRemark() + ";") + "logisticsNo=" + logisticsNo);
        orderService.updateById(order);
        return Result.success();
    }

    @PutMapping("/orders/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.update(new LambdaUpdateWrapper<Order>().eq(Order::getId, id).set(Order::getStatus, status));
        return Result.success();
    }

    @GetMapping("/articles")
    public Result<Page<Article>> adminArticles(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return Result.success(articleService.page(new Page<>(page, size), new LambdaQueryWrapper<Article>().orderByDesc(Article::getCreateTime)));
    }

    @PostMapping("/articles")
    public Result<Void> createArticle(@RequestBody Article article) {
        articleService.save(article);
        return Result.success();
    }

    @PutMapping("/articles/{id}")
    public Result<Void> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        articleService.updateById(article);
        return Result.success();
    }

    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.removeById(id);
        return Result.success();
    }

    @PutMapping("/articles/{id}/publish")
    public Result<Void> publishArticle(@PathVariable Long id, @RequestParam Integer status) {
        articleService.update(new LambdaUpdateWrapper<Article>().eq(Article::getId, id).set(Article::getStatus, status));
        return Result.success();
    }

    @GetMapping("/inventory")
    public Result<List<Product>> inventory() {
        return Result.success(productService.list(new LambdaQueryWrapper<Product>().lt(Product::getStock, 10)));
    }

    @GetMapping("/statistics/sales")
    public Result<List<Order>> sales(@RequestParam(defaultValue = "day") String dimension) {
        // 简化：先返回已支付后订单明细，前端可按 day/week/month 聚合
        return Result.success(orderService.list(new LambdaQueryWrapper<Order>().ge(Order::getStatus, 2)));
    }

    @GetMapping("/statistics/hot-products")
    public Result<List<Product>> hotProducts() {
        return Result.success(productService.list(new LambdaQueryWrapper<Product>().orderByDesc(Product::getSales).last("limit 20")));
    }

    @GetMapping("/statistics/user-behavior")
    public Result<List<UserBehavior>> userBehavior() {
        LocalDate from = LocalDate.now().minusDays(30);
        return Result.success(userBehaviorService.list(
                new LambdaQueryWrapper<UserBehavior>().ge(UserBehavior::getBehaviorTime, from.atStartOfDay())
        ));
    }

    @PostMapping("/fix/article-column")
    public Result<Void> fixArticleColumn() {
        try (Connection conn = dataSource.getConnection()) {
            // 修改 cover_image 字段长度为 1000
            String sql = "ALTER TABLE article MODIFY COLUMN cover_image VARCHAR(1000)";
            conn.createStatement().execute(sql);
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.fail("修改失败: " + e.getMessage());
        }
    }
}


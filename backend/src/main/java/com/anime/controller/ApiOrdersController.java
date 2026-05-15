package com.anime.controller;

import com.anime.common.Result;
import com.anime.dto.CreateOrderRequest;
import com.anime.security.AuthContext;
import com.anime.service.OrderBizService;
import com.anime.vo.OrderVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ApiOrdersController {

    private final OrderBizService orderBizService;

    @PostMapping
    public Result<OrderVO> create(@RequestBody CreateOrderRequest request) {
        Long userId = AuthContext.getUserId();
        return Result.success(orderBizService.createFromSelectedCart(userId, request));
    }

    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable("id") Long id) {
        orderBizService.pay(AuthContext.getUserId(), id);
        return Result.success();
    }

    @GetMapping
    public Result<Page<OrderVO>> myOrders(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return Result.success(orderBizService.myOrders(AuthContext.getUserId(), status, page, size));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable("id") Long id) {
        return Result.success(orderBizService.myOrderDetail(AuthContext.getUserId(), id));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        orderBizService.cancel(AuthContext.getUserId(), id);
        return Result.success();
    }

    @PutMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable("id") Long id) {
        orderBizService.receive(AuthContext.getUserId(), id);
        return Result.success();
    }
}


package com.anime.service.impl;

import com.anime.dto.CreateOrderRequest;
import com.anime.entity.Address;
import com.anime.entity.Cart;
import com.anime.entity.Order;
import com.anime.entity.OrderItem;
import com.anime.entity.Product;
import com.anime.entity.User;
import com.anime.exception.BusinessException;
import com.anime.mapper.ProductMapper;
import com.anime.service.AddressService;
import com.anime.service.CartService;
import com.anime.service.OrderBizService;
import com.anime.service.OrderItemService;
import com.anime.service.OrderService;
import com.anime.service.ProductService;
import com.anime.service.UserService;
import com.anime.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderBizServiceImpl implements OrderBizService {

    private final CartService cartService;
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final UserService userService;
    private final AddressService addressService;

    @Override
    @Transactional
    public OrderVO createFromSelectedCart(Long userId, CreateOrderRequest request) {
        List<Cart> carts = cartService.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1));
        if (carts.isEmpty()) {
            throw new BusinessException("no selected cart items");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Cart c : carts) {
            Product p = productService.getById(c.getProductId());
            if (p == null || p.getStatus() == 0) {
                throw new BusinessException("product unavailable: " + c.getProductId());
            }
            int ok = productMapper.deductStock(p.getId(), c.getQuantity());
            if (ok == 0) {
                throw new BusinessException("stock not enough: " + p.getName());
            }
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
        }

        Order order = new Order();
        order.setOrderNo("AN" + System.currentTimeMillis() + userId);
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayType(request.getPayType() != null ? request.getPayType() : 1);
        order.setStatus(1);
        order.setAddressId(request.getAddressId());
        order.setRemark(request.getRemark());
        orderService.save(order);

        for (Cart c : carts) {
            Product p = productService.getById(c.getProductId());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(p.getId());
            item.setProductName(p.getName());
            item.setProductImage(p.getMainImage());
            item.setPrice(p.getPrice());
            item.setQuantity(c.getQuantity());
            item.setTotalAmount(p.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
            orderItemService.save(item);
        }

        cartService.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).eq(Cart::getSelected, 1));
        
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        orderVO.setItems(items);
        
        return orderVO;
    }

    @Override
    public void pay(Long userId, Long orderId) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException("order not found");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("only unpaid order can be paid");
        }
        order.setStatus(2);
        order.setPaymentTime(LocalDateTime.now());
        orderService.updateById(order);
    }

    @Override
    public Page<OrderVO> myOrders(Long userId, Integer status, int page, int size) {
        var wrapper = new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> orderPage = orderService.page(new Page<>(page, size), wrapper);
        
        Page<OrderVO> voPage = new Page<>(page, size, orderPage.getTotal());
        List<OrderVO> voList = orderPage.getRecords().stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            vo.setItems(items);
            fillOrderInfo(vo, order);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public OrderVO myOrderDetail(Long userId, Long orderId) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(404, "order not found");
        }
        
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        vo.setItems(items);
        fillOrderInfo(vo, order);
        
        return vo;
    }

    private void fillOrderInfo(OrderVO vo, Order order) {
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
    }

    @Override
    public void cancel(Long userId, Long orderId) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(404, "order not found");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("only unpaid order can be canceled");
        }
        order.setStatus(0);
        orderService.updateById(order);
    }

    @Override
    public void receive(Long userId, Long orderId) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(404, "order not found");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException("only shipped order can be received");
        }
        order.setStatus(4);
        order.setReceiveTime(LocalDateTime.now());
        orderService.updateById(order);
    }
}


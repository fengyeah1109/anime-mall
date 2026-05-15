package com.anime.service.impl;

import com.anime.entity.AfterSale;
import com.anime.entity.Order;
import com.anime.entity.OrderItem;
import com.anime.mapper.AfterSaleMapper;
import com.anime.mapper.OrderItemMapper;
import com.anime.mapper.OrderMapper;
import com.anime.service.AfterSaleService;
import com.anime.vo.AfterSaleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AfterSaleServiceImpl extends ServiceImpl<AfterSaleMapper, AfterSale> implements AfterSaleService {

    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;

    public AfterSaleServiceImpl(OrderItemMapper orderItemMapper, OrderMapper orderMapper) {
        this.orderItemMapper = orderItemMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public List<AfterSaleVO> getUserAfterSales(Long userId) {
        List<AfterSale> afterSales = baseMapper.selectList(
                new LambdaQueryWrapper<AfterSale>()
                        .eq(AfterSale::getUserId, userId)
                        .orderByDesc(AfterSale::getApplyTime)
        );
        return convertToVOList(afterSales);
    }

    @Override
    public List<AfterSaleVO> getAllAfterSales() {
        List<AfterSale> afterSales = baseMapper.selectList(
                new LambdaQueryWrapper<AfterSale>()
                        .orderByDesc(AfterSale::getApplyTime)
        );
        return convertToVOList(afterSales);
    }

    @Override
    public AfterSaleVO getDetail(Long id) {
        AfterSale afterSale = getById(id);
        if (afterSale == null) {
            return null;
        }
        return convertToVO(afterSale);
    }

    @Override
    public boolean updateStatus(Long id, Integer status, Long adminId, String reply) {
        AfterSale afterSale = new AfterSale();
        afterSale.setId(id);
        afterSale.setStatus(status);
        afterSale.setReply(reply);
        afterSale.setHandleTime(LocalDateTime.now());
        return updateById(afterSale);
    }

    private List<AfterSaleVO> convertToVOList(List<AfterSale> afterSales) {
        if (CollectionUtils.isEmpty(afterSales)) {
            return new ArrayList<>();
        }
        List<Long> orderItemIds = afterSales.stream()
                .map(AfterSale::getOrderItemId)
                .distinct()
                .collect(Collectors.toList());
        
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getId, orderItemIds)
        );
        Map<Long, OrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, item -> item, (a, b) -> a));
        
        List<Long> orderIds = orderItems.stream()
                .map(OrderItem::getOrderId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().in(Order::getId, orderIds)
        );
        Map<Long, Order> orderMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, order -> order, (a, b) -> a));
        
        return afterSales.stream().map(afterSale -> {
            AfterSaleVO vo = new AfterSaleVO();
            BeanUtils.copyProperties(afterSale, vo);
            OrderItem item = orderItemMap.get(afterSale.getOrderItemId());
            if (item != null) {
                vo.setProductName(item.getProductName());
                vo.setProductImage(item.getProductImage());
                vo.setPrice(item.getPrice());
                vo.setQuantity(item.getQuantity());
                vo.setOrderId(item.getOrderId());
                Order order = orderMap.get(item.getOrderId());
                if (order != null) {
                    vo.setOrderNo(order.getOrderNo());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private AfterSaleVO convertToVO(AfterSale afterSale) {
        AfterSaleVO vo = new AfterSaleVO();
        BeanUtils.copyProperties(afterSale, vo);
        OrderItem item = orderItemMapper.selectById(afterSale.getOrderItemId());
        if (item != null) {
            vo.setProductName(item.getProductName());
            vo.setProductImage(item.getProductImage());
            vo.setPrice(item.getPrice());
            vo.setQuantity(item.getQuantity());
            vo.setOrderId(item.getOrderId());
            Order order = orderMapper.selectById(item.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
            }
        }
        return vo;
    }
}

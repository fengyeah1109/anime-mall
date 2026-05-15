package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.AfterSale;
import com.anime.security.CurrentUserHolder;
import com.anime.service.AfterSaleService;
import com.anime.vo.AfterSaleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/after-sales")
@RequiredArgsConstructor
public class ApiAdminAfterSalesController {

    private final AfterSaleService afterSaleService;

    @GetMapping
    public Result<Page<AfterSaleVO>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "id", required = false) Long id
    ) {
        Page<AfterSale> afterSalePage = new Page<>(page, size);
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(AfterSale::getStatus, status);
        }
        if (id != null) {
            wrapper.eq(AfterSale::getId, id);
        }
        wrapper.orderByDesc(AfterSale::getApplyTime);
        Page<AfterSale> result = afterSaleService.page(afterSalePage, wrapper);
        
        List<AfterSaleVO> voList = afterSaleService.getAllAfterSales();
        List<Long> afterSaleIds = result.getRecords().stream().map(AfterSale::getId).collect(Collectors.toList());
        List<AfterSaleVO> filteredVOList = voList.stream().filter(vo -> afterSaleIds.contains(vo.getId())).collect(Collectors.toList());
        
        Page<AfterSaleVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(filteredVOList);
        return Result.success(voPage);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        Integer status = (Integer) request.get("status");
        String reply = (String) request.get("reply");
        Long adminId = CurrentUserHolder.getAdminId();
        afterSaleService.updateStatus(id, status, adminId, reply);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<AfterSaleVO> get(@PathVariable Long id) {
        AfterSaleVO afterSale = afterSaleService.getDetail(id);
        return Result.success(afterSale);
    }
}

package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.AfterSale;
import com.anime.security.CurrentUserHolder;
import com.anime.service.AfterSaleService;
import com.anime.vo.AfterSaleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/after-sale")
@RequiredArgsConstructor
public class ApiAfterSaleController {

    private final AfterSaleService afterSaleService;

    @PostMapping
    public Result<Void> create(@RequestBody AfterSale afterSale) {
        Long userId = CurrentUserHolder.getUserId();
        afterSale.setUserId(userId);
        afterSale.setStatus(0);
        afterSale.setApplyTime(LocalDateTime.now());
        afterSaleService.save(afterSale);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<AfterSaleVO>> list() {
        Long userId = CurrentUserHolder.getUserId();
        List<AfterSaleVO> afterSales = afterSaleService.getUserAfterSales(userId);
        return Result.success(afterSales);
    }

    @GetMapping("/{id}")
    public Result<AfterSaleVO> detail(@PathVariable Long id) {
        AfterSaleVO afterSale = afterSaleService.getDetail(id);
        return Result.success(afterSale);
    }
}

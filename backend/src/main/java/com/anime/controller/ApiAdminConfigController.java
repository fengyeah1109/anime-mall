package com.anime.controller;

import com.anime.common.Result;
import com.anime.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasRole('ADMIN')")
public class ApiAdminConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取推荐权重配置
     */
    @GetMapping("/recommend")
    public Result<Map<String, Double>> getRecommendConfig() {
        Map<String, Double> config = new HashMap<>();
        config.put("cfWeight", systemConfigService.getConfigValueAsDouble("recommend.cf_weight", 0.4));
        config.put("contentWeight", systemConfigService.getConfigValueAsDouble("recommend.content_weight", 0.4));
        config.put("hotWeight", systemConfigService.getConfigValueAsDouble("recommend.hot_weight", 0.2));
        return Result.success(config);
    }

    /**
     * 更新推荐权重配置
     */
    @PutMapping("/recommend")
    public Result<Boolean> updateRecommendConfig(@RequestBody Map<String, Double> config) {
        Double cfWeight = config.get("cfWeight");
        Double contentWeight = config.get("contentWeight");
        Double hotWeight = config.get("hotWeight");

        if (cfWeight == null || contentWeight == null || hotWeight == null) {
            return Result.fail("参数不完整");
        }

        // 验证权重总和是否为1（允许0.01的误差）
        double sum = cfWeight + contentWeight + hotWeight;
        if (Math.abs(sum - 1.0) > 0.01) {
            return Result.fail("权重总和必须为1");
        }

        systemConfigService.updateConfig("recommend.cf_weight", cfWeight.toString());
        systemConfigService.updateConfig("recommend.content_weight", contentWeight.toString());
        systemConfigService.updateConfig("recommend.hot_weight", hotWeight.toString());

        return Result.success(true);
    }
}
package com.anime.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.anime.entity.SystemConfig;

import java.util.Map;

public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 根据配置键获取配置值
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键获取配置值（转换为Double）
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Double getConfigValueAsDouble(String configKey, Double defaultValue);

    /**
     * 获取所有配置项
     * @return 配置项映射
     */
    Map<String, String> getAllConfigs();

    /**
     * 更新配置项
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否更新成功
     */
    boolean updateConfig(String configKey, String configValue);
}
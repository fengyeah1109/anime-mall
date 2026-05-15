package com.anime.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.anime.entity.SystemConfig;
import com.anime.mapper.SystemConfigMapper;
import com.anime.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public String getConfigValue(String configKey) {
        SystemConfig config = this.lambdaQuery()
                .eq(SystemConfig::getConfigKey, configKey)
                .one();
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public Double getConfigValueAsDouble(String configKey, Double defaultValue) {
        String value = getConfigValue(configKey);
        try {
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = this.list();
        Map<String, String> configMap = new HashMap<>();
        for (SystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        return configMap;
    }

    @Override
    public boolean updateConfig(String configKey, String configValue) {
        SystemConfig config = this.lambdaQuery()
                .eq(SystemConfig::getConfigKey, configKey)
                .one();
        if (config != null) {
            config.setConfigValue(configValue);
            return this.updateById(config);
        } else {
            // 如果配置项不存在，创建新的
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            return this.save(newConfig);
        }
    }
}
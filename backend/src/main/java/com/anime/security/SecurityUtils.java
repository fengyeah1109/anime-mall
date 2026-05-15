package com.anime.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * 获取当前用户ID
     * @return 用户ID，如果未登录返回null
     */
    public static Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof AuthPrincipal) {
                return ((AuthPrincipal) principal).getUserId();
            }
        } catch (Exception e) {
            // 未登录或获取失败
        }
        return null;
    }
}
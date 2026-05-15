package com.anime.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthContext {

    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new com.anime.exception.BusinessException(401, "unauthorized");
        }
        return principal.getUserId();
    }

    public static String getRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new com.anime.exception.BusinessException(401, "unauthorized");
        }
        return principal.getRole();
    }
}


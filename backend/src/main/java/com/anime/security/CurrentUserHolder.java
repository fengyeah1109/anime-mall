package com.anime.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUserHolder {

    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal) {
            return ((AuthPrincipal) authentication.getPrincipal()).getUserId();
        }
        return null;
    }

    public static Long getAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal) {
            return ((AuthPrincipal) authentication.getPrincipal()).getUserId();
        }
        return null;
    }

    public static String getRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal) {
            return ((AuthPrincipal) authentication.getPrincipal()).getRole();
        }
        return null;
    }
}

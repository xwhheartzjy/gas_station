package org.codec.common;

import org.codec.entity.SysUser;
import org.springframework.security.core.Authentication;

public class RequestContext {

    private static final ThreadLocal<SysUser> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(SysUser authentication) {
        currentUser.set(authentication);
    }

    public static SysUser getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}
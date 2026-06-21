package com.uber.springserver.config;

import com.uber.springserver.models.Captain;
import com.uber.springserver.models.User;
import jakarta.servlet.http.HttpServletRequest;

public final class RequestContext {

    public static final String USER_ATTR = "authenticatedUser";
    public static final String CAPTAIN_ATTR = "authenticatedCaptain";

    private RequestContext() {
    }

    public static User getUser(HttpServletRequest request) {
        return (User) request.getAttribute(USER_ATTR);
    }

    public static Captain getCaptain(HttpServletRequest request) {
        return (Captain) request.getAttribute(CAPTAIN_ATTR);
    }
}

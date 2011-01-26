package com.wadpam.ricotta.gae;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class GoogleAccountsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final Logger LOG = LoggerFactory.getLogger(GoogleAccountsAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        LOG.debug("principal={}", request.getUserPrincipal());
        UserService userService = UserServiceFactory.getUserService();
        response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
    }

}

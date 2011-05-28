package com.wadpam.ricotta.gae;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.appengine.api.users.User;
import com.wadpam.ricotta.dao.AppUserDao;
import com.wadpam.ricotta.domain.AppUser;

public class GoogleAccountsAuthenticationProvider implements AuthenticationProvider {
    static final Logger           LOG       = LoggerFactory.getLogger(GoogleAccountsAuthenticationProvider.class);

    static final GrantedAuthority ROLE_USER = new GrantedAuthorityImpl("USER");
    private AppUserDao            appUserDao;
    private HandlerInterceptor    handlerInterceptor;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        User googleUser = (User) authentication.getPrincipal();

        AppUser user = appUserDao.findByUserId(googleUser.getUserId());
        LOG.debug("Loaded appUser {} by {}", user, googleUser.getUserId());

        if (user == null) {
            // User not in registry. Needs to register
            user = new AppUser();
            user.setEmail(googleUser.getEmail());
            user.setUserId(googleUser.getUserId());
            appUserDao.persist(user);
        }

        // FIXME: load ROLEs
        List<GrantedAuthority> authorities = Arrays.asList(ROLE_USER);
        return new GaeUserAuthentication(user, authorities, authentication.getDetails());
    }

    public final boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setAppUserDao(AppUserDao appUserDao) {
        this.appUserDao = appUserDao;
    }

    public void setHandlerInterceptor(HandlerInterceptor handlerInterceptor) {
        this.handlerInterceptor = handlerInterceptor;
    }

    public HandlerInterceptor getHandlerInterceptor() {
        return handlerInterceptor;
    }

}

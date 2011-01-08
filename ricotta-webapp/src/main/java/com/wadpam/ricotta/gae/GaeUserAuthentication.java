package com.wadpam.ricotta.gae;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.wadpam.ricotta.domain.AppUser;

public class GaeUserAuthentication implements Authentication {

    private final AppUser                user;
    private final List<GrantedAuthority> authorities;
    private final Object                 details;

    public GaeUserAuthentication(AppUser user, List<GrantedAuthority> authorities, Object details) {
        this.user = user;
        this.authorities = authorities;
        this.details = details;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        // TODO Auto-generated method stub
        return user.getUserId();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    }

}

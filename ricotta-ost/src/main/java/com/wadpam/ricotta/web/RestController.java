/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.ricotta.web;

import com.google.appengine.api.users.UserServiceFactory;
import com.wadpam.ricotta.dao.UberDaoBean;
import com.wadpam.ricotta.domain.ProjUser;
import com.wadpam.ricotta.domain.Role;
import com.wadpam.ricotta.model.v10.Me10;
import com.wadpam.ricotta.model.v10.Proj10;
import com.wadpam.ricotta.model.v10.Tokn10;
import java.security.Principal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author f94os
 */
@Controller
public class RestController {
    
    static final Logger LOG = LoggerFactory.getLogger(RestController.class);
    
    private UberDaoBean uberDao;
    
    @RequestMapping(value="project/v10", method= RequestMethod.POST)
    public ResponseEntity<List<Proj10>> createProject(Principal principal,
            @RequestParam String name) {
        String username = "Googlebot";
        if (null != principal) {
            username = principal.getName();
        }
        
        try {
            Object projKey = uberDao.createProj(name, username);
            
            // create a trunk branch
            Object branch = uberDao.createBranch(projKey, ProjectHandlerInterceptor.NAME_TRUNK, "The main branch");

        }
        catch (IllegalArgumentException alreadyExists) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        
        return new ResponseEntity<List<Proj10>>(uberDao.getProjects(username), HttpStatus.OK);
    }
    
    @RequestMapping(value="project/v10", method= RequestMethod.GET)
    public ResponseEntity<List<Proj10>> getProjects(Principal principal) {
        String username = "Googlebot";
        if (null != principal) {
            username = principal.getName();
        }
        return new ResponseEntity<List<Proj10>>(uberDao.getProjects(username), HttpStatus.OK);
    }
    
    @RequestMapping(value="role/v10", method= RequestMethod.GET)
    public ResponseEntity<List<Role>> getRoles() {
        return new ResponseEntity<List<Role>>(uberDao.getRoles(), HttpStatus.OK);
    }

    @RequestMapping(value="project/v10/{name}/token", method= RequestMethod.GET)
    public ResponseEntity<Proj10> getTokens(Principal principal,
            @PathVariable String name) {
        String username = "Googlebot";
        if (null != principal) {
            username = principal.getName();
        }
        final Proj10 body = uberDao.getTokens(username, name, ProjectHandlerInterceptor.NAME_TRUNK);
        return new ResponseEntity(body, HttpStatus.OK);
    }
    
    @RequestMapping(value="me/v10", method= RequestMethod.GET)
    public ResponseEntity<Me10> me(Principal principal,
            @RequestParam(value="path", defaultValue="/index.html") String path) {
        final Me10 me = new Me10();
        LOG.debug("path={}, principal is {}", path, principal);
        if (null == principal) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Login-Logout-URL", UserServiceFactory.getUserService().createLoginURL(path));
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }
        me.setUrl(UserServiceFactory.getUserService().createLogoutURL(path));
        me.setEmail(principal.getName());
        return new ResponseEntity<Me10>(me, HttpStatus.OK);
    }
    
    @RequestMapping(value="project/v10/{projectName}/user/{keyString}", method= RequestMethod.POST)
    public ResponseEntity<ProjUser> updateUser(
            @PathVariable String keyString,
            @RequestParam Long role) {
        final ProjUser body = uberDao.updateUser(keyString, role);
        if (null == body) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(body, HttpStatus.OK);
    }

    @RequestMapping(value="project/v10/{projectName}/token/{tokenId}", method= RequestMethod.POST, params={"name", "description"})
    public ResponseEntity<Tokn10> updateToken(
            @PathVariable String projectName, 
            @PathVariable Long tokenId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String context,
            @RequestParam String subsets,
            @RequestParam(value="separator", defaultValue=",") String separator) {
        final String[] subs = subsets.split(separator);
        final Tokn10 body = uberDao.updateToken(projectName, ProjectHandlerInterceptor.NAME_TRUNK, tokenId, name, description, context, subs);
        if (null == body) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(body, HttpStatus.OK);
    }

    @RequestMapping(value="project/v10/{projectName}/token/{tokenId}", method= RequestMethod.POST, params={"langCode"})
    public ResponseEntity<Tokn10> updateTranslation(
            @PathVariable String projectName, 
            @PathVariable Long tokenId,
            @RequestParam String langCode,
            @RequestParam(value="value", required=false) String value) {
        
        final List<String> body = uberDao.updateTrans(projectName, ProjectHandlerInterceptor.NAME_TRUNK, tokenId, langCode, value);
        if (null == body) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    public void setUberDao(UberDaoBean uberDao) {
        this.uberDao = uberDao;
    }
    
}

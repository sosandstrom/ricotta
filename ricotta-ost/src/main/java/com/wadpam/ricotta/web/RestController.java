/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.ricotta.web;

import com.wadpam.ricotta.dao.UberDaoBean;
import com.wadpam.ricotta.model.v10.Proj10;
import com.wadpam.ricotta.model.v10.Tokn10;
import java.security.Principal;
import java.util.List;
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
    
    private UberDaoBean uberDao;
    
    @RequestMapping(value="project/v10", method= RequestMethod.GET)
    public ResponseEntity<List<Proj10>> getProjects(Principal principal) {
        String username = "Googlebot";
        if (null != principal) {
            username = principal.getName();
        }
        return new ResponseEntity<List<Proj10>>(uberDao.getProjects(username), HttpStatus.OK);
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
    
    @RequestMapping(value="project/v10/{projectName}/token/{tokenId}", method= RequestMethod.POST)
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
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(body, HttpStatus.OK);
    }

    public void setUberDao(UberDaoBean uberDao) {
        this.uberDao = uberDao;
    }
    
}

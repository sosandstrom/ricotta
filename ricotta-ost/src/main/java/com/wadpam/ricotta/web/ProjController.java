package com.wadpam.ricotta.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.domain.Proj;

@Controller
@RequestMapping(value = "/proj")
public class ProjController extends AbstractDaoController {
    static final Logger LOG = LoggerFactory.getLogger(ProjController.class);

    @RequestMapping(value = "index.html")
    public String getProjs(Model model, HttpServletRequest request) {
        // projects
        String username = "Googlebot";
        if (null != request.getUserPrincipal()) {
            username = request.getUserPrincipal().getName();
        }
        // owned projects first
        List<Proj> projects = new ArrayList<Proj>(projDao.findByOwner(username));

        // then add projects where user
        for(String projName : projDao.findAllKeys()) {
            Key projKey = KeyFactory.createKey(Proj.class.getSimpleName(), projName);
            if (null != projUserDao.findByPrimaryKey(projKey, username)) {
                projects.add(projDao.findByPrimaryKey(projName));
            }
        }
        model.addAttribute("projs", projects);
        return "projs";
    }

}

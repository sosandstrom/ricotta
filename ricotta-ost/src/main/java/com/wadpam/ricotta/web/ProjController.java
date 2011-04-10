package com.wadpam.ricotta.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.domain.Branch;
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

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String createProj() {
        return "createProject";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String createProj(HttpServletRequest request, @ModelAttribute("project") Proj project) {
        Proj existing = projDao.findByPrimaryKey(project.getName());
        if (null != existing) {
            // TODO: validationErrors
            LOG.warn("Project {} already exists", existing);
            return "redirect:create.html";
        }

        // fetch the principal
        project.setOwner(request.getUserPrincipal().getName());
        projDao.persist(project);

        // create the trunk
        Branch trunk = new Branch();
        trunk.setProject((Key) project.getPrimaryKey());
        trunk.setName(ProjectHandlerInterceptor.NAME_TRUNK);

        return "redirect:/proj/" + project.getName() + "/branch/" + trunk.getName() + '/';
    }

}

package com.wadpam.ricotta.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.domain.Branch;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.velocity.Encoder;

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

    @RequestMapping(value = "{projName}/action.html", method = RequestMethod.GET)
    public String actionProjConfirm(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "action") String action) throws ResourceNotFoundException, ParseErrorException, Exception {
        String viewName = null;
        if ("Delete project".equals(action)) {
            viewName = "confirm";
        }
        else if ("Export project".equals(action)) {
            final VelocityContext model = new VelocityContext();
            model.put("encoder", new Encoder());
            final String projName = (String) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJNAME);
            final Proj proj = projDao.findByPrimaryKey(projName);
            model.put("p", proj);
            model.put("uberDao", uberDao);

            GenerateController.renderTemplate("ricotta-export-proj", model, response, "text/xml; charset=UTF-8");
        }
        return viewName;
    }

    @RequestMapping(value = "{projName}/action.html", method = RequestMethod.POST)
    public String actionProjConfirmed(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "confirmed") String confirmed) throws ResourceNotFoundException, ParseErrorException, Exception {
        String viewName = null;
        final Key projKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJKEY);
        LOG.debug("action is {}", confirmed);
        if ("Delete project".equals(confirmed)) {
            uberDao.deleteProj(projKey);
            viewName = "redirect:/proj/index.html";
        }
        return viewName;
    }
}

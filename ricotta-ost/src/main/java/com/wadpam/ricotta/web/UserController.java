package com.wadpam.ricotta.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wadpam.ricotta.dao.ProjectUserDao;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.ProjectUser;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/users")
public class UserController {
    static final Logger    LOGGER = LoggerFactory.getLogger(UserController.class);

    private ProjectUserDao projectUserDao;

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String getAddUserForm(HttpServletRequest request, Model model, @PathVariable String projectName) {
        final Project project = (Project) request.getAttribute("project");
        model.addAttribute("project", project);

        return "createUser";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String postProjectLanguage(HttpServletRequest request, @PathVariable String projectName,
            @ModelAttribute("projectUser") ProjectUser projectUser) throws IOException {
        final Project project = (Project) request.getAttribute("project");
        projectUser.setProject(project.getKey());
        projectUserDao.persist(projectUser);

        return "redirect:/projects/" + projectName + '/';
    }

    public void setProjectUserDao(ProjectUserDao projectUserDao) {
        this.projectUserDao = projectUserDao;
    }

}

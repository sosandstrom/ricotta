package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wadpam.ricotta.dao.ArtifactDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.ProjectUserDao;
import com.wadpam.ricotta.dao.TokenDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.ProjectUser;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.model.ProjectLanguageModel;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects")
public class ProjectController {
    static final Logger    LOGGER = LoggerFactory.getLogger(ProjectController.class);

    private ProjectDao     projectDao;

    private ProjectUserDao projectUserDao;

    private UberDao        uberDao;

    private ArtifactDao    artifactDao;

    private TokenDao       tokenDao;

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String getProjects(HttpServletRequest request, Model model) {
        LOGGER.debug("get projects list {}, {}", projectDao, request.getUserPrincipal());

        final String user = request.getUserPrincipal().getName();
        List<Project> projects = new ArrayList<Project>(projectDao.findByOwner(user));
        for(ProjectUser pu : projectUserDao.findByUser(user)) {
            projects.add(projectDao.findByPrimaryKey(pu.getProject()));
        }
        model.addAttribute("projects", projects);
        return "projects";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String createProject() {
        LOGGER.debug("display create project form");
        return "createProject";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String postProject(HttpServletRequest request, @ModelAttribute("project") Project project) throws IOException {
        LOGGER.debug("create project");

        // fetch the principal
        project.setOwner(request.getUserPrincipal().getName());

        projectDao.persist(project);

        return "redirect:/projects/" + project.getName() + '/';
    }

    @RequestMapping(value = "{projectName}/index.html", method = RequestMethod.GET)
    public String getProject(HttpServletRequest request, Model model, @PathVariable String projectName) {
        LOGGER.debug("get project");
        // TODO: check project role

        // fetch and add project details
        Project project = projectDao.findByName(projectName);
        model.addAttribute("project", project);

        // fetch and add languages for this project
        List<ProjectLanguageModel> languages = uberDao.loadProjectLanguages(project.getKey());
        model.addAttribute("languages", languages);

        // fetch and add artifacts for this project
        model.addAttribute("artifacts", artifactDao.findByProject(project.getKey()));

        // fetch and add users for this project
        model.addAttribute("users", projectUserDao.findByProject(project.getKey()));

        // fetch and add tokens for this project
        List<Token> tokens = tokenDao.findByProject(project.getKey(), true);
        model.addAttribute("tokens", tokens);

        return "project";
    }

    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public void setTokenDao(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public void setUberDao(UberDao uberDao) {
        this.uberDao = uberDao;
    }

    public void setArtifactDao(ArtifactDao artifactDao) {
        this.artifactDao = artifactDao;
    }

    public void setProjectUserDao(ProjectUserDao projectUserDao) {
        this.projectUserDao = projectUserDao;
    }

    public ProjectUserDao getProjectUserDao() {
        return projectUserDao;
    }

}

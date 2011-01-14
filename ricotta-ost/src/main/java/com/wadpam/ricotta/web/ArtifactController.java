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

import com.wadpam.ricotta.dao.ArtifactDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.domain.Artifact;
import com.wadpam.ricotta.domain.Project;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/artifacts")
public class ArtifactController {
    static final Logger LOGGER = LoggerFactory.getLogger(ArtifactController.class);

    private ProjectDao  projectDao;

    private ArtifactDao artifactDao;

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String getProjectLanguageForm(Model model, @PathVariable String projectName) {
        // TODO: check project role
        Project project = projectDao.findByName(projectName);
        model.addAttribute("project", project);

        return "createArtifact";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String postProjectLanguage(HttpServletRequest request, @PathVariable String projectName,
            @ModelAttribute("artifact") Artifact artifact) throws IOException {
        LOGGER.debug("create artifact");
        // TODO: check project role

        Project project = projectDao.findByName(projectName);
        artifact.setProject(project.getKey());
        artifactDao.persist(artifact);

        return "redirect:/projects/" + projectName + '/';
    }

    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public void setArtifactDao(ArtifactDao artifactDao) {
        this.artifactDao = artifactDao;
    }

}

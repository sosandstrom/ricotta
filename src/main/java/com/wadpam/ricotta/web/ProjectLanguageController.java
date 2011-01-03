package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.dao.LanguageDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.ProjectLanguageDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.ProjectLanguage;
import com.wadpam.ricotta.model.ProjectLanguageModel;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/languages")
public class ProjectLanguageController {
    static final Logger        LOGGER = LoggerFactory.getLogger(ProjectLanguageController.class);

    private ProjectDao         projectDao;

    private LanguageDao        languageDao;

    private ProjectLanguageDao projectLanguageDao;

    private UberDao            uberDao;

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String getProjectLanguageForm(Model model, @PathVariable String projectName) {
        // TODO: check project role
        Project project = projectDao.findByName(projectName);
        model.addAttribute("project", project);

        // for parent list
        List<ProjectLanguageModel> projectLanguages = uberDao.loadProjectLanguages(project.getKey());
        model.addAttribute("parentLanguages", projectLanguages);

        // for new project language list
        Set<Key> languageKeys = new HashSet<Key>();
        for(ProjectLanguageModel plm : projectLanguages) {
            languageKeys.add(plm.getLanguage().getKey());
        }

        List<Language> languages = languageDao.findAll();
        List<Language> availableLanguages = new ArrayList<Language>();
        for(Language l : languages) {
            if (false == languageKeys.contains(l.getKey())) {
                availableLanguages.add(l);
            }
        }
        model.addAttribute("languages", availableLanguages);

        return "createProjectLanguage";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String postProjectLanguage(HttpServletRequest request, @PathVariable String projectName,
            @ModelAttribute("projectLanguage") ProjectLanguage projectLanguage) throws IOException {
        LOGGER.debug("create projectLanguage");
        // TODO: check project role

        Project project = projectDao.findByName(projectName);
        projectLanguage.setProject(project.getKey());
        projectLanguageDao.persist(projectLanguage);

        return "redirect:/projects/" + projectName;
    }

    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public void setLanguageDao(LanguageDao languageDao) {
        this.languageDao = languageDao;
    }

    public void setProjectLanguageDao(ProjectLanguageDao projectLanguageDao) {
        this.projectLanguageDao = projectLanguageDao;
    }

    public void setUberDao(UberDao uberDao) {
        this.uberDao = uberDao;
    }

}

package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wadpam.ricotta.dao.LanguageDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.ProjectLanguageDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.model.TranslationModel;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/languages/{languageCode}/translations")
public class TranslationController {
    static final Logger        LOGGER = LoggerFactory.getLogger(TranslationController.class);

    private ProjectDao         projectDao;

    private LanguageDao        languageDao;

    private ProjectLanguageDao projectLanguageDao;

    private UberDao            uberDao;

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String getProjectLanguageForm(Model model, @PathVariable String projectName, @PathVariable String languageCode) {
        // TODO: check project role
        Project project = projectDao.findByName(projectName);
        model.addAttribute("project", project);

        Language language = languageDao.findByCode(languageCode);
        model.addAttribute("language", language);

        List<TranslationModel> translations = uberDao.loadTranslations(project.getKey(), language.getKey());
        model.addAttribute("translations", translations);

        return "translations";
    }

    @RequestMapping(value = "index.html", method = RequestMethod.POST)
    public String postProjectLanguage(HttpServletRequest request, @PathVariable String projectName,
            @PathVariable String languageCode) throws IOException {
        LOGGER.debug("post translations");
        // TODO: check project role

        Project project = projectDao.findByName(projectName);

        return "redirect:index.html";
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

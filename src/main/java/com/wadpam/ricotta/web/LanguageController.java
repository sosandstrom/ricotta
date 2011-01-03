package com.wadpam.ricotta.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wadpam.ricotta.dao.LanguageDao;
import com.wadpam.ricotta.domain.Language;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/languages/")
public class LanguageController {
    static final Logger LOGGER = LoggerFactory.getLogger(LanguageController.class);

    private LanguageDao languageDao;

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String getLanguages(HttpServletRequest request, Model model) {
        LOGGER.debug("get languages list");

        model.addAttribute("languages", languageDao.findAll());
        return "languages";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String createLanguage() {
        LOGGER.debug("display create language form");
        return "createLanguage";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String postLanguage(HttpServletRequest request, @ModelAttribute("language") Language language) throws IOException {
        LOGGER.debug("create language");

        languageDao.persist(language);

        return "redirect:/languages";
    }

    public void setLanguageDao(LanguageDao languageDao) {
        this.languageDao = languageDao;
    }

}

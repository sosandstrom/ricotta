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

import com.wadpam.ricotta.domain.Lang;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("lang")
public class LangController extends AbstractDaoController {
    static final Logger LOGGER = LoggerFactory.getLogger(LangController.class);

    @RequestMapping(value = {"index.html", ""}, method = RequestMethod.GET)
    public String getLangss(HttpServletRequest request, Model model) {
        LOGGER.debug("get languages");

        model.addAttribute("languages", langDao.findAll());
        return "languages";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String createLanguage() {
        LOGGER.debug("display create language form");
        return "createLanguage";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String postLanguage(HttpServletRequest request, @ModelAttribute("language") Lang lang) throws IOException {
        LOGGER.debug("create lang");

        langDao.persist(lang);

        return "redirect:index.html";
    }

}

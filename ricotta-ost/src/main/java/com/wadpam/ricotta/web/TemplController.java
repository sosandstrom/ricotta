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

import com.wadpam.ricotta.domain.Template;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/templ/")
public class TemplController extends AbstractDaoController {
    static final Logger LOGGER = LoggerFactory.getLogger(TemplController.class);

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String getTemplates(HttpServletRequest request, Model model) {
        LOGGER.debug("get templ list");

        model.addAttribute("malls", templateDao.findAll());
        return "malls";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String getTemplateForm() {
        LOGGER.debug("display create mall form");
        return "createMall";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String createTemplate(HttpServletRequest request, @ModelAttribute("mall") Template templ) throws IOException {
        LOGGER.debug("create mall");

        templateDao.persist(templ);

        return "redirect:index.html";
    }

    @RequestMapping(value = "/{templName}/index.html", method = RequestMethod.GET)
    public String editTemplate(Model model, @PathVariable String templName) {
        LOGGER.debug("display create templ form");
        Template existing = templateDao.findByPrimaryKey(templName);
        model.addAttribute("mall", existing);
        return "editMall";
    }

    @RequestMapping(value = "/{templName}/index.html", method = RequestMethod.POST)
    public String updateTemplate(HttpServletRequest request, @ModelAttribute("mall") Template templ,
            @PathVariable String templName) throws IOException {
        LOGGER.debug("save mall");

        Template existing = templateDao.findByPrimaryKey(templName);
        if (null == existing) {
            throw new IllegalArgumentException("No such template " + templName);
        }
        templ.setName(templName);
        templateDao.update(templ);

        return "redirect:index.html";
    }

}

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

import com.wadpam.ricotta.dao.MallDao;
import com.wadpam.ricotta.domain.Mall;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/templates/")
public class MallController {
    static final Logger LOGGER = LoggerFactory.getLogger(MallController.class);

    private MallDao     mallDao;

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String getMalls(HttpServletRequest request, Model model) {
        LOGGER.debug("get malls list");

        model.addAttribute("malls", mallDao.findAll());
        return "malls";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String getMallForm() {
        LOGGER.debug("display create mall form");
        return "createMall";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String createMall(HttpServletRequest request, @ModelAttribute("mall") Mall mall) throws IOException {
        LOGGER.debug("create mall");

        mallDao.persist(mall);

        return "redirect:/templates/";
    }

    @RequestMapping(value = "/{mallName}/index.html", method = RequestMethod.GET)
    public String editMallForm(Model model, @PathVariable String mallName) {
        LOGGER.debug("display create mall form");
        Mall existing = mallDao.findByName(mallName);
        model.addAttribute("mall", existing);
        return "editMall";
    }

    @RequestMapping(value = "/{mallName}/index.html", method = RequestMethod.POST)
    public String saveMall(HttpServletRequest request, @ModelAttribute("mall") Mall mall, @PathVariable String mallName)
            throws IOException {
        LOGGER.debug("save mall");

        Mall existing = mallDao.findByName(mallName);
        if (null == existing) {
            throw new IllegalArgumentException("No such template " + mallName);
        }
        mall.setKey(existing.getKey());
        mall.setName(mallName);
        mallDao.update(mall);

        return "redirect:/templates/";
    }

    public void setMallDao(MallDao mallDao) {
        this.mallDao = mallDao;
    }

}

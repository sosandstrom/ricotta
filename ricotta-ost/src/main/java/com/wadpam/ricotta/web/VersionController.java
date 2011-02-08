package com.wadpam.ricotta.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.dao.VersionDao;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.Version;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/versions")
public class VersionController {
    static final Logger     LOGGER       = LoggerFactory.getLogger(VersionController.class);

    static final DateFormat DATUM_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm z");

    private UberDao         uberDao;

    private VersionDao      versionDao;

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String getProjectLanguageForm(HttpServletRequest request, Model model) {
        final Project project = (Project) request.getAttribute("project");
        model.addAttribute("project", project);

        return "createVersion";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String postVersion(HttpServletRequest request, @ModelAttribute("version") Version version) throws IOException {
        LOGGER.debug("create version");

        final Project project = (Project) request.getAttribute("project");
        final Version from = (Version) request.getAttribute("version");
        version.setProject(project.getKey());
        version.setDatum(DATUM_FORMAT.format(new Date()));
        versionDao.persist(version);

        uberDao.cloneVersion(project, from.getKey(), version);

        return "redirect:/projects/" + project.getName() + '/';
    }

    @RequestMapping(value = "deleteVersions.html", method = RequestMethod.POST)
    public String deleteVersion(HttpServletRequest request) throws IOException {
        LOGGER.debug("delete version");

        final Project project = (Project) request.getAttribute("project");
        for(String vk : request.getParameterValues("versions")) {
            uberDao.deleteVersion(project, vk);
        }

        return "redirect:/projects/" + project.getName() + '/';
    }

    public void setUberDao(UberDao uberDao) {
        this.uberDao = uberDao;
    }

    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

}

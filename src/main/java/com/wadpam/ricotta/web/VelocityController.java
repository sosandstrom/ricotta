package com.wadpam.ricotta.web;

import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wadpam.ricotta.dao.LanguageDao;
import com.wadpam.ricotta.dao.MallDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.ProjectLanguageDao;
import com.wadpam.ricotta.dao.TranslationDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.Mall;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.model.TranslationModel;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/languages/{languageCode}/templates/{templateName}")
public class VelocityController {
    static final Logger        LOG = LoggerFactory.getLogger(VelocityController.class);

    private ProjectDao         projectDao;

    private LanguageDao        languageDao;

    private MallDao            mallDao;

    private ProjectLanguageDao projectLanguageDao;

    private TranslationDao     translationDao;

    private UberDao            uberDao;

    public void init() throws Exception {

        ExtendedProperties configuration = new ExtendedProperties();
        LOG.info("Initializing Velocity");

        final Properties p = new Properties();
        // p.setProperty("resource.loader", "file, class");
        p.setProperty("resource.loader", "dao");

        // p.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        // p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        p.setProperty("dao.resource.loader.description", "Ricotta DAO Resource Loader");
        p.setProperty("dao.resource.loader.class", "com.wadpam.ricotta.velocity.DaoResourceLoader");

        // p.setProperty("file.resource.loader.description", "Velocity File Resource Loader");
        // p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        // p.setProperty("file.resource.loader.path", templateFolder);
        // p.setProperty("file.resource.loader.cache", "true");
        // p.setProperty("file.resource.loader.modificationCheckInterval", "0");
        Velocity.init(p);
    }

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String getProjectLanguageForm(@PathVariable String projectName, @PathVariable String languageCode,
            @PathVariable String templateName, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        LOG.debug("rendering template " + templateName);
        // TODO: check project role
        final VelocityContext model = new VelocityContext();
        Project project = projectDao.findByName(projectName);
        model.put("project", project);

        Language language = languageDao.findByCode(languageCode);
        model.put("language", language);

        List<TranslationModel> translations = uberDao.loadTranslations(project.getKey(), language.getKey());
        model.put("translations", translations);

        Mall mall = mallDao.findByName(templateName);
        model.put("mall", mall);

        final PrintWriter writer = response.getWriter();
        Template template = Velocity.getTemplate(templateName);
        template.merge(model, writer);
        writer.close();
        response.setStatus(HttpServletResponse.SC_OK);
        return null;
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

    public void setTranslationDao(TranslationDao translationDao) {
        this.translationDao = translationDao;
    }

    public void setMallDao(MallDao mallDao) {
        this.mallDao = mallDao;
    }

}

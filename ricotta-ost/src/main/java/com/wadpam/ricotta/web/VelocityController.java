package com.wadpam.ricotta.web;

import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
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

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.dao.ArtifactDao;
import com.wadpam.ricotta.dao.LanguageDao;
import com.wadpam.ricotta.dao.MallDao;
import com.wadpam.ricotta.dao.ProjectLanguageDao;
import com.wadpam.ricotta.dao.TranslationDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.domain.Artifact;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.Mall;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.Version;
import com.wadpam.ricotta.model.TranslationModel;
import com.wadpam.ricotta.velocity.Encoder;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/languages/{languageCode}/templates/{templateName}")
public class VelocityController {
    static final Logger        LOG = LoggerFactory.getLogger(VelocityController.class);

    private LanguageDao        languageDao;

    private MallDao            mallDao;

    private ProjectLanguageDao projectLanguageDao;

    private ArtifactDao        artifactDao;

    private TranslationDao     translationDao;

    private UberDao            uberDao;

    public void init() throws Exception {

        ExtendedProperties configuration = new ExtendedProperties();
        LOG.info("Initializing Velocity");

        final Properties p = new Properties();
        p.setProperty("resource.loader", "dao");

        p.setProperty("dao.resource.loader.description", "Ricotta DAO Resource Loader");
        p.setProperty("dao.resource.loader.class", "com.wadpam.ricotta.velocity.DaoResourceLoader");

        Velocity.init(p);
    }

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String renderTemplate(HttpServletRequest request, @PathVariable String projectName, @PathVariable String languageCode,
            @PathVariable String templateName, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        return renderTemplateByArtifact(request, projectName, languageCode, templateName, null, response);
    }

    @RequestMapping(value = "/artifacts/{artifactName}/index.html", method = RequestMethod.GET)
    public String renderTemplateByArtifact(HttpServletRequest request, @PathVariable String projectName,
            @PathVariable String languageCode, @PathVariable String templateName, @PathVariable String artifactName,
            HttpServletResponse response) throws ResourceNotFoundException, ParseErrorException, Exception {
        LOG.debug("rendering template " + templateName);
        final VelocityContext model = new VelocityContext();
        model.put("encoder", new Encoder());

        final Project project = (Project) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJECT);
        model.put(ProjectHandlerInterceptor.KEY_PROJECT, project);
        final Version version = (Version) request.getAttribute(ProjectHandlerInterceptor.KEY_VERSION);

        Language language = languageDao.findByCode(languageCode);
        if (null == language) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such language " + languageCode);
            return null;
        }
        model.put("language", language);

        Key artifactKey = null;
        if (null != artifactName) {
            Artifact artifact = artifactDao.findByNameProject(artifactName, project.getKey());
            if (null == artifact) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such artifact " + artifactName);
                return null;
            }
            model.put("artifact", artifact);
            artifactKey = artifact.getKey();
        }

        List<TranslationModel> translations = uberDao.loadTranslations(project.getKey(), version.getKey(), language.getKey(),
                artifactKey);
        model.put("translations", translations);

        Mall mall = mallDao.findByName(templateName);
        if (null == mall) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such template " + templateName);
            return null;
        }
        model.put("mall", mall);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain; charset=UTF-8"); // mall.getMimeType());
        final PrintWriter writer = response.getWriter();
        Template template = Velocity.getTemplate(templateName);
        template.merge(model, writer);
        writer.close();
        response.setStatus(HttpServletResponse.SC_OK);
        return null;
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

    public void setArtifactDao(ArtifactDao artifactDao) {
        this.artifactDao = artifactDao;
    }

}

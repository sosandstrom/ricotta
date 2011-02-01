package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.dao.LanguageDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.ProjectLanguageDao;
import com.wadpam.ricotta.dao.TokenDao;
import com.wadpam.ricotta.dao.TranslationDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Translation;
import com.wadpam.ricotta.model.TranslationModel;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/languages/{languageCode}/translations")
public class TranslationController {
    static final Logger        LOG                = LoggerFactory.getLogger(TranslationController.class);

    static final String        PREFIX_DESCRIPTION = "description.";

    static final String        PREFIX_TOKEN       = "token.";

    private ProjectDao         projectDao;

    private LanguageDao        languageDao;

    private ProjectLanguageDao projectLanguageDao;

    private TokenDao           tokenDao;

    private TranslationDao     translationDao;

    private UberDao            uberDao;

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String getProjectLanguageForm(HttpServletRequest request, Model model, @PathVariable String projectName,
            @PathVariable String languageCode) {
        final Project project = (Project) request.getAttribute("project");
        model.addAttribute("project", project);

        Language language = languageDao.findByCode(languageCode);
        model.addAttribute("language", language);

        List<TranslationModel> translations = uberDao.loadTranslations(project.getKey(), language.getKey(), null);
        model.addAttribute("translations", translations);

        return "translations";
    }

    @RequestMapping(value = "index.html", method = RequestMethod.POST)
    public String postProjectLanguage(HttpServletRequest request, @PathVariable String projectName,
            @PathVariable String languageCode) throws IOException {
        LOG.debug("post translations");
        final Project project = (Project) request.getAttribute("project");
        Language language = languageDao.findByCode(languageCode);

        String name, value;
        Key key;
        Translation t;
        Token token;
        // iterate all posted parameters (descriptions and translations)
        for(@SuppressWarnings("unchecked")
        Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            name = e.nextElement();
            value = request.getParameter(name);
            LOG.debug("{} = {}", name, value);
            try {
                if (name.startsWith(PREFIX_DESCRIPTION)) {
                    key = KeyFactory.stringToKey(name.substring(PREFIX_DESCRIPTION.length()));
                    token = tokenDao.findByPrimaryKey(key);
                    if (false == value.equals(token.getDescription())) {
                        token.setDescription(value);
                        tokenDao.update(token);
                        LOG.debug("updated description for {} to {}", token.getName(), value);
                    }
                }
                else {
                    key = KeyFactory.stringToKey(name);
                    t = null;
                    LOG.debug("field key kind for {} is {}", key.toString(), key.getKind());
                    if (Translation.class.getSimpleName().equals(key.getKind())) {
                        // update or delete existing translation
                        t = translationDao.findByPrimaryKey(key);
                        if (null != value && 0 < value.length()) {
                            if (false == value.equals(t.getLocal())) {
                                t.setLocal(value);
                                translationDao.update(t);
                                LOG.debug("updated translation for {} to {}", key, value);
                            }
                        }
                        else {
                            final List<Key> ts = translationDao.findKeysByTokenLanguageVersion(t.getToken(), t.getLanguage(),
                                    t.getVersion());
                            translationDao.delete(ts);
                            LOG.debug("deleted translation for {} value={}", t.getToken(), t.getLocal());
                        }
                    }
                    else {
                        // create new translation for token?
                        if (null != value && 0 < value.length()) {
                            t = new Translation();
                            t.setProject(project.getKey());
                            t.setToken(key);
                            t.setLanguage(language.getKey());
                            t.setLocal(value);
                            // TODO: set version
                            translationDao.persist(t);
                            LOG.debug("persisted new translation for {}: {}", key, value);
                        }
                    }
                }
            }
            catch (javax.persistence.PersistenceException pe) {
                LOG.warn(name, pe);
            }

        }

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

    public void setTranslationDao(TranslationDao translationDao) {
        this.translationDao = translationDao;
    }

    public void setTokenDao(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

}

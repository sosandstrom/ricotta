package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.ArrayList;
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
import com.wadpam.ricotta.dao.ViewContextDao;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Translation;
import com.wadpam.ricotta.domain.Version;
import com.wadpam.ricotta.domain.ViewContext;
import com.wadpam.ricotta.model.TranslationModel;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/languages/{languageCode}/translations")
public class TranslationController {
    static final Logger        LOG                = LoggerFactory.getLogger(TranslationController.class);

    static final String        PREFIX_DESCRIPTION = TransController.PREFIX_DESCRIPTION;

    static final String        PREFIX_TOKEN       = TransController.PREFIX_TOKEN;

    private ProjectDao         projectDao;

    private LanguageDao        languageDao;

    private ProjectLanguageDao projectLanguageDao;

    private TokenDao           tokenDao;

    private TranslationDao     translationDao;

    private UberDao            uberDao;

    private ViewContextDao     viewContextDao;

    @RequestMapping(value = "{pageName}.html", method = RequestMethod.GET)
    public String getProjectLanguageForm(HttpServletRequest request, Model model, @PathVariable String projectName,
            @PathVariable String languageCode, @PathVariable String pageName) {
        final Project project = (Project) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJECT);
        final Version version = (Version) request.getAttribute(ProjectHandlerInterceptor.KEY_VERSION);
        model.addAttribute("project", project);

        Language language = languageDao.findByCode(languageCode);
        model.addAttribute("language", language);

        List<ViewContext> viewContexts = viewContextDao.findByProject(project.getKey());
        if (viewContexts.isEmpty() || "NO_CONTEXT".equals(pageName)) {
            List<TranslationModel> translations = uberDao.loadTranslations(project.getKey(), version.getKey(), language.getKey(),
                    null);
            model.addAttribute("translations", translations);

            // POST to action, not to same page!
            model.addAttribute("action", "/projects/" + projectName + "/languages/" + languageCode + "/translations/");
            return "translations";
        }
        model.addAttribute("viewContexts", viewContexts);
        return "viewContexts";
    }

    @RequestMapping(value = "{viewContextName}/index.html", method = RequestMethod.GET)
    public String getContextTranslations(HttpServletRequest request, Model model, @PathVariable String projectName,
            @PathVariable String languageCode, @PathVariable String viewContextName) {
        final Project project = (Project) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJECT);
        final Version version = (Version) request.getAttribute(ProjectHandlerInterceptor.KEY_VERSION);
        model.addAttribute("project", project);

        Language language = languageDao.findByCode(languageCode);
        model.addAttribute("language", language);

        ViewContext viewContext = viewContextDao.findByNameProject(viewContextName, project.getKey());
        model.addAttribute("viewContext", viewContext);

        List<TranslationModel> translations = uberDao.loadTranslations(project.getKey(), version.getKey(), language.getKey(),
                null);
        List<TranslationModel> contextTranslations = new ArrayList<TranslationModel>();
        for(TranslationModel tm : translations) {
            if (viewContext.getKey().equals(tm.getToken().getViewContext())) {
                contextTranslations.add(tm);
            }
        }
        model.addAttribute("translations", contextTranslations);

        // POST to action, not to same page!
        model.addAttribute("action", "/projects/" + projectName + "/languages/" + languageCode + "/translations/");

        return "translations";
    }

    @RequestMapping(value = "index.html", method = RequestMethod.POST)
    public String postProjectLanguage(HttpServletRequest request, @PathVariable String projectName,
            @PathVariable String languageCode) throws IOException {
        LOG.debug("post translations");
        final Project project = (Project) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJECT);
        final Version version = (Version) request.getAttribute(ProjectHandlerInterceptor.KEY_VERSION);
        Language language = languageDao.findByCode(languageCode);

        String name, value;
        Key key;
        Translation t;
        Token token;
        List<String> changes = new ArrayList<String>();
        // iterate all posted parameters (descriptions and translations)
        for(@SuppressWarnings("unchecked")
        Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            name = e.nextElement();
            value = request.getParameter(name);
            LOG.debug("{} = {}", name, value);
            try {
                if (name.startsWith(TransController.PREFIX_DESCRIPTION)) {
                    key = KeyFactory.stringToKey(name.substring(TransController.PREFIX_DESCRIPTION.length()));
                    token = tokenDao.findByPrimaryKey(key);
                    if (false == value.equals(token.getDescription())) {
                        final String d = String.format("D %s %s, was ", token.getName(), value, token.getDescription());
                        token.setDescription(value);
                        tokenDao.update(token);
                        LOG.debug(d);
                        changes.add(d);
                    }
                }
                else {
                    key = KeyFactory.stringToKey(name);
                    t = null;
                    LOG.debug("field key kind for {} is {}", key.toString(), key.getKind());
                    if (Translation.class.getSimpleName().equals(key.getKind())) {
                        // update or delete existing translation
                        t = translationDao.findByPrimaryKey(key);
                        changes.addAll(updateTranslation(project.getKey(), null, language, t, name, value, true));
                    }
                    else {
                        // create new translation for token
                        changes.addAll(updateTranslation(project.getKey(), key, language, null, name, value, false));
                    }
                }
            }
            catch (javax.persistence.PersistenceException pe) {
                LOG.warn(name, pe);
            }

        }

        if (!changes.isEmpty()) {
            uberDao.invalidateCache(project.getKey(), version.getKey(), language.getKey(), null);
        }

        uberDao.notifyOwner(project, version, languageCode, changes, request.getUserPrincipal().getName());

        return "redirect:index.html";
    }

    /** If translation is null, projectKey, tokenKey and languageKey must be specified! */
    protected List<String> updateTranslation(Key projectKey, Key tokenKey, Language language, Translation t, String name,
            String value, boolean delete) {
        List<String> returnValue = new ArrayList<String>();
        if (null != t) {
            final Token token = tokenDao.findByPrimaryKey(t.getToken());
            if (null != value && 0 < value.length()) {
                if (false == value.equals(t.getLocal())) {
                    final String u = String
                            .format("U %s %s=%s, was %s", language.getCode(), token.getName(), value, t.getLocal());
                    t.setLocal(value);
                    translationDao.update(t);
                    returnValue.add(u);
                    LOG.debug(u);
                }
            }
            else if (delete) {
                final List<Key> ts = translationDao.findKeysByTokenLanguageVersion(t.getToken(), t.getLanguage(), t.getVersion());
                translationDao.delete(ts);
                final String d = String.format("R %s %s", language.getCode(), token.getName());
                LOG.debug(d);
                returnValue.add(d);
            }
        }
        else {
            final Token token = tokenDao.findByPrimaryKey(tokenKey);
            // create new translation for token?
            if (null != value && 0 < value.length()) {
                t = new Translation();
                t.setProject(projectKey);
                t.setToken(tokenKey);
                t.setLanguage(language.getKey());
                t.setLocal(value);
                t.setVersion(token.getVersion());
                translationDao.persist(t);
                final String c = String.format("A %s %s=%s", language.getCode(), token.getName(), value);
                returnValue.add(c);
                LOG.debug(c);
            }
        }
        return returnValue;
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

    public void setViewContextDao(ViewContextDao viewContextDao) {
        this.viewContextDao = viewContextDao;
    }

}

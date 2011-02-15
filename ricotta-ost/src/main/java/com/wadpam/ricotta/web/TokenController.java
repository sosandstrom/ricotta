package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
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
import com.wadpam.ricotta.dao.ArtifactDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.TokenArtifactDao;
import com.wadpam.ricotta.dao.TokenDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.dao.ViewContextDao;
import com.wadpam.ricotta.domain.Artifact;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.TokenArtifact;
import com.wadpam.ricotta.domain.Version;
import com.wadpam.ricotta.domain.ViewContext;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/tokens")
public class TokenController {
    static final String      VIEW_CONTEXT_PREFIX = "viewContext.";

    static final Logger      LOGGER              = LoggerFactory.getLogger(TokenController.class);

    private ProjectDao       projectDao;

    private ArtifactDao      artifactDao;

    private TokenDao         tokenDao;

    private TokenArtifactDao tokenArtifactDao;

    private UberDao          uberDao;

    private ViewContextDao   viewContextDao;

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String createToken(Model model, @PathVariable String projectName) {
        // TODO: check project role
        LOGGER.debug("display create token form");
        Project project = projectDao.findByName(projectName);
        model.addAttribute("project", project);

        // fetch and add artifacts for this project
        model.addAttribute("artifacts", artifactDao.findByProject(project.getKey()));

        return "createToken";
    }

    @RequestMapping(value = "{action}.html", method = RequestMethod.POST)
    public String postToken(HttpServletRequest request, @PathVariable String projectName) throws IOException {
        LOGGER.debug("create token");
        final Project project = (Project) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJECT);

        // version
        final Version version = (Version) request.getAttribute(ProjectHandlerInterceptor.KEY_VERSION);

        if (null != request.getParameter("name")) {
            Token token = new Token();
            token.setName(request.getParameter("name"));
            token.setDescription(request.getParameter("description"));
            token.setProject(project.getKey());
            token.setVersion(version.getKey());
            tokenDao.persist(token);

            uberDao.invalidateCache(token.getProject(), token.getVersion(), null, null);
        }
        else {
            updateArtifactTokens(request, project, version, null, null);

            // delete selected project tokens:
            final List<Key> keys = new ArrayList<Key>();
            Key key;
            String values[] = request.getParameterValues("delete");
            if (null != values) {
                for(String keyString : values) {
                    key = KeyFactory.stringToKey(keyString);
                    keys.add(key);
                }
                uberDao.deleteTokens(keys);
                uberDao.invalidateCache(project.getKey(), version.getKey(), null, null);
            }
        }

        return "redirect:/projects/" + projectName + "/tokens/index.html";
    }

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String tokens(Model model, @PathVariable String projectName) {
        LOGGER.debug("display tokens");
        Project project = projectDao.findByName(projectName);
        model.addAttribute("project", project);

        // fetch and add tokens for this project
        model.addAttribute("tokens", tokenDao.findByProject(project.getKey(), true));

        // fetch and add artifacts for this project
        model.addAttribute("artifacts", artifactDao.findByProject(project.getKey()));

        // fetch and add viewContexts for this project
        List<ViewContext> viewContexts = new ArrayList<ViewContext>(viewContextDao.findByProject(project.getKey()));
        ViewContext noContext = new ViewContext();
        noContext.setName("NO CONTEXT");
        noContext.setDescription("Tokens in no specific context");
        viewContexts.add(0, noContext);
        model.addAttribute("viewContexts", viewContexts);

        // and TokenArtifact mappings
        HashMap<String, TokenArtifact> mappings = new HashMap<String, TokenArtifact>();
        for(TokenArtifact ta : tokenArtifactDao.findByProject(project.getKey())) {
            mappings.put(ta.getKeyString(), ta);
        }
        model.addAttribute("mappings", mappings);

        return "tokens";
    }

    protected void updateArtifactTokens(HttpServletRequest request, Project project, Version version, Token token,
            Artifact artifact) {
        // <select name="viewContext.<c:out value='${token.keyString}' />
        // <option value="${c.keyString}"
        Token t;
        Key tokenKey;
        String name, keyString;
        boolean contextsChanged = false;
        for(Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            name = e.nextElement();
            if (name.startsWith(VIEW_CONTEXT_PREFIX)) {
                keyString = request.getParameter(name);
                tokenKey = KeyFactory.stringToKey(name.substring(VIEW_CONTEXT_PREFIX.length()));
                t = tokenDao.findByPrimaryKey(tokenKey);
                // no viewContext selected
                if (0 == keyString.length()) {
                    if (null != t.getViewContext()) {
                        t.setViewContext(null);
                        tokenDao.update(t);
                        contextsChanged = true;
                    }
                }
                else {
                    // context selected
                    if (null == t.getViewContext() || false == keyString.equals(KeyFactory.keyToString(t.getViewContext()))) {
                        Key contextKey = KeyFactory.stringToKey(keyString);
                        t.setViewContext(contextKey);
                        tokenDao.update(t);
                        contextsChanged = true;
                    }
                }
            }
        }
        // invalidate cache?
        if (contextsChanged) {
            LOGGER.debug("project {} version {}", project, version);
            uberDao.invalidateCache(project.getKey(), version.getKey(), null, null);
        }

        // <input type="checkbox" name="mappings" value="tokenKey.artifactKey" />
        List<TokenArtifact> mappings;
        if (null != token) {
            mappings = tokenArtifactDao.findByToken(token.getKey());
        }
        else if (null != artifact) {
            mappings = tokenArtifactDao.findByArtifact(artifact.getKey());
        }
        else {
            mappings = tokenArtifactDao.findByProject(project.getKey());
        }

        // put them in a Map
        HashMap<String, TokenArtifact> current = new HashMap<String, TokenArtifact>();
        for(TokenArtifact ta : mappings) {
            current.put(ta.getKeyString(), ta);
        }

        // process all mappings
        TokenArtifact ta;
        String mappingValues[] = request.getParameterValues("mappings");
        if (null != mappingValues) {
            for(String value : mappingValues) {
                // find existing
                ta = current.remove(value);
                if (null == ta) {
                    try {
                        int beginIndex = value.indexOf('.');

                        ta = new TokenArtifact();
                        ta.setProject(project.getKey());
                        ta.setToken(KeyFactory.stringToKey(value.substring(0, beginIndex)));
                        final Key artifactKey = KeyFactory.stringToKey(value.substring(beginIndex + 1));
                        ta.setArtifact(artifactKey);
                        tokenArtifactDao.persist(ta);
                        uberDao.invalidateCache(project.getKey(), version.getKey(), null, artifactKey);
                    }
                    catch (IllegalArgumentException log) {
                        LOGGER.warn("No such tokenArtifact " + value);
                    }
                }
            }
        }

        // delete no longer checked
        for(TokenArtifact entity : current.values()) {
            tokenArtifactDao.delete(entity);
            uberDao.invalidateCache(entity.getProject(), entity.getVersion(), null, entity.getArtifact());
        }
    }

    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public void setArtifactDao(ArtifactDao artifactDao) {
        this.artifactDao = artifactDao;
    }

    public void setTokenDao(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public void setTokenArtifactDao(TokenArtifactDao tokenArtifactDao) {
        this.tokenArtifactDao = tokenArtifactDao;
    }

    public ViewContextDao getViewContextDao() {
        return viewContextDao;
    }

    public void setViewContextDao(ViewContextDao viewContextDao) {
        this.viewContextDao = viewContextDao;
    }

    public void setUberDao(UberDao uberDao) {
        this.uberDao = uberDao;
    }

}

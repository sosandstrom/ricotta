package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.dao.ArtifactDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.TokenArtifactDao;
import com.wadpam.ricotta.dao.TokenDao;
import com.wadpam.ricotta.domain.Artifact;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.TokenArtifact;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects/{projectName}/tokens")
public class TokenController {
    static final Logger      LOGGER = LoggerFactory.getLogger(TokenController.class);

    private ProjectDao       projectDao;

    private ArtifactDao      artifactDao;

    private TokenDao         tokenDao;

    private TokenArtifactDao tokenArtifactDao;

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
    public String postToken(HttpServletRequest request, @PathVariable String projectName, @ModelAttribute("token") Token token)
            throws IOException {
        LOGGER.debug("create token");

        Project project = projectDao.findByName(projectName);
        LOGGER.debug(project.toString());
        // TODO: check project role

        if (null != token.getName()) {
            token.setProject(project.getKey());
            LOGGER.debug(token.toString());
            tokenDao.persist(token);
            updateArtifactTokens(request, project, token, null);
        }
        else {
            updateArtifactTokens(request, project, null, null);
        }

        return "redirect:/projects/" + projectName + "/tokens/";
    }

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String tokens(Model model, @PathVariable String projectName) {
        // TODO: check project role
        LOGGER.debug("display tokens");
        Project project = projectDao.findByName(projectName);
        model.addAttribute("project", project);

        // fetch and add artifacts for this project
        model.addAttribute("tokens", tokenDao.findByProject(project.getKey()));

        // fetch and add artifacts for this project
        model.addAttribute("artifacts", artifactDao.findByProject(project.getKey()));

        // and TokenArtifact mappings
        HashMap<String, TokenArtifact> mappings = new HashMap<String, TokenArtifact>();
        for(TokenArtifact ta : tokenArtifactDao.findByProject(project.getKey())) {
            mappings.put(ta.getKeyString(), ta);
        }
        model.addAttribute("mappings", mappings);

        return "tokens";
    }

    protected void updateArtifactTokens(HttpServletRequest request, Project project, Token token, Artifact artifact) {
        // <input type="checkbox" name="mappings" value="tokenKey-artifactKey" />
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
        for(String value : request.getParameterValues("mappings")) {
            // find existing
            ta = current.remove(value);
            if (null == ta) {
                int beginIndex = value.indexOf('-');

                ta = new TokenArtifact();
                ta.setProject(project.getKey());
                ta.setToken(KeyFactory.stringToKey(value.substring(0, beginIndex)));
                ta.setArtifact(KeyFactory.stringToKey(value.substring(beginIndex + 1)));
                tokenArtifactDao.persist(ta);
            }
        }

        // delete no longer checked
        for(TokenArtifact entity : current.values()) {
            tokenArtifactDao.delete(entity);
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
}

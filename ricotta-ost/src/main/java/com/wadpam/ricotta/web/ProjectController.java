package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.dao.ArtifactDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.ProjectUserDao;
import com.wadpam.ricotta.dao.TokenDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.dao.UberDaoBean;
import com.wadpam.ricotta.dao.VersionDao;
import com.wadpam.ricotta.dao.ViewContextDao;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.ProjectUser;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Version;
import com.wadpam.ricotta.domain.ViewContext;
import com.wadpam.ricotta.model.ProjectLanguageModel;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/projects")
public class ProjectController {
    static final Logger            LOGGER           = LoggerFactory.getLogger(ProjectController.class);

    private ProjectDao             projectDao;

    private ProjectUserDao         projectUserDao;

    private UberDao                uberDao;

    private ArtifactDao            artifactDao;

    private TokenDao               tokenDao;

    private VersionDao             versionDao;

    private ViewContextDao         viewContextDao;

    private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String getProjects(HttpServletRequest request, Model model) {
        LOGGER.debug("get projects list {}, {}", projectDao, request.getUserPrincipal());

        String username = "Googlebot";
        if (null != request.getUserPrincipal()) {
            username = request.getUserPrincipal().getName();
        }
        List<Project> projects = new ArrayList<Project>(projectDao.findByOwner(username));
        for(ProjectUser pu : projectUserDao.findByUser(username)) {
            projects.add(projectDao.findByPrimaryKey(pu.getProject()));
        }
        model.addAttribute("projects", projects);
        return "projects";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String createProject() {
        LOGGER.debug("display create project form");
        return "createProject";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String postProject(HttpServletRequest request, @ModelAttribute("project") Project project) throws IOException {
        LOGGER.debug("create project");

        // fetch the principal
        project.setOwner(request.getUserPrincipal().getName());

        projectDao.persist(project);

        return "redirect:/projects/" + project.getName() + '/';
    }

    @RequestMapping(value = "{projectName}/uploadContext.html", method = RequestMethod.GET)
    public String uploadContext(Model model, @PathVariable String projectName) {

        // create upload URL for Blob
        model.addAttribute("action", blobstoreService.createUploadUrl("/projects/" + projectName + "/uploadedContext.html"));

        return "uploadContext";
    }

    @RequestMapping(value = "{projectName}/uploadedContext.html", method = RequestMethod.POST)
    public String uploadedContext(HttpServletRequest request, @PathVariable String projectName) throws IOException {
        LOGGER.debug("create context details");
        Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(request);
        BlobKey blobKey = blobs.get("screenShot");
        return "redirect:/projects/" + projectName + "/createContext.html?blobKey=" + blobKey.getKeyString();
    }

    @RequestMapping(value = "{projectName}/createContext.html", method = RequestMethod.GET)
    public String createContext(HttpServletRequest request, @PathVariable String projectName, @RequestParam String blobKey,
            Model model) throws IOException {
        LOGGER.debug("create context details");
        model.addAttribute("blobKeyString", blobKey);
        return "createContext";
    }

    @RequestMapping(value = "{projectName}/createContext.html", method = RequestMethod.POST)
    public String createdContext(HttpServletRequest request, @ModelAttribute("viewContext") ViewContext viewContext,
            @RequestParam String blobKeyString) throws IOException {
        LOGGER.debug("created context");
        final Project project = (Project) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJECT);

        viewContext.setProject(project.getKey());
        viewContext.setBlobKey(new BlobKey(blobKeyString));

        viewContextDao.persist(viewContext);

        return "redirect:/projects/" + project.getName() + '/';
    }

    @RequestMapping(value = "{projectName}/index.html", method = RequestMethod.POST)
    public String deleteUsersTokens(HttpServletRequest request) throws IOException {
        LOGGER.debug("delete from project");
        final Project project = (Project) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJECT);
        final Version version = (Version) request.getAttribute(ProjectHandlerInterceptor.KEY_VERSION);

        // delete selected project users:
        final List<Key> keys = new ArrayList<Key>();
        String values[] = request.getParameterValues("users");
        if (null != values) {
            for(String keyString : values) {
                keys.add(KeyFactory.stringToKey(keyString));
            }
            projectUserDao.delete(keys);
        }

        return "redirect:/projects/" + project.getName() + '/';
    }

    @RequestMapping(value = "patchAll.html", method = RequestMethod.GET)
    public String patchAll() {
        ((UberDaoBean) uberDao).patch(uberDao.getHead().getKey());
        return "redirect:/index.html";
    }

    @RequestMapping(value = "upgrade.html", method = RequestMethod.GET)
    public String upgrade() {
        ((UberDaoBean) uberDao).upgrade();
        return "redirect:/index.html";
    }

    @RequestMapping(value = "{projectName}/index.html", method = RequestMethod.GET)
    public String getProject(HttpServletRequest request, Model model, @PathVariable String projectName) {
        LOGGER.debug("get project");

        // fetch and add project details
        final Project project = (Project) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJECT);
        model.addAttribute("project", project);

        // version
        final Version version = (Version) request.getAttribute(ProjectHandlerInterceptor.KEY_VERSION);
        model.addAttribute(ProjectHandlerInterceptor.KEY_VERSION, version);

        // fetch and add languages for this project
        List<ProjectLanguageModel> languages = uberDao.loadProjectLanguages(project.getKey(), version.getKey());
        model.addAttribute("languages", languages);

        // fetch and add viewContexts for this project
        List<ViewContext> viewContexts = viewContextDao.findByProject(project.getKey());
        model.addAttribute("viewContexts", viewContexts);

        // fetch and add artifacts for this project
        model.addAttribute("artifacts", artifactDao.findByProject(project.getKey()));

        // fetch and add versions for this project
        model.addAttribute(UberDao.VALUE_HEAD, uberDao.getHead());
        model.addAttribute("versions", versionDao.findByProject(project.getKey()));

        // fetch and add users for this project
        model.addAttribute("users", projectUserDao.findByProject(project.getKey()));

        // fetch and add tokens for this project
        List<Token> tokens = tokenDao.findByProjectVersion(project.getKey(), version.getKey(), true);
        model.addAttribute("tokens", tokens);

        return "project";
    }

    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public void setTokenDao(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public void setUberDao(UberDao uberDao) {
        this.uberDao = uberDao;
    }

    public void setArtifactDao(ArtifactDao artifactDao) {
        this.artifactDao = artifactDao;
    }

    public void setProjectUserDao(ProjectUserDao projectUserDao) {
        this.projectUserDao = projectUserDao;
    }

    public ProjectUserDao getProjectUserDao() {
        return projectUserDao;
    }

    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

    public void setViewContextDao(ViewContextDao viewContextDao) {
        this.viewContextDao = viewContextDao;
    }

}

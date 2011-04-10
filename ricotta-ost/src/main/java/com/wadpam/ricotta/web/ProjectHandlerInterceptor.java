package com.wadpam.ricotta.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.dao.BranchDao;
import com.wadpam.ricotta.dao.ProjDao;
import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.ProjectUserDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.dao.VersionDao;
import com.wadpam.ricotta.domain.Branch;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.Lang;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.ProjectUser;
import com.wadpam.ricotta.domain.Version;

public class ProjectHandlerInterceptor extends HandlerInterceptorAdapter {
    static final Logger           LOG                = LoggerFactory.getLogger(ProjectHandlerInterceptor.class);

    protected static final String KEY_PROJECT        = "project";
    protected static final String KEY_VERSION        = "version";

    protected static final String KEY_PROJ           = Proj.class.getSimpleName();
    protected static final String KEY_BRANCH         = Branch.class.getSimpleName();
    protected static final String KEY_LANG           = Lang.class.getSimpleName();
    protected static final String KEY_PROJLANG       = ProjLang.class.getSimpleName();
    protected static final String KEY_CONTEXT        = Ctxt.class.getSimpleName();
    protected static final String KEY_LANGCODE       = "langCode";
    protected static final String KEY_PROJKEY        = "projKey";
    protected static final String KEY_LANGKEY        = "langKey";
    protected static final String KEY_PROJLANGKEY    = "projLangKey";
    protected static final String KEY_BRANCHKEY      = "branchKey";
    protected static final String KEY_CONTEXTKEY     = "ctxtKey";
    protected static final String KEY_PROJNAME       = "projName";
    protected static final String KEY_BRANCHNAME     = "branchName";
    protected static final String KEY_CONTEXTNAME    = "ctxtName";

    public static final String    NAME_TRUNK         = "trunk";

    static final Pattern          REGEXP_PROJECT     = Pattern.compile("\\A/projects/([^/]+)/");
    static final Pattern          REGEXP_TRANSLATION = Pattern.compile("\\A/projects/([^/]+)/languages/[^/]+/templates/[^/]+/");

    static final Pattern          REGEXP_PROJ        = Pattern.compile("\\A/proj/([^/]+)");
    static final Pattern          REGEXP_BRANCH      = Pattern.compile("/branch/([^/]+)");
    static final Pattern          REGEXP_LANG        = Pattern.compile("/lang/([^/]+)");
    static final Pattern          REGEXP_CONTEXT     = Pattern.compile("/ctxt/([^/]+)");

    private ProjectDao            projectDao;
    private ProjectUserDao        projectUserDao;
    private UberDao               uberDao;
    private VersionDao            versionDao;

    private ProjDao               projDao;
    private BranchDao             branchDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("link", new LinkBuilder(request));
        Matcher matcher = REGEXP_PROJECT.matcher(request.getRequestURI());
        if (matcher.find()) {
            final String projectName = matcher.group(1);
            LOG.debug("================== projectName is {} =======================", projectName);
            final Project project = projectDao.findByName(projectName);
            if (null == project) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such project " + projectName);
                return false;
            }
            request.setAttribute(KEY_PROJECT, project);

            // load version
            String versionParam = request.getParameter(KEY_VERSION);
            Version version = null;
            if (null == versionParam) {
                version = uberDao.getHead();
            }
            else {
                version = versionDao.findByNameProject(versionParam, project.getKey());
                if (null == version) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such version " + versionParam);
                    return false;
                }
            }
            request.setAttribute(KEY_VERSION, version);
            LOG.debug("                   versionName is {}", version.getName());

            // template request?
            matcher = REGEXP_TRANSLATION.matcher(request.getRequestURI());
            if (matcher.find()) {
                return true;
            }

            // owner?
            String user = request.getUserPrincipal().getName();
            if (project.getOwner().equals(user)) {
                return true;
            }

            // member?
            final ProjectUser projectUser = projectUserDao.findByProjectUser(project.getKey(), user);
            LOG.debug("checking if {} is authorized for {}: " + projectUser, user, projectName);
            if (null == projectUser) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No such user for project " + user);
                return false;
            }
            return true;
        }

        String projName = null, branchName = null;
        // try /proj/projname first
        matcher = REGEXP_PROJ.matcher(request.getRequestURI());
        if (matcher.find()) {
            projName = matcher.group(1);

            // create projKey first
            if (null != projName) {
                request.setAttribute(KEY_PROJNAME, projName);
                final Key projKey = KeyFactory.createKey(Proj.class.getSimpleName(), projName);
                request.setAttribute(KEY_PROJKEY, projKey);

                // then /branch/branchname
                matcher = REGEXP_BRANCH.matcher(request.getRequestURI());
                if (matcher.find()) {
                    branchName = matcher.group(1);
                }
                else {
                    branchName = NAME_TRUNK;
                }
                if (null != branchName) {
                    request.setAttribute(KEY_BRANCHNAME, branchName);
                    final Key branchKey = KeyFactory.createKey(projKey, Branch.class.getSimpleName(), branchName);
                    request.setAttribute(KEY_BRANCHKEY, branchKey);

                    // lang available?
                    matcher = REGEXP_LANG.matcher(request.getRequestURI());
                    if (matcher.find()) {
                        String langCode = matcher.group(1);
                        request.setAttribute(KEY_LANGCODE, langCode);
                        final Key langKey = KeyFactory.createKey(Lang.class.getSimpleName(), langCode);
                        request.setAttribute(KEY_LANGKEY, langKey);
                        final Key projLangKey = KeyFactory.createKey(branchKey, ProjLang.class.getSimpleName(), langCode);
                        request.setAttribute(KEY_PROJLANGKEY, projLangKey);
                    }

                    // context available?
                    matcher = REGEXP_CONTEXT.matcher(request.getRequestURI());
                    if (matcher.find()) {
                        String ctxtName = matcher.group(1);
                        request.setAttribute(KEY_CONTEXTNAME, ctxtName);
                        final Key ctxtKey = KeyFactory.createKey(branchKey, Ctxt.class.getSimpleName(), ctxtName);
                        request.setAttribute(KEY_CONTEXTKEY, ctxtKey);
                    }

                }
            }
        }

        return true;
    }

    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public ProjectDao getProjectDao() {
        return projectDao;
    }

    public void setProjectUserDao(ProjectUserDao projectUserDao) {
        this.projectUserDao = projectUserDao;
    }

    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

    public void setUberDao(UberDao uberDao) {
        this.uberDao = uberDao;
    }
}

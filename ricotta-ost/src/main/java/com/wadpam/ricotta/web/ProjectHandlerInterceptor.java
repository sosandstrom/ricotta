package com.wadpam.ricotta.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.wadpam.ricotta.dao.ProjectDao;
import com.wadpam.ricotta.dao.ProjectUserDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.dao.VersionDao;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.ProjectUser;
import com.wadpam.ricotta.domain.Version;

public class ProjectHandlerInterceptor extends HandlerInterceptorAdapter {
    static final Logger           LOG                = LoggerFactory.getLogger(ProjectHandlerInterceptor.class);

    protected static final String KEY_PROJECT        = "project";
    protected static final String KEY_VERSION        = "version";

    static final Pattern          REGEXP_PROJECT     = Pattern.compile("\\A/projects/([^/]+)/");
    static final Pattern          REGEXP_TRANSLATION = Pattern.compile("\\A/projects/([^/]+)/languages/[^/]+/templates/[^/]+/");

    private ProjectDao            projectDao;

    private ProjectUserDao        projectUserDao;

    private UberDao               uberDao;

    private VersionDao            versionDao;

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
            if (null == versionParam) {
                request.setAttribute(KEY_VERSION, uberDao.getHead());
            }
            else {
                final Version version = versionDao.findByNameProject(versionParam, project.getKey());
                if (null == version) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such version " + versionParam);
                    return false;
                }
                request.setAttribute(KEY_VERSION, version);
            }

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

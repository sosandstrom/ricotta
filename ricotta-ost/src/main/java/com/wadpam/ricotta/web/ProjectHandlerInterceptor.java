package com.wadpam.ricotta.web;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.dao.ProjDao;
import com.wadpam.ricotta.dao.ProjUserDao;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.domain.Branch;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.Lang;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.domain.ProjUser;
import com.wadpam.ricotta.domain.Subset;
import com.wadpam.ricotta.domain.Template;

public class ProjectHandlerInterceptor extends HandlerInterceptorAdapter {
    static final Logger           LOG                 = LoggerFactory.getLogger(ProjectHandlerInterceptor.class);

    protected static final String KEY_PROJECT         = "project";
    protected static final String KEY_VERSION         = "version";

    protected static final String KEY_PROJ            = Proj.class.getSimpleName();
    protected static final String KEY_BRANCH          = Branch.class.getSimpleName();
    protected static final String KEY_LANG            = Lang.class.getSimpleName();
    protected static final String KEY_PROJLANG        = ProjLang.class.getSimpleName();
    protected static final String KEY_CONTEXT         = Ctxt.class.getSimpleName();
    protected static final String KEY_PROJUSER        = "projUser";                                                                   // .class.getSimpleName();
    protected static final String KEY_LANGCODE        = "langCode";
    protected static final String KEY_PROJKEY         = "projKey";
    protected static final String KEY_LANGKEY         = "langKey";
    protected static final String KEY_PROJLANGKEY     = "projLangKey";
    protected static final String KEY_BRANCHKEY       = "branchKey";
    protected static final String KEY_CONTEXTKEY      = "ctxtKey";
    protected static final String KEY_TEMPLKEY        = "templKey";
    protected static final String KEY_SUBSETKEY       = "subsetKey";
    protected static final String KEY_PROJNAME        = "projName";
    protected static final String KEY_BRANCHNAME      = "branchName";
    protected static final String KEY_CONTEXTNAME     = "ctxtName";
    protected static final String KEY_PRINCIPAL       = "principal";

    public static final String    NAME_TRUNK          = "trunk";

    static final Pattern          REGEXP_PROJECT      = Pattern.compile("\\A/projects/([^/]+)/");
    static final Pattern          REGEXP_GENERATE_OLD = Pattern
                                                              .compile("\\A/projects/([^/]+)/languages/([^/]+)/templates/([^/]+)/");
    static final Pattern          REGEXP_ARTIFACT     = Pattern.compile("/artifacts/([^/]+)/");

    static final Pattern          REGEXP_PROJ         = Pattern.compile("\\A/proj/([^/]+)/");
    static final Pattern          REGEXP_BRANCH       = Pattern.compile("/branch/([^/]+)/");
    static final Pattern          REGEXP_LANG         = Pattern.compile("/lang/([^/]+)/");
    static final Pattern          REGEXP_CONTEXT      = Pattern.compile("/ctxt/([^/]+)/");
    static final Pattern          REGEXP_TEMPL        = Pattern.compile("/templ/([^/]+)/");
    static final Pattern          REGEXP_SUBSET       = Pattern.compile("/subset/([^/]+)/");
    static final Pattern          REGEXP_GENERATE     = Pattern
                                                              .compile("\\A/proj/([^/]+)/branch/([^/]+)/lang/([^/]+)/templ/([^/]+)/");

    // private ProjectDao projectDao;
    // private ProjectUserDao projectUserDao;
    // private UberDao uberDao;
    // private VersionDao versionDao;

    private ProjDao               projDao;
    private ProjUserDao           projUserDao;

    // private BranchDao branchDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean hasAccess = false;
        String requestURI = request.getRequestURI();

        // generate request, old format?
        Matcher matcher = REGEXP_GENERATE_OLD.matcher(requestURI);
        if (matcher.find()) {
            hasAccess = true;
            final String projectName = matcher.group(1);
            final String languageCode = matcher.group(2);
            final String templateName = matcher.group(3);
            String versionName = request.getParameter(KEY_VERSION);
            if (null == versionName || UberDao.VALUE_HEAD.equals(versionName)) {
                versionName = NAME_TRUNK;
            }
            matcher = REGEXP_ARTIFACT.matcher(requestURI);
            final String artifactName = matcher.find() ? matcher.group(1) : null;

            // build new format of URI
            requestURI = String.format("/proj/%s/branch/%s/lang/%s/templ/%s/", projectName, versionName, languageCode,
                    templateName);
            if (null != artifactName) {
                requestURI = String.format("%ssubset/%s/", requestURI, artifactName);
            }
            LOG.debug("rewrote requestURI to {}", requestURI);
        }
        else {
            // generate request, new format?
            matcher = REGEXP_GENERATE.matcher(requestURI);
            hasAccess = matcher.find();
        }

        // try /proj/{projname} first
        matcher = REGEXP_PROJ.matcher(requestURI);
        if (matcher.find()) {
            final String projName = matcher.group(1);

            // create projKey first
            if (null != projName) {
                request.setAttribute(KEY_PROJNAME, projName);
                final Key projKey = KeyFactory.createKey(Proj.class.getSimpleName(), projName);
                request.setAttribute(KEY_PROJKEY, projKey);

                // check access?
                if (!hasAccess) {
                    // owner?
                    String user = request.getUserPrincipal().getName();
                    final Proj proj = projDao.findByPrimaryKey(projName);
                    request.setAttribute(KEY_PROJ, proj);
                    if (proj.getOwner().equals(user)) {
                        hasAccess = true;
                    }
                    else {
                        // member?
                        List<String> users = projUserDao.findKeysByProj(projKey);
                        hasAccess = users.contains(user);
                    }

                    if (hasAccess) {
                        final ProjUser projUser = projUserDao.findByPrimaryKey(projKey, user);
                        request.setAttribute(KEY_PROJUSER, projUser);
                    }
                }

                // then build /branch/branchname
                matcher = REGEXP_BRANCH.matcher(requestURI);
                final String branchName = matcher.find() ? matcher.group(1) : NAME_TRUNK;
                if (null != branchName) {
                    request.setAttribute(KEY_BRANCHNAME, branchName);
                    final Key branchKey = KeyFactory.createKey(projKey, Branch.class.getSimpleName(), branchName);
                    request.setAttribute(KEY_BRANCHKEY, branchKey);

                    // lang available?
                    matcher = REGEXP_LANG.matcher(requestURI);
                    if (matcher.find()) {
                        String langCode = matcher.group(1);
                        request.setAttribute(KEY_LANGCODE, langCode);
                        final Key langKey = KeyFactory.createKey(Lang.class.getSimpleName(), langCode);
                        request.setAttribute(KEY_LANGKEY, langKey);
                        final Key projLangKey = KeyFactory.createKey(branchKey, ProjLang.class.getSimpleName(), langCode);
                        request.setAttribute(KEY_PROJLANGKEY, projLangKey);
                    }

                    // context available?
                    matcher = REGEXP_CONTEXT.matcher(requestURI);
                    if (matcher.find()) {
                        String ctxtName = matcher.group(1);
                        request.setAttribute(KEY_CONTEXTNAME, ctxtName);
                        final Key ctxtKey = KeyFactory.createKey(branchKey, Ctxt.class.getSimpleName(), ctxtName);
                        request.setAttribute(KEY_CONTEXTKEY, ctxtKey);
                    }

                    // templ available?
                    matcher = REGEXP_TEMPL.matcher(requestURI);
                    if (matcher.find()) {
                        String templName = matcher.group(1);
                        final Key templKey = KeyFactory.createKey(Template.class.getSimpleName(), templName);
                        request.setAttribute(KEY_TEMPLKEY, templKey);
                        LOG.debug("matched templ to {} {}", templName, templKey);
                    }

                    // subset available?
                    matcher = REGEXP_SUBSET.matcher(requestURI);
                    if (matcher.find()) {
                        String subsetName = matcher.group(1);
                        final Key subsetKey = KeyFactory.createKey(branchKey, Subset.class.getSimpleName(), subsetName);
                        request.setAttribute(KEY_SUBSETKEY, subsetKey);
                    }

                }
            }
            return hasAccess;
        }
        return true;
    }

    public void setProjDao(ProjDao projDao) {
        this.projDao = projDao;
    }

    public final void setProjUserDao(ProjUserDao projUserDao) {
        this.projUserDao = projUserDao;
    }
}

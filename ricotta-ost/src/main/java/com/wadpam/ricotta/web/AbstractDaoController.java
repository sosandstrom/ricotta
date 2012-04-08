package com.wadpam.ricotta.web;

import com.wadpam.ricotta.dao.*;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;


public abstract class AbstractDaoController {
    protected LangDao       langDao;
    protected TemplateDao   templateDao;
    protected ProjDao       projDao;
    protected BranchDao     branchDao;
    protected ProjLangDao   projLangDao;
    protected ProjUserDao   projUserDao;
    protected ToknDao       toknDao;
    protected CtxtDao       ctxtDao;
    protected TransDao      transDao;
    protected SubsetDao     subsetDao;
    protected SubsetToknDao subsetToknDao;
    protected RoleDao       roleDao;
    protected UberDaoBean       uberDao;

    // ----------------- Populate Model ----------------------

    @ModelAttribute(ProjectHandlerInterceptor.KEY_PROJNAME)
    public Object populateProjName(HttpServletRequest request) {
        return request.getAttribute(ProjectHandlerInterceptor.KEY_PROJNAME);
    }

    @ModelAttribute(ProjectHandlerInterceptor.KEY_BRANCHNAME)
    public Object populateBranchName(HttpServletRequest request) {
        return request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHNAME);
    }

    @ModelAttribute(ProjectHandlerInterceptor.KEY_LANGCODE)
    public Object populateLangCode(HttpServletRequest request) {
        return request.getAttribute(ProjectHandlerInterceptor.KEY_LANGCODE);
    }

    @ModelAttribute(ProjectHandlerInterceptor.KEY_PRINCIPAL)
    public Authentication populatePrincipal(HttpServletRequest request) {
        Authentication returnValue = SecurityContextHolder.getContext().getAuthentication();
        return returnValue;
    }

    @ModelAttribute(ProjectHandlerInterceptor.KEY_PROJUSER)
    public Object populateProjUser(HttpServletRequest request) {
        final Object returnValue = request.getAttribute(ProjectHandlerInterceptor.KEY_PROJUSER);
        return returnValue;
    }

    // -------------- Getters and Setters ------------------------

    public final void setProjDao(ProjDao projDao) {
        this.projDao = projDao;
    }

    public final void setBranchDao(BranchDao branchDao) {
        this.branchDao = branchDao;
    }

    public final void setProjLangDao(ProjLangDao projLangDao) {
        this.projLangDao = projLangDao;
    }

    public final void setToknDao(ToknDao toknDao) {
        this.toknDao = toknDao;
    }

    public final void setTransDao(TransDao transDao) {
        this.transDao = transDao;
    }

    public final void setSubsetDao(SubsetDao subsetDao) {
        this.subsetDao = subsetDao;
    }

    public final void setSubsetToknDao(SubsetToknDao subsetToknDao) {
        this.subsetToknDao = subsetToknDao;
    }

    public final void setLangDao(LangDao langDao) {
        this.langDao = langDao;
    }

    public final void setTemplateDao(TemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    public final void setProjUserDao(ProjUserDao projUserDao) {
        this.projUserDao = projUserDao;
    }

    public final void setUberDao(UberDaoBean uberDao) {
        this.uberDao = uberDao;
    }

    public final void setCtxtDao(CtxtDao ctxtDao) {
        this.ctxtDao = ctxtDao;
    }

    public final void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }
}

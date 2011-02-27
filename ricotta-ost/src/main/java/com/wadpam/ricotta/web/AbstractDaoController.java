package com.wadpam.ricotta.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.wadpam.ricotta.dao.BranchDao;
import com.wadpam.ricotta.dao.LangDao;
import com.wadpam.ricotta.dao.ProjDao;
import com.wadpam.ricotta.dao.ProjLangDao;
import com.wadpam.ricotta.dao.ProjUserDao;
import com.wadpam.ricotta.dao.SubsetDao;
import com.wadpam.ricotta.dao.SubsetToknDao;
import com.wadpam.ricotta.dao.TemplateDao;
import com.wadpam.ricotta.dao.ToknDao;
import com.wadpam.ricotta.dao.TransDao;

public abstract class AbstractDaoController {
    protected LangDao       langDao;
    protected TemplateDao   templateDao;
    protected ProjDao       projDao;
    protected BranchDao     branchDao;
    protected ProjLangDao   projLangDao;
    protected ProjUserDao   projUserDao;
    protected ToknDao       toknDao;
    protected TransDao      transDao;
    protected SubsetDao     subsetDao;
    protected SubsetToknDao subsetToknDao;

    // ----------------- Populate Model ----------------------

    @ModelAttribute("projName")
    public Object populateProjName(HttpServletRequest request) {
        return request.getAttribute(ProjectHandlerInterceptor.KEY_PROJNAME);
    }

    @ModelAttribute("branchName")
    public Object populateBranchName(HttpServletRequest request) {
        return request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHNAME);
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
}

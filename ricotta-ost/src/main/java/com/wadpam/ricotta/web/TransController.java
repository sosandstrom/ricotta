package com.wadpam.ricotta.web;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.model.TransModel;

@Controller
@RequestMapping(value = "/proj/{projName}/branch/{branchName}")
public class TransController extends AbstractDaoController {
    static final Logger LOG = LoggerFactory.getLogger(TransController.class);

    @RequestMapping(value = "lang/{langCode}/index.html")
    public String getTrans(Model model, HttpServletRequest request) {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final List<Ctxt> contexts = ctxtDao.findByBranch(branchKey);
        if (false == contexts.isEmpty()) {
            model.addAttribute("viewContexts", contexts);
            return "contexts";
        }
        final String langCode = (String) request.getAttribute(ProjectHandlerInterceptor.KEY_LANGCODE);
        final ProjLang projLang = projLangDao.findByPrimaryKey(branchKey, langCode);
        final Collection<TransModel> trans = uberDao.loadTrans(branchKey, null, projLang, null);
        model.addAttribute("translations", trans);
        return "trans";
    }

    @RequestMapping(value = "lang/{langCode}/ctxt/index.html")
    public String getContexts(Model model, HttpServletRequest request) {
        return getTrans(model, request);
    }

    @RequestMapping(value = "lang/{langCode}/ctxt/{contextName}/index.html")
    public String getTransByContext(Model model, HttpServletRequest request) {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final Key ctxtKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_CONTEXTKEY);
        final String langCode = (String) request.getAttribute(ProjectHandlerInterceptor.KEY_LANGCODE);
        final ProjLang projLang = projLangDao.findByPrimaryKey(branchKey, langCode);
        final Collection<TransModel> trans = uberDao.loadTrans(branchKey, null, projLang, ctxtKey);
        model.addAttribute("translations", trans);
        return "trans";
    }

}

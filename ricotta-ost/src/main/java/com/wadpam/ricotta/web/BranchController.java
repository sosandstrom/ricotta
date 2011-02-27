package com.wadpam.ricotta.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.model.ProjLangModel;

@Controller
@RequestMapping(value = "/proj/{projName}")
public class BranchController extends AbstractDaoController {
    static final Logger LOG = LoggerFactory.getLogger(BranchController.class);

    @RequestMapping(value = "index.html")
    public String getTrunk(HttpServletRequest request, @PathVariable String projName) {
        return String.format("redirect:/proj/%s/branch/%s/", projName,
                request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHNAME));
    }

    @RequestMapping(value = "branch/{branchName}/index.html")
    public String getBranch(Model model, HttpServletRequest request) {
        // final Key projKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJKEY);
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        // final Branch branch = branchDao.findByPrimaryKey(projKey, branchName);
        // LOG.info("branch {} is {}", branch.getName(), branch.getDescription());

        // project languages
        List<ProjLangModel> languages = new ArrayList<ProjLangModel>();
        for(ProjLang pl : projLangDao.findByBranch(branchKey)) {
            ProjLangModel plm = new ProjLangModel();
            plm.setLang(langDao.findByPrimaryKey(pl.getLang().getName()));
            if (null != pl.getDefaultLang()) {
                plm.setDefaultCode(pl.getDefaultLang().getName());
            }
            languages.add(plm);
        }
        model.addAttribute("languages", languages);

        return "branch";
    }

}

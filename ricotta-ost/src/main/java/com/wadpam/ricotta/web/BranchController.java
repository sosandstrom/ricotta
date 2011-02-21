package com.wadpam.ricotta.web;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.dao.BranchDao;
import com.wadpam.ricotta.domain.Branch;

@Controller
@RequestMapping(value = "/proj/{projName}/branch")
public class BranchController {
    static final Logger LOG = LoggerFactory.getLogger(BranchController.class);

    private BranchDao   branchDao;

    @RequestMapping(value = "{branchName}/index.html")
    public String getBranch(HttpServletRequest request, @PathVariable String branchName) {
        final Key projKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJKEY);
        // final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final Branch branch = branchDao.findByPrimaryKey(projKey, branchName);
        LOG.info("branch {} is {}", branch.getName(), branch.getDescription());
        return "branch";
    }

    public void setBranchDao(BranchDao branchDao) {
        this.branchDao = branchDao;
    }

}

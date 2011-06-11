package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.wadpam.ricotta.domain.Branch;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.Lang;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.domain.ProjUser;
import com.wadpam.ricotta.domain.Subset;
import com.wadpam.ricotta.model.ProjLangModel;

@Controller
@RequestMapping(value = "/proj/{projName}")
public class BranchController extends AbstractDaoController {
    static final Logger            LOG              = LoggerFactory.getLogger(BranchController.class);

    private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @RequestMapping(value = "index.html")
    public String getTrunk(HttpServletRequest request, @PathVariable String projName) {
        return String.format("redirect:/proj/%s/branch/%s/", projName,
                request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHNAME));
    }

    @RequestMapping(value = "branch/{branchName}/index.html")
    public String getBranch(Model model, HttpServletRequest request) {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);

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

        // contexts
        List<Ctxt> ctxts = ctxtDao.findByBranch(branchKey);
        model.addAttribute("ctxts", ctxts);

        // subsets
        List<Subset> subsets = subsetDao.findByBranch(branchKey);
        model.addAttribute("subsets", subsets);

        // branches
        List<Branch> branches = branchDao.findByProject(branchKey.getParent());
        model.addAttribute("branches", branches);

        // project users
        List<ProjUser> users = projUserDao.findByProj(branchKey.getParent());
        model.addAttribute("users", users);

        return "branch";
    }

    // --------- projlang part ----------------

    @RequestMapping(value = "branch/{branchName}/lang/create.html", method = RequestMethod.GET)
    public String createProjLang(HttpServletRequest request, Model model) {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final Map<String, Lang> langs = new TreeMap<String, Lang>();
        for(Lang l : langDao.findAll()) {
            langs.put(l.getCode(), l);
        }
        List<Lang> parentLangs = new ArrayList<Lang>();
        for(ProjLang pl : projLangDao.findByBranch(branchKey)) {
            Lang l = langDao.findByPrimaryKey(pl.getLangCode());
            parentLangs.add(l);
            langs.remove(l.getCode());
        }
        model.addAttribute("languages", langs.values());
        model.addAttribute("parentLanguages", parentLangs);
        return "createProjectLanguage";
    }

    @RequestMapping(value = "branch/{branchName}/lang/create.html", method = RequestMethod.POST)
    public String postSubset(HttpServletRequest request, @ModelAttribute("projLang") ProjLang projLang) throws IOException {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final ProjLang existing = projLangDao.findByPrimaryKey(branchKey, projLang.getLangCode());
        if (null != existing) {
            LOG.warn("ProjLang {} already exists", existing);
        }
        else {
            projLang.setBranch(branchKey);
            projLang.setLang((Key) langDao.findByPrimaryKey(projLang.getLangCode()).getPrimaryKey());
            projLangDao.persist(projLang);
        }

        return "redirect:../index.html";
    }

    // --------- subset part ----------------

    @RequestMapping(value = "branch/{branchName}/subset.html", method = RequestMethod.GET)
    public String getSubset(HttpServletRequest request, Model model) {
        return "createSubset";
    }

    @RequestMapping(value = "branch/{branchName}/subset.html", method = RequestMethod.POST)
    public String postSubset(HttpServletRequest request, @ModelAttribute("subset") Subset subset) throws IOException {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final Subset existing = subsetDao.findByPrimaryKey(branchKey, subset.getName());
        if (null != existing) {
            LOG.warn("Subset {} already exists", existing);
        }
        else {
            subset.setBranch(branchKey);
            subsetDao.persist(subset);
        }

        return "redirect:index.html";
    }

    // --------- user part ----------------

    @RequestMapping(value = "branch/{branchName}/user.html", method = RequestMethod.GET)
    public String getUser(HttpServletRequest request, Model model) {
        return "createUser";
    }

    @RequestMapping(value = "branch/{branchName}/user.html", method = RequestMethod.POST)
    public String postUser(HttpServletRequest request, @ModelAttribute("projectUser") ProjUser projectUser) throws IOException {
        final Key projKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJKEY);
        final ProjUser existing = projUserDao.findByPrimaryKey(projKey, projectUser.getUser());
        if (null != existing) {
            LOG.warn("User {} already added", existing);
        }
        else {
            projectUser.setProj(projKey);
            projUserDao.persist(projectUser);
        }

        return "redirect:index.html";
    }

    // --------------- context part ----------------------

    @RequestMapping(value = "branch/{branchName}/uploadCtxt.html", method = RequestMethod.GET)
    public String uploadContext(Model model, @PathVariable String projName, @PathVariable String branchName) {

        // create upload URL for Blob
        model.addAttribute("action",
                blobstoreService.createUploadUrl("/proj/" + projName + "/branch/" + branchName + "/uploadedCtxt.html"));

        return "uploadCtxt";
    }

    @RequestMapping(value = "branch/{branchName}/uploadedCtxt.html", method = RequestMethod.POST)
    public String uploadedContext(HttpServletRequest request) throws IOException {
        LOG.debug("create context details");
        Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(request);
        BlobKey blobKey = blobs.get("screenShot");
        return "redirect:createCtxt.html?blobKey=" + blobKey.getKeyString();
    }

    @RequestMapping(value = "branch/{branchName}/createCtxt.html", method = RequestMethod.GET)
    public String createContext(HttpServletRequest request, @RequestParam String blobKey, Model model) throws IOException {
        LOG.debug("create context details");
        model.addAttribute("blobKeyString", blobKey);
        return "createCtxt";
    }

    @RequestMapping(value = "branch/{branchName}/createCtxt.html", method = RequestMethod.POST)
    public String createdContext(HttpServletRequest request, @ModelAttribute("ctxt") Ctxt ctxt, @RequestParam String blobKeyString)
            throws IOException {
        LOG.debug("created context");
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);

        final Ctxt existing = ctxtDao.findByPrimaryKey(branchKey, ctxt.getName());
        if (null != existing) {
            LOG.warn("Context {} already exists", existing);
        }
        else {
            ctxt.setBranch(branchKey);
            ctxt.setBlobKey(new BlobKey(blobKeyString));
            ctxtDao.persist(ctxt);
        }

        return "redirect:index.html";
    }

    // ---------------------- branch part -------------------------------------

    @RequestMapping(value = "branch/{branchName}/create.html", method = RequestMethod.GET)
    public String getBranchForm(HttpServletRequest request, Model model) {
        return "createBranch";
    }

    @RequestMapping(value = "branch/{branchName}/create.html", method = RequestMethod.POST)
    public String createBranch(HttpServletRequest request, @ModelAttribute("branch") Branch branch) throws IOException {
        final Key fromKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final Key projKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJKEY);
        final Branch existing = branchDao.findByPrimaryKey(projKey, branch.getName());
        if (null != existing) {
            LOG.warn("Branch {} already exists", existing);
        }
        else {
            uberDao.copyBranch(fromKey, branch.getName(), branch.getDescription());
        }

        return "redirect:../" + branch.getName() + "/index.html";
    }

    @RequestMapping(value = "branch/{branchName}/deleteBranches.html", method = RequestMethod.POST)
    public String deleteBranches(HttpServletRequest request) {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final Key projKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJKEY);

        return "redirect:../" + branchKey.getName() + "/index.html";
    }
}

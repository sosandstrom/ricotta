package com.wadpam.ricotta.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.domain.Tokn;
import com.wadpam.ricotta.domain.Trans;
import com.wadpam.ricotta.domain.Translation;
import com.wadpam.ricotta.model.TransModel;

@Controller
@RequestMapping(value = "/proj/{projName}/branch/{branchName}/lang/{langCode}")
public class TransController extends AbstractDaoController {
    static final Logger LOG                = LoggerFactory.getLogger(TransController.class);
    static final String PREFIX_DESCRIPTION = "description.";
    static final String PREFIX_TOKEN       = "token.";

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String getTrans(Model model, HttpServletRequest request) {
        return getTrans(model, request, true);
    }

    protected String getTrans(Model model, HttpServletRequest request, boolean showAvailableContexts) {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final List<Ctxt> contexts = ctxtDao.findByBranch(branchKey);
        if (false == contexts.isEmpty() && showAvailableContexts) {
            model.addAttribute("viewContexts", contexts);
            return "contexts";
        }
        final String langCode = (String) request.getAttribute(ProjectHandlerInterceptor.KEY_LANGCODE);
        final ProjLang projLang = projLangDao.findByPrimaryKey(branchKey, langCode);
        final Collection<TransModel> trans = uberDao.loadTrans(branchKey, null, projLang, null);
        model.addAttribute("translations", trans);
        return "trans";
    }

    @RequestMapping(value = "ctxt/index.html", method = RequestMethod.GET)
    public String getAllTrans(Model model, HttpServletRequest request) {
        return getTrans(model, request, false);
    }

    @RequestMapping(value = "ctxt/{contextName}/index.html", method = RequestMethod.GET)
    public String getTransByContext(Model model, HttpServletRequest request) {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);

        final Key ctxtKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_CONTEXTKEY);
        final String ctxtName = (String) request.getAttribute(ProjectHandlerInterceptor.KEY_CONTEXTNAME);
        final Ctxt viewContext = ctxtDao.findByPrimaryKey(branchKey, ctxtName);
        model.addAttribute("viewContext", viewContext);

        final String langCode = (String) request.getAttribute(ProjectHandlerInterceptor.KEY_LANGCODE);
        final ProjLang projLang = projLangDao.findByPrimaryKey(branchKey, langCode);
        final Collection<TransModel> trans = uberDao.loadTrans(branchKey, null, projLang, ctxtKey);
        model.addAttribute("translations", trans);
        return "trans";
    }

    @RequestMapping(value = "index.html", method = RequestMethod.POST)
    public String saveTrans(Model model, HttpServletRequest request) {
        return saveTransInner(model, request);
    }

    @RequestMapping(value = "ctxt/index.html", method = RequestMethod.POST)
    public String saveAllTrans(Model model, HttpServletRequest request) {
        return saveTransInner(model, request);
    }

    @RequestMapping(value = "ctxt/{contextName}/index.html", method = RequestMethod.POST)
    public String saveTransByContext(Model model, HttpServletRequest request) {
        return saveTransInner(model, request);
    }

    protected String saveTransInner(Model model, HttpServletRequest request) {
        LOG.debug("saveTransInner");
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final Key projLangKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJLANGKEY);

        String name, value, tokenId;
        Key key;
        Trans t;
        Tokn token;
        List<String> changes = new ArrayList<String>();
        // iterate all posted parameters (descriptions and translations)
        for(@SuppressWarnings("unchecked")
        Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            name = e.nextElement();
            value = request.getParameter(name);
            LOG.debug("{} = {}", name, value);
            try {
                // token description to update
                if (name.startsWith(PREFIX_DESCRIPTION)) {
                    tokenId = name.substring(PREFIX_DESCRIPTION.length());
                    token = toknDao.findByPrimaryKey(branchKey, Long.parseLong(tokenId));
                    if (false == value.equals(token.getDescription())) {
                        final String d = String.format("D %s %s, was ", token.getName(), value, token.getDescription());
                        token.setDescription(value);
                        toknDao.update(token);
                        LOG.debug(d);
                        changes.add(d);
                    }
                }
                else {
                    // translation to update
                    key = KeyFactory.stringToKey(name);
                    t = null;
                    LOG.debug("field key kind for {} is {}", key.toString(), key.getKind());
                    if (Translation.class.getSimpleName().equals(key.getKind())) {
                        // update or delete existing translation
                        t = transDao.findByPrimaryKey(key.getParent(), key.getId());
                        changes.addAll(uberDao.updateTrans(projLangKey, null, t, name, value, true));
                    }
                    else {
                        // create new translation for token
                        token = toknDao.findByPrimaryKey(branchKey, key.getId());
                        changes.addAll(uberDao.updateTrans(projLangKey, token, null, name, value, false));
                    }
                }
            }
            catch (javax.persistence.PersistenceException pe) {
                LOG.warn(name, pe);
            }

        }

        final Key projKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJKEY);
        final Proj proj = projDao.findByPrimaryKey(projKey.getName());
        uberDao.notifyOwner(proj, branchKey.getName(), projLangKey.getName(), changes, request.getUserPrincipal().getName());

        return "redirect:index.html";
    }

    // ------------------ import ---------------------

    @RequestMapping(value = "import.html", method = RequestMethod.GET)
    public String getImport() {
        return "import";
    }

    @RequestMapping(value = "import.html", method = RequestMethod.POST)
    public String postImport(HttpServletRequest request, @RequestParam String regexp, @RequestParam String custom,
            @RequestParam String body) {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final String langCode = (String) request.getAttribute(ProjectHandlerInterceptor.KEY_LANGCODE);
        if ("custom".equals(regexp)) {
            regexp = custom;
        }
        uberDao.importBody(request, branchKey, langCode, regexp, body);
        return "redirect:ctxt/index.html";
    }
}

package com.wadpam.ricotta.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.Subset;
import com.wadpam.ricotta.domain.SubsetTokn;
import com.wadpam.ricotta.domain.Tokn;
import com.wadpam.ricotta.model.SubsetToknModel;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
@RequestMapping("/proj/{projName}/branch/{branchName}/tokn")
public class ToknController extends AbstractDaoController {
    static final String VIEW_CONTEXT_PREFIX = "viewContext.";

    static final String NO_CONTEXT_NAME     = "_NO_CONTEXT_";

    static final Logger LOGGER              = LoggerFactory.getLogger(ToknController.class);

    @RequestMapping(value = "create.html", method = RequestMethod.GET)
    public String createTokenForm(Model model, HttpServletRequest request) {
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);

        model.addAttribute("viewContexts", getContextMap(branchKey));

        return "createToken";
    }

    @RequestMapping(value = "create.html", method = RequestMethod.POST)
    public String createToken(HttpServletRequest request, @ModelAttribute(value = "token") Tokn tokn) throws IOException {
        LOGGER.debug("create token");
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        tokn.setBranch(branchKey);
        toknDao.persist(tokn);

        return "redirect:index.html";
    }

    @RequestMapping(value = "index.html", method = RequestMethod.POST)
    public String updateTokens(HttpServletRequest request) throws IOException {
        String[] id = request.getParameterValues("id");
        String[] name = request.getParameterValues("name");
        String[] description = request.getParameterValues("description");
        String[] ctxt = request.getParameterValues("ctxt");
        // LOGGER.debug("update tokens {} ({})", id, id.length);
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);

        // update token details
        for(int i = 0; null != id && i < id.length; i++) {
            final Tokn t = toknDao.findByPrimaryKey(branchKey, Long.parseLong(id[i]));
            // modified?
            boolean changed = false;
            if (null != t) {
                if (!name[i].equals(t.getName())) {
                    changed = true;
                    t.setName(name[i]);
                }
                if (!description[i].equals(t.getDescription())) {
                    changed = true;
                    t.setDescription(description[i]);
                }
                // has context been cleared?
                if (NO_CONTEXT_NAME.equals(ctxt[i]) && null != t.getViewContext()) {
                    changed = true;
                    t.setViewContext(null);
                }
                // has context been set?
                else if (null == t.getViewContext() && !NO_CONTEXT_NAME.equals(ctxt[i])) {
                    changed = true;
                    t.setViewContext(KeyFactory.createKey(branchKey, Ctxt.class.getSimpleName(), ctxt[i]));
                }
                // has context changed?
                else if (null != t.getViewContext()) {
                    if (!ctxt[i].equals(t.getViewContext().getName())) {
                        changed = true;
                        t.setViewContext(KeyFactory.createKey(branchKey, Ctxt.class.getSimpleName(), ctxt[i]));
                    }
                }

                if (changed) {
                    toknDao.update(t);
                }
            }
        }

        // process all SubsetTokns:
        String[] mappings = request.getParameterValues("mappings");
        if (null != mappings) {

            // stored mappings
            final Map<String, SubsetTokn> stMap = getSubsetToknMap(subsetDao.findByBranch(branchKey));
            for(String stKeyString : mappings) {
                SubsetTokn st = stMap.remove(stKeyString);
                // LOGGER.debug("existing for {} is {}", stKeyString, st);
                // create missing mappings
                if (null == st) {
                    Key stKey = KeyFactory.stringToKey(stKeyString);
                    subsetToknDao.persist(stKey.getParent(), stKey.getId());
                }
            }
            // remove unchecked mappings
            List<SubsetTokn> remove = new ArrayList<SubsetTokn>(stMap.values());
            subsetToknDao.delete(remove);
        }

        // delete selected project tokens:
        String values[] = request.getParameterValues("delete");
        if (null != values && "Delete selected tokens".equals(request.getParameter("Action"))) {
            final List<Key> keys = new ArrayList<Key>();
            Key key;
            for(String keyString : values) {
                key = KeyFactory.stringToKey(keyString);
                keys.add(key);
            }
            uberDao.deleteTokns(keys);
        }
        return "redirect:index.html";
    }

    // @RequestMapping(value = "{action}.html", method = RequestMethod.POST)
    // public String postToken(HttpServletRequest request, @PathVariable String projectName) throws IOException {
    // LOGGER.debug("create token");
    // final Project project = (Project) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJECT);
    //
    // // version
    // final Version version = (Version) request.getAttribute(ProjectHandlerInterceptor.KEY_VERSION);
    //
    // if (null != request.getParameter("name")) {
    // Token token = new Token();
    // token.setName(request.getParameter("name"));
    // token.setDescription(request.getParameter("description"));
    // token.setProject(project.getKey());
    // token.setVersion(version.getKey());
    // tokenDao.persist(token);
    //
    // uberDao.invalidateCache(token.getProject(), token.getVersion(), null, null);
    // }
    // else {
    // updateArtifactTokens(request, project, version, null, null);
    //
    // // delete selected project tokens:
    // String values[] = request.getParameterValues("delete");
    // if (null != values && "Delete selected tokens".equals(request.getParameter("Action"))) {
    // final List<Key> keys = new ArrayList<Key>();
    // Key key;
    // for(String keyString : values) {
    // key = KeyFactory.stringToKey(keyString);
    // keys.add(key);
    // }
    // uberDao.deleteTokens(keys);
    // uberDao.invalidateCache(project.getKey(), version.getKey(), null, null);
    // }
    // }
    //
    // return "redirect:/projects/" + projectName + "/tokens/index.html";
    // }

    protected Map<String, Ctxt> getContextMap(Key branchKey) {
        // fetch and add viewContexts for this branch
        Map<String, Ctxt> cMap = new TreeMap<String, Ctxt>();
        for(Ctxt ctxt : ctxtDao.findByBranch(branchKey)) {
            cMap.put(ctxt.getName(), ctxt);
        }

        Ctxt noContext = new Ctxt();
        noContext.setName(NO_CONTEXT_NAME);
        noContext.setDescription("Tokens in no specified context");
        cMap.put(NO_CONTEXT_NAME, noContext);

        return cMap;
    }

    public String createKeyString(Tokn tokn, Subset subset) {
        return KeyFactory.createKeyString((Key) subset.getPrimaryKey(), SubsetTokn.class.getSimpleName(), tokn.getId());
    }

    protected Map<String, SubsetTokn> getSubsetToknMap(List<Subset> subsets) {
        final HashMap<String, SubsetTokn> mappings = new HashMap<String, SubsetTokn>();
        for(Subset s : subsets) {
            for(SubsetTokn ta : subsetToknDao.findBySubset((Key) s.getPrimaryKey())) {
                mappings.put(ta.getKeyString(), ta);
            }
        }
        return mappings;
    }

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public String tokens(Model model, HttpServletRequest request) {
        LOGGER.debug("display tokens");
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);

        // fetch and add subsets for this branch
        final List<Subset> subsets = subsetDao.findByBranch(branchKey);

        // fetch and add tokens for this branch
        final List<Tokn> tokens = toknDao.findByBranch(branchKey);
        final List<SubsetToknModel> stms = new ArrayList<SubsetToknModel>();
        for(Tokn t : tokens) {
            stms.add(new SubsetToknModel(t, subsets));
        }
        model.addAttribute("tokens", stms);

        model.addAttribute("subsets", subsets);

        model.addAttribute("viewContexts", getContextMap(branchKey));

        model.addAttribute("keyFactory", this);

        // and TokenArtifact mappings
        final Map<String, SubsetTokn> mappings = getSubsetToknMap(subsets);
        model.addAttribute("mappings", mappings);

        return "tokns";
    }
    // protected void updateArtifactTokens(HttpServletRequest request, Project project, Version version, Token token,
    // Artifact artifact) {
    // // <select name="viewContext.<c:out value='${token.keyString}' />
    // // <option value="${c.keyString}"
    // Token t;
    // Key tokenKey;
    // String name, keyString;
    // boolean contextsChanged = false;
    // for(Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
    // name = e.nextElement();
    // if (name.startsWith(VIEW_CONTEXT_PREFIX)) {
    // keyString = request.getParameter(name);
    // tokenKey = KeyFactory.stringToKey(name.substring(VIEW_CONTEXT_PREFIX.length()));
    // t = tokenDao.findByPrimaryKey(tokenKey);
    // // no viewContext selected
    // if (0 == keyString.length()) {
    // if (null != t.getViewContext()) {
    // t.setViewContext(null);
    // tokenDao.update(t);
    // contextsChanged = true;
    // }
    // }
    // else {
    // // context selected
    // if (null == t.getViewContext() || false == keyString.equals(KeyFactory.keyToString(t.getViewContext()))) {
    // Key contextKey = KeyFactory.stringToKey(keyString);
    // t.setViewContext(contextKey);
    // tokenDao.update(t);
    // contextsChanged = true;
    // }
    // }
    // }
    // }
    // // invalidate cache?
    // if (contextsChanged) {
    // LOGGER.debug("project {} version {}", project, version);
    // uberDao.invalidateCache(project.getKey(), version.getKey(), null, null);
    // }
    //
    // // <input type="checkbox" name="mappings" value="tokenKey.artifactKey" />
    // List<TokenArtifact> mappings;
    // if (null != token) {
    // mappings = tokenArtifactDao.findByToken(token.getKey());
    // }
    // else if (null != artifact) {
    // mappings = tokenArtifactDao.findByArtifact(artifact.getKey());
    // }
    // else {
    // mappings = tokenArtifactDao.findByProject(project.getKey());
    // }
    //
    // // put them in a Map
    // HashMap<String, TokenArtifact> current = new HashMap<String, TokenArtifact>();
    // for(TokenArtifact ta : mappings) {
    // current.put(ta.getKeyString(), ta);
    // }
    //
    // // process all mappings
    // TokenArtifact ta;
    // String mappingValues[] = request.getParameterValues("mappings");
    // if (null != mappingValues) {
    // for(String value : mappingValues) {
    // // find existing
    // ta = current.remove(value);
    // if (null == ta) {
    // try {
    // int beginIndex = value.indexOf('.');
    //
    // ta = new TokenArtifact();
    // ta.setProject(project.getKey());
    // ta.setToken(KeyFactory.stringToKey(value.substring(0, beginIndex)));
    // final Key artifactKey = KeyFactory.stringToKey(value.substring(beginIndex + 1));
    // ta.setArtifact(artifactKey);
    // ta.setVersion(version.getKey());
    // tokenArtifactDao.persist(ta);
    // uberDao.invalidateCache(project.getKey(), version.getKey(), null, artifactKey);
    // }
    // catch (IllegalArgumentException log) {
    // LOGGER.warn("No such tokenArtifact " + value);
    // }
    // }
    // }
    // }
    //
    // // delete no longer checked
    // for(TokenArtifact entity : current.values()) {
    // tokenArtifactDao.delete(entity);
    // uberDao.invalidateCache(entity.getProject(), entity.getVersion(), null, entity.getArtifact());
    // }
    // }
    //

}

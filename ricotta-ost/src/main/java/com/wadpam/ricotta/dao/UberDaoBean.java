package com.wadpam.ricotta.dao;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import net.sf.mardao.api.domain.PrimaryKeyEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.domain.Branch;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.Lang;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.domain.ProjUser;
import com.wadpam.ricotta.domain.Role;
import com.wadpam.ricotta.domain.Subset;
import com.wadpam.ricotta.domain.SubsetTokn;
import com.wadpam.ricotta.domain.Template;
import com.wadpam.ricotta.domain.Tokn;
import com.wadpam.ricotta.domain.Trans;
import com.wadpam.ricotta.model.TransModel;
import com.wadpam.ricotta.model.v10.Proj10;
import com.wadpam.ricotta.model.v10.Tokn10;
import com.wadpam.ricotta.web.AbstractDaoController;
import com.wadpam.ricotta.web.ProjectHandlerInterceptor;
import com.wadpam.ricotta.web.admin.AdminTask;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import net.sf.mardao.api.dao.AEDDaoImpl;
import org.xml.sax.SAXException;

public class UberDaoBean extends AbstractDaoController implements UberDao, AdminTask {
    public static final String NO_CONTEXT_NAME     = "_NO_CONTEXT_";
    
    static final Logger LOG = LoggerFactory.getLogger(UberDaoBean.class);

    public void init() {
        populate();
        patchRoles();
        patchBranches();
    }
    
    protected static String getParam(Map<String,String[]> params, String name) {
        final String values[] = params.get(name);
        if (null != values && 0 < values.length) {
            return values[0];
        }
        return null;
    }

    @Override
    public Object processTask(String taskName, Map parameterMap) {
        Map<String,String[]> params = parameterMap;
        
        // write XML dump to BlobStore
        if ("dump_xml_to_blob".equals(taskName)) {
            return processDumpXmlToBlob();
        }
        
        // parse XML and persist
        if ("persist_xml_blob".equals(taskName)) {
            return persistXmlBlob(params);
        }
        
        return null;
    }
    
    protected static Proj10 convert(Proj from) {
        if (null == from) {
            return null;
        }
        
        final Proj10 to = new Proj10(from.getName(), from.getOwner());
        
        return to;
    }
    
    protected static List<Proj10> convertProjects(Collection<Proj> from) {
        final List<Proj10> to = new ArrayList<Proj10>();
        
        for (Proj entity : from) {
            to.add(convert(entity));
        }
        
        return to;
    }

    protected static Tokn10 convert(Tokn from) {
        if (null == from) {
            return null;
        }
        
        final Tokn10 to = new Tokn10(from.getId(), from.getName(), from.getDescription());
        
        to.setContext(null != from.getViewContext() ? from.getViewContext().getName() : "");
        
        return to;
    }

    protected static List<Tokn10> convertTokens(List<Tokn> from) {
        final List<Tokn10> to = new ArrayList<Tokn10>();
        
        for (Tokn entity : from) {
            to.add(convert(entity));
        }
        
        Collections.sort(to, TOKEN_COMPARATOR);
        
        return to;
    }

    private void patchBranches() {
        Date date = new Date();
        for(Proj p : projDao.findAll()) {
            for(Branch b : branchDao.findByProject(p.getPrimaryKey())) {
                b._setCreatedDate(date);
                branchDao.update(b);
            }
            p._setCreatedDate(date);
            projDao.update(p);
        }
    }

    private void patchRoles() {
        for(Proj proj : projDao.findAll()) {
            final Key projKey = (Key) proj.getPrimaryKey();
            // add owner
            createUser(projKey, proj.getOwner(), Role.ROLE_OWNER);

            // add Role.DEVELOPER to all users without role
            for(ProjUser pu : projUserDao.findByProj(projKey)) {
                if (null == pu.getRole()) {
                    pu.setRole(Role.ROLE_DEVELOPER);
                    projUserDao.update(pu);
                }
            }
        }
    }

    public static List<Long> intersection(List<Long> a, List<Long> b) {
        final List<Long> u = new ArrayList<Long>();
        for(Long o : a) {
            if (b.contains(o)) {
                u.add(o);
            }
        }
        return u;
    }
    
    public Collection<Proj> getProjectsByUsername(String username) {
        // owned projects first
        List<Proj> projects = new ArrayList<Proj>(projDao.findByOwner(username));

        // then add projects where user
        for(String projName : projDao.findAllKeys()) {
            Key projKey = KeyFactory.createKey(Proj.class.getSimpleName(), projName);
            if (null != projUserDao.findByPrimaryKey(projKey, username)) {
                projects.add(projDao.findByPrimaryKey(projName));
            }
        }
        
        // unique and sort
        final TreeSet<Proj> returnValue = new TreeSet<Proj>(PROJ_COMPARATOR);
        returnValue.addAll(projects);
        return returnValue;
    }
    
    public List<Proj10> getProjects(String username) {
        final Collection<Proj> projects = getProjectsByUsername(username);
        final List<Proj10> returnValue = convertProjects(projects);
        
        // populate JSON properties
        Key projKey, branchKey;
        for (Proj10 p10 : returnValue) {
            projKey = projDao.createKey(p10.getName());
            branchKey = branchDao.createKey(projKey, ProjectHandlerInterceptor.NAME_TRUNK);
            
            // populate languages
            List<ProjLang> langs = new ArrayList<ProjLang>();
            p10.setProjLangs(langs);
            for (ProjLang pl : projLangDao.findByBranch(branchKey)) {
                
                // exclude default language
                if (null == pl.getDefaultLang()) {
                    p10.setDefProjLang(pl);
                }
                else {
                    langs.add(pl);
                }
            }
            
            // populate contexts
            p10.setContexts(ctxtDao.findByBranch(branchKey));
        }
        
        return returnValue;
    }
    
    private static List<Role> _roles = null;
    public List<Role> getRoles() {
        if (null == _roles) {
            _roles = roleDao.findAll();
        }
        return _roles;
    }
    
    public Proj10 getTokens(String username, String projectName, String branchName) {
        final Proj10 proj = new Proj10(projectName, null);
        final Key projKey = projDao.createKey(projectName);
        final Key branchKey = branchDao.createKey(projKey, branchName);
        final List<Tokn> entities = toknDao.findByBranch(branchKey);
        final List<Tokn10> tokens = convertTokens(entities);
        proj.setTokens(tokens);
        
        // map the tokens by id
        final HashMap<Long, Tokn10> tokenMap = new HashMap<Long, Tokn10>();
        for (Tokn10 t : tokens) {
            tokenMap.put(t.getId(), t);
        }
        
        // populate contexts
        proj.setContexts(ctxtDao.findByBranch(branchKey));
        
        // get languages for this project branch
        Tokn10 t10;
        proj.setProjLangs(projLangDao.findByBranch(branchKey));
        for (ProjLang projLang : proj.getProjLangs()) {
            if (null == projLang.getDefaultLang()) {
                proj.setDefProjLang(projLang);
            }

            // fetch translations for this language
            List<Trans> trans = transDao.findByProjLang(projLang.getPrimaryKey());
            for (Trans t : trans) {
                t10 = tokenMap.get(t.getToken());
                t10.getTrans().put(projLang.getLangCode(), t.getLocal());
            }
        }
        
        // get subsets for this project branch
        Key subsetKey;
        proj.setSubsets(subsetDao.findKeysByBranch(branchKey));
        for (String subset : proj.getSubsets()) {
            subsetKey = subsetDao.createKey(branchKey, subset);
            List<Long> subTokens = subsetToknDao.findKeysBySubset(subsetKey);
            
            LOG.debug("Subset {} has {}", subset, subTokens);
            for (Long id : subTokens) {
                t10 = tokenMap.get(id);
                t10.getSubsets().add(subset);
            }
        }
        
        // get project users
        proj.setUsers(projUserDao.findByProj(projKey));
        
        return proj;
    }

    @Override
    public void importBody(HttpServletRequest request, Key branchKey, String langCode, Key ctxtKey, String regexp, String body) {
        LOG.info("matching {} on {}", body, regexp);
        List<String> changes = new ArrayList<String>();
        final Key projLangKey = projLangDao.createKey(branchKey, langCode);

        final Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {
            final String tokenName = matcher.group(1);
            final String value = matcher.group(2);
            LOG.info("found {}={}", tokenName, value);

            try {

                // create new token?
                Tokn token = null;
                List<Tokn> tokens = toknDao.findByBranchName(branchKey, tokenName);
                if (tokens.isEmpty()) {
                    final String change = String.format("C %s %s", langCode, tokenName);
                    LOG.info(change);
                    changes.add(change);
                    token = new Tokn();
                    token.setName(tokenName);
                    token.setBranch(branchKey);
                    token.setViewContext(ctxtKey);
                    toknDao.persist(token);
                }
                else if (1 == tokens.size()) {
                    token = tokens.get(0);
                }
                // if we have multiple tokens with same name, we should ignore this import,
                // as we cannot resolve which token to update.

                if (null != token) {
                    Trans translation = transDao.findByPrimaryKey(projLangKey, token.getId());
                    changes.addAll(updateTrans(projLangKey, token, translation, tokenName, value, true));
                }
            }
            catch (RuntimeException e) {
                LOG.error("Problems importing translation " + value + " for token " + tokenName, e);
            }
        }

        Proj proj = projDao.findByPrimaryKey(branchKey.getParent().getName());
        notifyOwner(proj, branchKey.getName(), langCode, changes, request.getUserPrincipal().getName());
    }

    @Override
    public Collection<TransModel> loadTrans(Key branchKey, Key subsetKey, ProjLang projLang, Key ctxtKey) {
        final Map<Long, TransModel> returnValue = new TreeMap<Long, TransModel>();
        Map<Long, Tokn> tokens;
        if (null == subsetKey) {
            // cannot filter on ctxt and subset at same time
            if (null != ctxtKey) {
                final List<Long> tokenKeys = toknDao.findKeysByViewContext(ctxtKey);
                tokens = toknDao.findByPrimaryKeys(branchKey, tokenKeys);
            }
            else {
                // find all tokens for branch:
                tokens = new TreeMap<Long, Tokn>();
                for(Tokn tokn : toknDao.findByBranch(branchKey)) {
                    tokens.put(tokn.getId(), tokn);
                }
            }
        }
        else {
            // find tokens for subset:
            final List<Long> tokenKeys = subsetToknDao.findKeysBySubset(subsetKey);
            tokens = toknDao.findByPrimaryKeys(branchKey, tokenKeys);
        }

        // fetch translations for this language
        Map<Long, Trans> trans = transDao.findByPrimaryKeys(projLang.getPrimaryKey(), tokens.keySet());
        TransModel model;
        for(Entry<Long, Tokn> entry : tokens.entrySet()) {
            model = new TransModel();
            model.setLocal(trans.get(entry.getKey()));
            model.setToken(entry.getValue());
            model.setKey((Key) (null != model.getLocal() ? model.getLocal().getPrimaryKey() : model.getToken().getPrimaryKey()));
            returnValue.put(entry.getKey(), model);
        }

        // if non-default language, fetch default translations too
        if (null != projLang.getDefaultLang()) {
            final String defLangCode = projLang.getDefaultLang().getName();
            final Key defLangKey = KeyFactory.createKey(branchKey, ProjLang.class.getSimpleName(), defLangCode);
            trans = transDao.findByPrimaryKeys(defLangKey, tokens.keySet());

            // populate existing TransModels
            for(Entry<Long, Trans> entry : trans.entrySet()) {
                model = returnValue.get(entry.getKey());
                model.setParent(entry.getValue());
            }
        }

        // order by name
        List<TransModel> unsorted = new ArrayList<TransModel>(returnValue.values());
        Collections.sort(unsorted);

        return unsorted;
    }

    @Override
    public void notifyOwner(Proj proj, String branchName, String langCode, List<String> changes, String from) {
        notifyOwner(from, proj.getOwner(), proj.getName(), branchName, langCode, changes);
    }

    protected void notifyOwner(String from, String to, String projName, String branchName, String languageCode,
            List<String> changes) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject("Ricotta: changes in " + projName + ":" + branchName + " (" + languageCode + ")");
            StringBuffer sb = new StringBuffer();
            for(String s : changes) {
                sb.append(s);
                sb.append('\n');
            }
            msg.setText(sb.toString());
            Transport.send(msg);

        }
        catch (AddressException e) {
            e.printStackTrace();
        }
        catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /** If translation is null, projectKey, tokenKey and languageKey must be specified! */
    @Override
    public List<String> updateTrans(Key projLangKey, Tokn token, Trans t, String name, String value, boolean delete) {
        final String langCode = projLangKey.getName();
        List<String> returnValue = new ArrayList<String>();
        if (null != t) {
            if (null == token) {
                token = toknDao.findByPrimaryKey(projLangKey.getParent(), t.getToken());
            }
            if (null != value && 0 < value.length()) {
                if (false == value.equals(t.getLocal())) {
                    final String u = String.format("U %s %s=%s, was %s", langCode, token.getName(), value, t.getLocal());
                    t.setLocal(value);
                    transDao.update(t);
                    returnValue.add(u);
                    LOG.debug(u);
                }
            }
            else if (delete) {
                transDao.delete(t);
                final String d = String.format("R %s %s", langCode, token.getName());
                LOG.debug(d);
                returnValue.add(d);
            }
        }
        else {
            // create new translation for token?
            if (null != value && 0 < value.length()) {
                t = new Trans();
                t.setProjLang(projLangKey);
                t.setToken(token.getId());
                t.setLocal(value);
                transDao.persist(t);
                final String c = String.format("A %s %s=%s", langCode, token.getName(), value);
                returnValue.add(c);
                LOG.debug(c);
            }
        }
        return returnValue;
    }
    
    public List<String> updateTrans(String projectName, String branchName, Long tokenId, String langCode, String value) {
        final Key projKey = projDao.createKey(projectName);
        final Key branchKey = branchDao.createKey(projKey, branchName);
        final Key projLangKey = projLangDao.createKey(branchKey, langCode);
        final Tokn tokn = toknDao.findByPrimaryKey(branchKey, tokenId);
        if (null == tokn) {
            return null;
        }
        final Trans t = transDao.findByPrimaryKey(projLangKey, tokenId);
        
        final List<String> log = updateTrans(projLangKey, tokn, t, null, value, null == value || "".equals(value));
        
        return log;
    }

    @Override
    public void copyBranch(Key fromKey, String name, String description) {
        final Key projKey = fromKey.getParent();
        final Key toKey = (Key) createBranch(projKey, name, description);

        // contexts
        for(Ctxt c : ctxtDao.findByBranch(fromKey)) {
            String blobKeyString = null;
            if (null != c.getBlobKey()) {
                blobKeyString = c.getBlobKey().getKeyString();
            }
            createCtxt(toKey, c.getName(), c.getDescription(), blobKeyString);
        }

        // projLang
        for(ProjLang pl : projLangDao.findByBranch(fromKey)) {
            createProjLang(toKey, pl.getLangCode(), pl.getDefaultLang(), pl.getLang());
        }

        // tokens
        for(Tokn t : toknDao.findByBranch(fromKey)) {
            copyTokn(t, toKey);
        }

        // subsets
        for(Subset s : subsetDao.findByBranch(fromKey)) {
            copySubset(s, toKey);
        }
    }

    private void copyTokn(Tokn from, Key branchKey) {
        final Key fromBranch = from.getBranch();
        Object ctxtKey = null;
        if (null != from.getViewContext()) {
            final String ctxtName = from.getViewContext().getName();
            ctxtKey = ctxtDao.createKey(branchKey, ctxtName);
        }
        final Key toKey = (Key) createTokn(branchKey, from.getId(), from.getName(), from.getDescription(), ctxtKey);

        for(ProjLang pl : projLangDao.findByBranch(fromBranch)) {
            final Key projLangKey = (Key) pl.getPrimaryKey();
            Trans t = transDao.findByPrimaryKey(projLangKey, from.getId());
            if (null != t) {
                final Object toPL = projLangDao.createKey(branchKey, pl.getLangCode());
                createTrans(toPL, toKey.getId(), t.getLocal());
            }
        }
    }

    private void copySubset(Subset from, Key branchKey) {
        final Key toKey = (Key) createSubset(branchKey, from.getName(), from.getDescription());
        final Key fromKey = (Key) from.getPrimaryKey();
        for(SubsetTokn st : subsetToknDao.findBySubset(fromKey)) {
            createSubsetTokn(toKey, st.getTokn());
        }
    }

    public Tokn10 createToken(String projectName, String branchName, 
            String name, String description, String context) {
        Tokn10 t10 = null;
        final Key projKey = projDao.createKey(projectName);
        final Key branchKey = branchDao.createKey(projKey, branchName);
        final Tokn tokn = new Tokn();

        tokn.setBranch(branchKey);
        tokn.setName(name);
        tokn.setDescription(description);

        // context reference
        final Key viewContext = NO_CONTEXT_NAME.equals(context) ? null : ctxtDao.createKey(branchKey, context);
        tokn.setViewContext(viewContext);
        LOG.debug("create for {}", tokn);

        toknDao.persist(tokn);

        t10 = convert(tokn);
        
        return t10;
    }

    public static List<Object> getKeys(List entities) {
        final List<Object> returnValue = new ArrayList<Object>();
        for(Object o : entities) {
            returnValue.add(((PrimaryKeyEntity) o).getPrimaryKey());
        }
        return returnValue;
    }

    protected Template persistTemplate(String name, String description) {
        Reader in = new InputStreamReader(getClass().getResourceAsStream("/" + name + ".xml"));
        StringBuffer sb = new StringBuffer();
        char buf[] = new char[256];
        int count;
        try {
            while (0 < (count = in.read(buf))) {
                sb.append(buf, 0, count);
            }
            in.close();
            final Template template = templateDao.persist(name, sb.toString(), description, "text/plain");
            return template;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Populates the database with the basic project - ricotta-ost itself!
     */
    protected final void populate() {
        // populate Lang
        final Lang en = langDao.persist("en", "English");
        final Key enKey = (Key) en.getPrimaryKey();
        final Lang en_GB = langDao.persist("en_GB", "British English");
        final Lang sv = langDao.persist("sv", "Swedish");

        // populate Templates
        final Template ricottaExportAll = persistTemplate("ricotta-export-all", "Export all ricotta projects to XML");
        final Template ricottaExportOld = persistTemplate("ricotta-export-old", "Export all old ricotta projects to XML");
        final Template ricottaExportProject = persistTemplate("ricotta-export-project", "Export an old ricotta project to XML");
        final Template ricottaExportVersion = persistTemplate("ricotta-export-version",
                "Export an old ricotta project version to XML");
        final Template ricottaExportProj = persistTemplate("ricotta-export-proj", "Export a ricotta project to XML");
        final Template ricottaExportBranch = persistTemplate("ricotta-export-branch", "Export a ricotta project branch to XML");

        // populate Roles
        roleDao.persist(Role.ROLE_VIEWER, "Viewer; read-only user");
        roleDao.persist(Role.ROLE_TRANSLATOR, "Translator; can add and edit translations");
        roleDao.persist(Role.ROLE_DEVELOPER, "Developer; can add and edit tokens");
        roleDao.persist(Role.ROLE_OWNER, "Owner; can edit and delete project");

        // Projects
        final Proj proj = projDao.persist("ricotta", "s.o.sandstrom@gmail.com");
        final Key projKey = (Key) proj.getPrimaryKey();
        projUserDao.persist(projKey, "test@example.com", Role.ROLE_OWNER);
        projUserDao.persist(projKey, "developer@example.com", Role.ROLE_DEVELOPER);
        projUserDao.persist(projKey, "translator@example.com", Role.ROLE_TRANSLATOR);
        projUserDao.persist(projKey, "viewer@example.com", Role.ROLE_VIEWER);

        // trunk per project
        final Branch trunk = branchDao.persist(projKey, "trunk", "Latest version");
        final Key branchKey = (Key) trunk.getPrimaryKey();

        // ProjLang
        final ProjLang plEN = projLangDao.persist(branchKey, en.getCode(), null, enKey);
        final ProjLang plGB = projLangDao.persist(branchKey, en_GB.getCode(), enKey, (Key) en_GB.getPrimaryKey());
        final ProjLang plSV = projLangDao.persist(branchKey, sv.getCode(), enKey, (Key) sv.getPrimaryKey());

        // Variant
        final Subset ricottaOst = subsetDao.persist(branchKey, "webapp", "The web app");
        final Subset ricottaPlugin = subsetDao.persist(branchKey, "plugin", "The maven plugin");
        final Subset android = subsetDao.persist(branchKey, "Android", "Android app");

        // Contexts
        final Ctxt projects = ctxtDao.persist(branchKey, "projects view", null, "The projects view");
        final Ctxt home = ctxtDao.persist(branchKey, "Home view", null, "The Home view");

        // Tokens
        final Tokn appTitle = toknDao.persist(branchKey, 1L, "The Application title as displayed to the user", "appTitle",
                projects.getPrimaryKey());
        final Tokn tokenProject = toknDao.persist(branchKey, 2L, "The Project Entity", "project_Project_Android_Specific", null);
        final Tokn tokenLong = toknDao.persist(branchKey, 3L, "A Really Long Token with Translations", "A Really Long Token with Translations", 
                home.getPrimaryKey());

        // subset tokens
        subsetToknDao.persist(ricottaOst.getPrimaryKey(), 1L);
        subsetToknDao.persist(ricottaOst.getPrimaryKey(), 2L);
        subsetToknDao.persist(ricottaPlugin.getPrimaryKey(), 1L);
        subsetToknDao.persist(android.getPrimaryKey(), 1L);

        // Trans
        transDao.persist(plEN.getPrimaryKey(), appTitle.getId(), "Ricotta");
        transDao.persist(plGB.getPrimaryKey(), tokenProject.getId(), "Project");
        transDao.persist(plSV.getPrimaryKey(), tokenProject.getId(), "Projekt");
        transDao.persist(plEN.getPrimaryKey(), tokenLong.getId(), "Ricotta is a Translations management</br>tool with complimentary build tools.");

    }

    // ------------------- export ---------------------
    public List<Proj> getProj() {
        return projDao.findAll();
    }

    public List<Lang> getLang() {
        return langDao.findAll();
    }

    public List<Template> getTemplate() {
        return templateDao.findAll();
    }

    public List<ProjUser> projUsers(Object projKey) {
        return projUserDao.findByProj((Key) projKey);
    }

    public List<Branch> branches(Object projKey) {
        return branchDao.findByProject((Key) projKey);
    }

    public List<ProjLang> langs(Key branchKey) {
        return projLangDao.findByBranch(branchKey);
    }

    public List<Tokn> tokns(Key branchKey) {
        return toknDao.findByBranch(branchKey);
    }

    public List<Ctxt> ctxts(Key branchKey) {
        return ctxtDao.findByBranch(branchKey);
    }

    public Map<Long, List<Trans>> trans(Key branchKey) {
        final Map<Long, List<Trans>> trans = new TreeMap<Long, List<Trans>>();
        for(ProjLang pl : projLangDao.findByBranch(branchKey)) {
            for(Trans t : transDao.findByProjLang((Key) pl.getPrimaryKey())) {
                List<Trans> ts = trans.get(t.getToken());
                if (null == ts) {
                    ts = new ArrayList<Trans>();
                    trans.put(t.getToken(), ts);
                }
                ts.add(t);
            }
        }
        return trans;
    }

    public List<Subset> subsets(Key branchKey) {
        return subsetDao.findByBranch(branchKey);
    }

    public List<Long> subTokns(Key subsetKey) {
        return subsetToknDao.findKeysBySubset(subsetKey);
    }

    // ---------------------- import XML ----------------------------

    @Override
    public Object createBranch(Object proj, String name, String description) {
        final Branch b = branchDao.persist((Key) proj, name, description);
        return b.getPrimaryKey();
    }

    @Override
    public Object createLang(String code, String name) {
        final Lang l = langDao.persist(code, name);
        return l.getPrimaryKey();
    }

    @Override
    public Object createTempl(String name, String description, String body) {
        final Template t = templateDao.persist(name, body, description, null);
        return t.getPrimaryKey();
    }

    @Override
    public Object createProj(String name, String owner) throws IllegalArgumentException {
        final Proj existing = projDao.findByPrimaryKey(name);
        if (null != existing) {
            throw new IllegalArgumentException("Project with name already exists");
        }
        final Proj p = projDao.persist(name, owner);
        createUser(p.getPrimaryKey(), owner, Role.ROLE_OWNER);
        return p.getPrimaryKey();
    }

    @Override
    public Object createUser(Object proj, String email, long role) {
        final ProjUser pu = projUserDao.persist((Key) proj, email, role);
        return pu.getPrimaryKey();
    }

    @Override
    public Object createProjLang(Object branchKey, String langCode, Object defaultLangKey, Object langKey) {
        final ProjLang pl = projLangDao.persist((Key) branchKey, langCode, (Key) defaultLangKey, (Key) langKey);
        return pl.getPrimaryKey();
    }

    @Override
    public Object createCtxt(Object branch, String name, String description, String blobKeyString) {
        BlobKey blobKey = (null != blobKeyString) ? new BlobKey(blobKeyString) : null;
        final Ctxt c = ctxtDao.persist((Key) branch, name, blobKey, description);
        return c.getPrimaryKey();
    }

    @Override
    public Object createTokn(Object branch, Long id, String name, String description, Object ctxtKey) {
        final Tokn t = toknDao.persist((Key) branch, id, description, name, (Key) ctxtKey);
        return t.getPrimaryKey();
    }

    @Override
    public Object createSubset(Object branch, String name, String description) {
        final Subset s = subsetDao.persist((Key) branch, name, description);
        return s.getPrimaryKey();
    }

    @Override
    public Object createTrans(Object projLangKey, Long toknId, String value) {
        final Trans t = transDao.persist((Key) projLangKey, toknId, value);
        return t.getPrimaryKey();
    }

    @Override
    public Object createSubsetTokn(Object subsetKey, Long toknId) {
        final SubsetTokn st = subsetToknDao.persist((Key) subsetKey, toknId);
        return st.getPrimaryKey();
    }

    // --------------------- delete methods ----------------------

    @Override
    public void deleteTokns(List<Key> keys) {
        if (null != keys && !keys.isEmpty()) {
            final Key branchKey = keys.get(0).getParent();

            // prepare ids
            final List<Long> ids = new ArrayList<Long>();
            for(Key t : keys) {
                ids.add(t.getId());
            }

            // delete all translations for branch's tokens
            final List<String> projLangs = projLangDao.findKeysByBranch(branchKey);
            for(String pl : projLangs) {
                Key plKey = KeyFactory.createKey(branchKey, ProjLang.class.getSimpleName(), pl);
                List<Long> transKeys = transDao.findKeysByProjLang(plKey);
                transDao.delete(plKey, intersection(transKeys, ids));
            }

            // delete all subsetTokns for branch
            final List<String> subsets = subsetDao.findKeysByBranch(branchKey);
            for(String subsetName : subsets) {
                Key sKey = KeyFactory.createKey(branchKey, Subset.class.getSimpleName(), subsetName);
                List<Long> stKeys = subsetToknDao.findKeysBySubset(sKey);
                subsetToknDao.delete(sKey, intersection(stKeys, ids));
            }

            // finally, delete the tokens
            toknDao.delete(branchKey, ids);
        }
    }

    @Override
    public void deleteBranch(Key branchKey) {
        // tokens
        List<Long> ids = toknDao.findKeysByBranch(branchKey);
        List<Key> keys = new ArrayList<Key>();
        for(Long id : ids) {
            keys.add(KeyFactory.createKey(branchKey, Tokn.class.getSimpleName(), id));
        }
        deleteTokns(keys);

        // projLangs
        List<String> codes = projLangDao.findKeysByBranch(branchKey);
        projLangDao.delete(branchKey, codes);

        // contexts
        List<String> names = ctxtDao.findKeysByBranch(branchKey);
        ctxtDao.delete(branchKey, names);

        // subsets
        List<String> subsets = subsetDao.findKeysByBranch(branchKey);
        subsetDao.delete(branchKey, subsets);

        // and itself
        branchDao.deleteByCore(branchKey);
    }

    public void deleteProj(Key projKey) {
        // users
        List<String> userKeys = projUserDao.findKeysByProj(projKey);
        projUserDao.delete(projKey, userKeys);

        // branches
        for(String branchName : branchDao.findKeysByProject(projKey)) {
            deleteBranch(KeyFactory.createKey(projKey, Branch.class.getSimpleName(), branchName));
        }

        // the proj
        projDao.deleteByCore(projKey);
    }
    
    public Proj getProj(String name) {
        return projDao.findByPrimaryKey(name);
    }

    protected static final Comparator<Proj> PROJ_COMPARATOR = new Comparator<Proj>() {

        @Override
        public int compare(Proj o1, Proj o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
        
    };
    
    protected static final Comparator<Tokn10> TOKEN_COMPARATOR = new Comparator<Tokn10>() {

        @Override
        public int compare(Tokn10 o1, Tokn10 o2) {
            int returnValue = o1.getName().compareToIgnoreCase(o2.getName());
            if (0 == returnValue) {
                returnValue = o2.getContext().compareToIgnoreCase(o2.getContext());
            }
            return returnValue;
        }
    };

    public Tokn10 updateToken(String projectName, String branchName, Long tokenId, 
            String name, String description, String context, String[] subs) {
        Tokn10 t10 = null;
        final Key projKey = projDao.createKey(projectName);
        final Key branchKey = branchDao.createKey(projKey, branchName);
        final Tokn tokn = toknDao.findByPrimaryKey(branchKey, tokenId);

        LOG.debug("update {} for {}", tokenId, tokn);
        
        if (null != tokn) {
            Set<String> subSet = new HashSet<String>();
            Collections.addAll(subSet, subs);
            tokn.setName(name);
            tokn.setDescription(description);
            
            // context reference
            final Key viewContext = NO_CONTEXT_NAME.equals(context) ? null : ctxtDao.createKey(branchKey, context);
            tokn.setViewContext(viewContext);
            
            toknDao.update(tokn);
            
            // subsets (mutable)
            final List<Key> existing = subsetToknDao.findKeysByBranchKeyTokenId(branchKey, tokenId);
            final ArrayList<Key> remove = new ArrayList<Key>(existing);
            for (Key stKey : existing) {
                if (subSet.remove(stKey.getParent().getName())) {
                    remove.remove(stKey);
                }
            }
            
            // remove remaining existing, insert remaining from subSet
            LOG.debug("deleting {}, inserting {}", remove, subSet);
            subsetToknDao.deleteByCore(remove);
            final List<SubsetTokn> insert = new ArrayList<SubsetTokn>();
            SubsetTokn st;
            for (String s : subSet) {
                st = new SubsetTokn();
                st.setSubset(subsetDao.createKey(branchKey, s));
                st.setTokn(tokenId);
                insert.add(st);
            }
            subsetToknDao.persist(insert);
            
            t10 = convert(tokn);
        }
        
        return t10;
    }

    public ProjUser updateUser(String keyString, Long role) {
        ProjUser pu = null;
        final Key projUserKey = KeyFactory.stringToKey(keyString);
        final Key projKey = projUserKey.getParent();
        
        pu = projUserDao.findByPrimaryKey(projKey, projUserKey.getName());
        if (null != pu) {
            pu.setRole(role);
            projUserDao.update(pu);
        }
        
        return pu;
    }

    private Object processDumpXmlToBlob() {
        try {
            final BlobKey returnValue = AEDDaoImpl.xmlWriteToBlobs(
                    langDao, projDao, roleDao, templateDao, // AppUserDao,
                    projUserDao, branchDao,
                    ctxtDao, projLangDao, subsetDao,
                    toknDao, transDao, subsetToknDao);
            return returnValue;
        } catch (IOException ex) {
            LOG.error("IOException dumping XML", ex);
            return ex.getMessage();
        } catch (SAXException ex) {
            LOG.error("SAXException dumping XML", ex);
            return ex.getMessage();
        } catch (TransformerConfigurationException ex) {
            LOG.error("TransformerConfigurationException dumping XML", ex);
            return ex.getMessage();
        }
    }
    
    protected Object persistXmlBlob(Map<String,String[]> params) {
        try {
            String baseUrl = getParam(params, "baseUrl");
            String blobKey = getParam(params, "blobKey");

            if (null != baseUrl && null != blobKey) {
                AEDDaoImpl.xmlParseFromBlobs(baseUrl, blobKey,
                        langDao, projDao, roleDao, templateDao, // AppUserDao,
                        projUserDao, branchDao,
                        ctxtDao, projLangDao, subsetDao,
                        toknDao, transDao, subsetToknDao);
            }
            return blobKey;
        } catch (IOException ex) {
            LOG.error("IOException dumping XML", ex);
            return ex.getMessage();
        } catch (SAXException ex) {
            LOG.error("SAXException dumping XML", ex);
            return ex.getMessage();
        } catch (ParserConfigurationException ex) {
            LOG.error("ParserConfigurationException dumping XML", ex);
            return ex.getMessage();
        }
    }
}

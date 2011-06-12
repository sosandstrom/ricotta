package com.wadpam.ricotta.dao;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.wadpam.ricotta.web.AbstractDaoController;

public class UberDaoBean extends AbstractDaoController implements UberDao {
    static final Logger LOG = LoggerFactory.getLogger(UberDaoBean.class);

    public void init() {
        populate();
        // patchRoles();
    }

    private void patchRoles() {
        for(Proj proj : projDao.findAll()) {
            final Key projKey = (Key) proj.getPrimaryKey();
            // add owner just in case
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

    @Override
    public void importBody(HttpServletRequest request, Key branchKey, String langCode, String regexp, String body) {
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
        projUserDao.persist(projKey, "test@example.com", Role.ROLE_VIEWER);

        // trunk per project
        final Branch trunk = branchDao.persist(projKey, "trunk", "2011-01-28 10:10 GMT+7", "Latest version");
        final Key branchKey = (Key) trunk.getPrimaryKey();

        // ProjLang
        final ProjLang plEN = projLangDao.persist(branchKey, en.getCode(), null, enKey);
        final ProjLang plGB = projLangDao.persist(branchKey, en_GB.getCode(), enKey, (Key) en_GB.getPrimaryKey());
        final ProjLang plSV = projLangDao.persist(branchKey, sv.getCode(), enKey, (Key) sv.getPrimaryKey());

        // Variant
        final Subset ricottaOst = subsetDao.persist(branchKey, "ricotta-ost", "The web app");
        final Subset ricottaPlugin = subsetDao.persist(branchKey, "ricotta-maven-plugin", "The maven plugin");

        // Contexts
        final Ctxt projects = ctxtDao.persist(branchKey, "projects", null, "The projects view");

        // Tokens
        final Tokn appTitle = toknDao.persist(branchKey, 1L, "The Application title as displayed to the user", "appTitle",
                (Key) projects.getPrimaryKey());
        final Tokn tokenProject = toknDao.persist(branchKey, 2L, "The Project Entity", "Project", null);

        // subset tokens
        final SubsetTokn appTitleOst = subsetToknDao.persist(ricottaOst.getPrimaryKey(), 1L);
        subsetToknDao.persist(ricottaOst.getPrimaryKey(), 2L);
        subsetToknDao.persist(ricottaPlugin.getPrimaryKey(), 1L);

        // Trans
        transDao.persist(plEN.getPrimaryKey(), appTitle.getId(), "Ricotta");
        transDao.persist(plGB.getPrimaryKey(), tokenProject.getId(), "Project");
        transDao.persist(plSV.getPrimaryKey(), tokenProject.getId(), "Projekt");

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
        final Branch b = branchDao.persist(proj, name, null, description);
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
    public Object createProj(String name, String owner) {
        final Proj p = projDao.persist(name, owner);
        createUser(p.getPrimaryKey(), owner, Role.ROLE_OWNER);
        return p.getPrimaryKey();
    }

    @Override
    public Object createUser(Object proj, String email, long role) {
        final ProjUser pu = projUserDao.persist(proj, email, role);
        return pu.getPrimaryKey();
    }

    @Override
    public Object createProjLang(Object branchKey, String langCode, Object defaultLangKey, Object langKey) {
        final ProjLang pl = projLangDao.persist(branchKey, langCode, (Key) defaultLangKey, (Key) langKey);
        return pl.getPrimaryKey();
    }

    @Override
    public Object createCtxt(Object branch, String name, String description, String blobKeyString) {
        BlobKey blobKey = (null != blobKeyString) ? new BlobKey(blobKeyString) : null;
        final Ctxt c = ctxtDao.persist(branch, name, blobKey, description);
        return c.getPrimaryKey();
    }

    @Override
    public Object createTokn(Object branch, Long id, String name, String description, Object ctxtKey) {
        final Tokn t = toknDao.persist(branch, id, description, name, (Key) ctxtKey);
        return t.getPrimaryKey();
    }

    @Override
    public Object createSubset(Object branch, String name, String description) {
        final Subset s = subsetDao.persist(branch, name, description);
        return s.getPrimaryKey();
    }

    @Override
    public Object createTrans(Object projLangKey, Long toknId, String value) {
        final Trans t = transDao.persist(projLangKey, toknId, value);
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
        branchDao.delete(branchKey);
    }

    public void deleteProj(Key projKey) {
        // users
        for(String u : projUserDao.findKeysByProj(projKey)) {
            projUserDao.delete(projKey, u);
        }

        // branches
        for(String branchName : branchDao.findKeysByProject(projKey)) {
            deleteBranch(KeyFactory.createKey(projKey, Branch.class.getSimpleName(), branchName));
        }

        // the proj
        projDao.delete(projKey);
    }
}

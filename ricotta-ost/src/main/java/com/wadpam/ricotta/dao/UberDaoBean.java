package com.wadpam.ricotta.dao;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.domain.Artifact;
import com.wadpam.ricotta.domain.Branch;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.Lang;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.Mall;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.ProjectLanguage;
import com.wadpam.ricotta.domain.ProjectUser;
import com.wadpam.ricotta.domain.Subset;
import com.wadpam.ricotta.domain.SubsetTokn;
import com.wadpam.ricotta.domain.Template;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.TokenArtifact;
import com.wadpam.ricotta.domain.Tokn;
import com.wadpam.ricotta.domain.Trans;
import com.wadpam.ricotta.domain.Translation;
import com.wadpam.ricotta.domain.Version;
import com.wadpam.ricotta.domain.ViewContext;
import com.wadpam.ricotta.model.ProjectLanguageModel;
import com.wadpam.ricotta.model.TransModel;
import com.wadpam.ricotta.model.TranslationModel;
import com.wadpam.ricotta.web.AbstractDaoController;

public class UberDaoBean extends AbstractDaoController implements UberDao {
    static final Logger        LOG                           = LoggerFactory.getLogger(UberDaoBean.class);

    public static final String MALL_BODY_ANDROID             = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                                                     + "<!-- Project: ${project.name} -->\n"
                                                                     + "<!-- Language: ${language.name} (${language.code}) -->\n"
                                                                     + "<!-- Template name: ${mall.name} -->\n"
                                                                     + "<!-- Artifact name: ${artifact.name} -->\n\n"
                                                                     + "<resources>\n"
                                                                     + "#foreach( $t in $translations )#if( $t.local )\n"
                                                                     + "        <string name=\"${t.token.name}\">${encoder.android($t.local.local)}</string>\n"
                                                                     + "#elseif( $t.parent )\n"
                                                                     + "        <string name=\"${t.token.name}\">${encoder.android($t.parent.local)}</string>\n"
                                                                     + "#end#end\n" + "</resources>";

    private static Cache       _cache                        = null;

    static final String        CACHE_KEY_TRANSLATIONS_PREFIX = "loadTranslations";

    private ArtifactDao        artifactDao;
    private LanguageDao        languageDao;
    private MallDao            mallDao;
    private ProjectDao         projectDao;
    private ProjectLanguageDao projectLanguageDao;
    private ProjectUserDao     projectUserDao;
    private TokenDao           tokenDao;
    private TokenArtifactDao   tokenArtifactDao;
    private TranslationDao     translationDao;
    private VersionDao         versionDao;
    private ViewContextDao     viewContextDao;

    private Version            _HEAD                         = null;

    public void init() {
        Key HEAD = populate();
        // moved to ProjectController.patchAll()
        // patch(HEAD);
    }

    // ------------------ methods managing the cache ------------------------------

    public static Cache getCache() throws CacheException {
        if (null == _cache) {
            final CacheManager manager = CacheManager.getInstance();
            CacheFactory cacheFactory = manager.getCacheFactory();
            _cache = cacheFactory.createCache(Collections.emptyMap());
        }
        return _cache;
    }

    public static String cacheKeyLoadTranslations(Key projectKey, Key versionKey, Key languageKey, Key artifactKey) {
        return CACHE_KEY_TRANSLATIONS_PREFIX + projectKey + versionKey
                + (null == languageKey ? "" : languageKey.toString() + (null != artifactKey ? artifactKey : ""));
    }

    public void invalidateAll() {
        LOG.debug("invalidating ALL");
        try {
            final Cache cache = getCache();
            cache.clear();
        }
        catch (CacheException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void invalidateCache(Key projectKey, Key versionKey, Key languageKey, Key artifactKey) {
        if (null != projectKey) {

            // all languages or specific?
            if (null == languageKey) {
                // get the default language
                ProjectLanguage pl = projectLanguageDao.findDefault(projectKey, versionKey);
                // LOG.debug("invalidateCache {} PL {}", languageKey, pl);
                // invalidate defualt language first:
                invalidateCache(projectKey, versionKey, pl.getLanguage(), artifactKey);
            }
            else {
                // get the specified language
                ProjectLanguage pl = projectLanguageDao.findByLanguageProjectVersion(languageKey, projectKey, versionKey);
                // LOG.debug("invalidateCache {} PL {}", languageKey, pl);
                // invalidate sub-languages (NOT recursive, only one level)
                for(ProjectLanguage spl : projectLanguageDao.findByParent(pl.getKey())) {
                    invalidateCacheInternal(spl.getProject(), versionKey, spl.getLanguage(), artifactKey);
                }

                // all artifacts or specific?
                if (null == artifactKey) {
                    // project-language first
                    invalidateCacheInternal(projectKey, versionKey, languageKey, null);

                    // all!
                    for(Key a : artifactDao.findKeysByProject(projectKey)) {
                        invalidateCache(projectKey, versionKey, languageKey, a);
                    }
                }
                else {
                    // base: invalidate this
                    invalidateCacheInternal(projectKey, versionKey, languageKey, artifactKey);
                }

            }
        }
    }

    /** For exact project-language-artifact */
    protected void invalidateCacheInternal(Key projectKey, Key versionKey, Key languageKey, Key artifactKey) {
        final String cacheKey = cacheKeyLoadTranslations(projectKey, versionKey, languageKey, artifactKey);
        LOG.debug("invalidating {}" + cacheKey);
        try {
            final Cache cache = getCache();
            cache.remove(cacheKey);
        }
        catch (CacheException e) {
            e.printStackTrace();
        }
    }

    // ------------------ methods managing the cache ------------------------------

    @Override
    public void cloneVersion(Project project, Key from, Version version) {
        LOG.info("--- cloning {} version {}", project.getName(), version.getName());
        // project & artifacts are "immutable"

        // project languages
        ProjectLanguage root = projectLanguageDao.findDefault(project.getKey(), from);
        cloneProjectLanguage(root, version.getKey());

        // tokens
        List<Token> tokens = tokenDao.findByProjectVersion(project.getKey(), from, true);
        LOG.error("--- cloning {} tokens", tokens.size());
        for(Token t : tokens) {
            cloneToken(t, version.getKey());
        }
    }

    protected void cloneProjectLanguage(ProjectLanguage projectLanguage, Key toVersion) {
        // load children for old version before cloning:
        List<ProjectLanguage> children = projectLanguageDao.findByProjectParentVersion(projectLanguage.getProject(),
                projectLanguage.getLanguage(), projectLanguage.getVersion());

        // clone
        projectLanguage.setKey(null);
        projectLanguage.setVersion(toVersion);
        projectLanguageDao.persist(projectLanguage);

        // and clone children recursively
        for(ProjectLanguage child : children) {
            cloneProjectLanguage(child, toVersion);
        }
    }

    protected void cloneToken(Token t, Key toVersion) {
        // retrieve before cloning
        List<TokenArtifact> tas = tokenArtifactDao.findByToken(t.getKey());
        List<Translation> ts = translationDao.findByTokenVersion(t.getKey(), t.getVersion());

        // clone
        t.setKey(null);
        t.setVersion(toVersion);
        tokenDao.persist(t);

        // clone tokenArtifacts
        for(TokenArtifact ta : tas) {
            cloneTokenArtifact(ta, t.getKey(), toVersion);
        }

        // and translations
        for(Translation tr : ts) {
            cloneTranslation(tr, t.getKey(), toVersion);
        }
    }

    protected void cloneTranslation(Translation t, Key tokenKey, Key toVersion) {
        t.setKey(null);
        t.setToken(tokenKey);
        t.setVersion(toVersion);
        translationDao.persist(t);
    }

    protected void cloneTokenArtifact(TokenArtifact ta, Key tokenKey, Key toVersion) {
        ta.setKey(null);
        ta.setToken(tokenKey);
        ta.setVersion(toVersion);
        tokenArtifactDao.persist(ta);
    }

    @Override
    public void deleteTokens(List<Key> keys) {
        List<Key> taKeys;
        for(Key tokenKey : keys) {
            // delete associated TokenArtifacts:
            taKeys = tokenArtifactDao.findKeysByToken(tokenKey);
            tokenArtifactDao.delete(taKeys);

            // delete associated translations:
            taKeys = translationDao.findKeysByToken(tokenKey);
            translationDao.delete(taKeys);
        }

        tokenDao.delete(keys);
    }

    @Override
    public void deleteVersion(Project project, String vk) {
        final Key versionKey = KeyFactory.stringToKey(vk);

        // ProjectLanguages
        projectLanguageDao.delete(projectLanguageDao.findKeysByVersion(versionKey));

        // Tokens (incl TokenArtifacts and Translations)
        deleteTokens(tokenDao.findKeysByVersion(versionKey));

        // and the version itself
        versionDao.delete(Arrays.asList(versionKey));
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
                else {
                    token = tokens.get(0);
                }

                Trans translation = transDao.findByPrimaryKey(projLangKey, token.getId());
                changes.addAll(updateTrans(projLangKey, token, translation, tokenName, value, true));
            }
            catch (RuntimeException e) {
                LOG.error("Problems importing translation " + value + " for token " + tokenName, e);
            }
        }

        Proj proj = projDao.findByPrimaryKey(branchKey.getParent().getName());
        notifyOwner(proj, branchKey.getName(), langCode, changes, request.getUserPrincipal().getName());
    }

    @Override
    public List<ProjectLanguageModel> loadProjectLanguages(Key project, Key version) {
        final List<ProjectLanguageModel> returnValue = new ArrayList<ProjectLanguageModel>();

        final List<ProjectLanguage> pls = projectLanguageDao.findByProjectVersion(project, version);

        // create a key-value map
        HashMap<Key, ProjectLanguage> plMap = new HashMap<Key, ProjectLanguage>();
        for(ProjectLanguage pl : pls) {
            plMap.put(pl.getKey(), pl);
        }

        // create the model list
        ProjectLanguageModel plm;
        Language parentLanguage;
        for(ProjectLanguage pl : pls) {
            plm = new ProjectLanguageModel();
            returnValue.add(plm);

            plm.setLanguage(languageDao.findByPrimaryKey(pl.getLanguage()));

            if (null != pl.getParent()) {
                parentLanguage = languageDao.findByPrimaryKey(pl.getParent());
                plm.setParentName(parentLanguage.getName());
            }
        }

        return returnValue;
    }

    @Override
    public Collection<TransModel> loadTrans(Key branchKey, Key subsetKey, ProjLang projLang, Key ctxtKey) {
        final Map<Long, TransModel> returnValue = new TreeMap<Long, TransModel>();
        Map<Long, Tokn> tokens;
        if (null == subsetKey) {
            // find all tokens for branch:
            tokens = new TreeMap<Long, Tokn>();
            for(Tokn tokn : toknDao.findByBranch(branchKey)) {
                tokens.put(tokn.getId(), tokn);
            }
        }
        else {
            // find tokens for subset:
            final List<SubsetTokn> subTokens = subsetToknDao.findBySubset(subsetKey);
            final List<Long> tokenKeys = new ArrayList<Long>();
            for(SubsetTokn st : subTokens) {
                tokenKeys.add(st.getTokn());
            }
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

        return returnValue.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TranslationModel> loadTranslations(Key projectKey, Key versionKey, Key languageKey, Key artifactKey) {
        final List<TranslationModel> returnValue = new ArrayList<TranslationModel>();
        try {
            // check cache first!
            Cache cache = getCache();
            final String cacheKey = cacheKeyLoadTranslations(projectKey, versionKey, languageKey, artifactKey);
            final List<TranslationModel> tms = (List<TranslationModel>) cache.get(cacheKey);
            LOG.debug("Cache hit for {}: {}", cacheKey, tms);
            if (null != tms) {
                return tms;
            }

            final ProjectLanguage projectLanguage = projectLanguageDao.findByLanguageProjectVersion(languageKey, projectKey,
                    versionKey);
            List<Token> tokens = null;
            if (null != artifactKey) {
                List<TokenArtifact> mappings = tokenArtifactDao.findByArtifactVersion(artifactKey, versionKey);
                tokens = new ArrayList<Token>();
                Token token;
                for(TokenArtifact ta : mappings) {
                    token = tokenDao.findByPrimaryKey(ta.getToken());
                    // if you have deleted tokens using the admin console...
                    if (null != token) {
                        tokens.add(token);
                    }
                    else {
                        // cleanup
                        LOG.warn("Removing orphaned {}", ta);
                        tokenArtifactDao.delete(ta);
                    }
                }
            }
            else {
                tokens = tokenDao.findByProjectVersion(projectKey, versionKey, true);
            }
            final Set<Key> tokenKeys = new HashSet<Key>();
            for(Token t : tokens) {
                tokenKeys.add(t.getKey());
            }
            LOG.debug("fetched {} tokens for {} keys", tokens.size(), tokenKeys.size());

            // this language's tokens
            final Map<Key, Translation> locals = translationDao.findByLanguageKeyTokens(projectKey, languageKey, tokenKeys);

            // if there is a parent, get its tokens
            Map<Key, Translation> parents = new HashMap<Key, Translation>();
            if (null != projectLanguage.getParent()) {
                parents = translationDao.findByLanguageKeyTokens(projectKey, projectLanguage.getParent(), tokenKeys);
            }

            // for each token, build a TranslationModel
            TranslationModel tm;
            Translation local;
            for(Token token : tokens) {
                tm = new TranslationModel();
                returnValue.add(tm);
                tm.setToken(token);
                local = locals.get(token.getKey());
                tm.setLocal(local);
                tm.setParent(parents.get(token.getKey()));

                // if locally translated, pass translation key, otherwise token key
                tm.setKey(null != local ? local.getKey() : token.getKey());
            }

            Collections.sort(returnValue, new Comparator<TranslationModel>() {
                @Override
                public int compare(TranslationModel arg0, TranslationModel arg1) {
                    return arg0.getToken().getName().compareToIgnoreCase(arg1.getToken().getName());
                }
            });

            cache.put(cacheKeyLoadTranslations(projectKey, versionKey, languageKey, artifactKey), returnValue);
        }
        catch (CacheException e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    @Override
    public void notifyOwner(Proj proj, String branchName, String langCode, List<String> changes, String from) {
        notifyOwner(from, proj.getOwner(), proj.getName(), branchName, langCode, changes);
    }

    @Override
    public void notifyOwner(Project project, Version version, String languageCode, List<String> changes, String from) {
        notifyOwner(from, project.getOwner(), project.getName(), version.getName(), languageCode, changes);
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

    public static List<Object> getKeys(List entities) {
        final List<Object> returnValue = new ArrayList<Object>();
        for(Object o : entities) {
            returnValue.add(((PrimaryKeyEntity) o).getPrimaryKey());
        }
        return returnValue;
    }

    /**
     * Patches any old persisted data
     */
    public void patch(final Key HEAD) {
        // ProjectLanguages needs version
        for(ProjectLanguage pl : projectLanguageDao.findAll()) {
            if (null == pl.getVersion()) {
                LOG.warn("HEAD ProjectLanguage {}", pl);
                pl.setVersion(HEAD);
                projectLanguageDao.update(pl);
            }
        }

        // token-related
        Map<Key, Key> tokenProjectMap = new HashMap<Key, Key>();
        for(Token t : tokenDao.findAll()) {
            tokenProjectMap.put(t.getKey(), t.getProject());

            // add token version?
            if (null == t.getVersion()) {
                LOG.warn("HEAD token {}", t.getName());
                t.setVersion(HEAD);
                tokenDao.update(t);
            }

            // add TokenArtifact version?
            for(TokenArtifact ta : tokenArtifactDao.findByToken(t.getKey())) {
                if (null == ta.getVersion()) {
                    LOG.warn("HEAD TokenArtifact {}, {}", t.getName(), ta.getKey());
                    ta.setVersion(HEAD);
                    tokenArtifactDao.update(ta);
                }
            }

            // patch all translations:
            for(Translation tr : translationDao.findByToken(t.getKey())) {
                boolean patch = (null == tr.getProject() || null == tr.getVersion());
                if (null == tr.getProject()) {
                    LOG.warn("Project {} {} " + tr.getLocal(), t.getName(), tr.getLanguage());
                    tr.setProject(tokenProjectMap.get(tr.getToken()));
                }

                if (null == tr.getVersion()) {
                    LOG.warn("HEAD translation {} {} " + tr.getLocal(), t.getName(), tr.getLanguage());
                    tr.setVersion(HEAD);
                }

                if (patch) {
                    translationDao.update(tr);
                }
            }

        }

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
    protected final Key populate() {
        // HEAD version is cross-project
        _HEAD = versionDao.persist(null, "2011-01-28 10:10 GMT+7", "Latest version", VALUE_HEAD, null);
        final Key HEAD = _HEAD.getKey();

        {
            // populate Languages
            final Language en = languageDao.persist("en", "English");
            final Language en_GB = languageDao.persist("en_GB", "British English");
            final Language sv = languageDao.persist("sv", "Swedish");

            // populate Templates
            final Mall androidStringsInherited = mallDao.persist(MALL_BODY_ANDROID,
                    "Android strings.xml with parent default translations", "text/plain", "strings_android_inherit");

            // Projects
            final Project project = projectDao.persist("ricotta", "s.o.sandstrom@gmail.com");
            projectUserDao.persist(project.getKey(), "test@example.com");

            // ProjectLanguages
            ProjectLanguage root = projectLanguageDao.persist(null, en.getKey(), null, project.getKey(), HEAD);
            projectLanguageDao.persist(null, en_GB.getKey(), en.getKey(), project.getKey(), HEAD);
            projectLanguageDao.persist(null, sv.getKey(), en.getKey(), project.getKey(), HEAD);

            // Artifact
            final Artifact ricottaOst = artifactDao.persist(project.getKey(), "ricotta-ost");
            final Artifact ricottaPlugin = artifactDao.persist(project.getKey(), "ricotta-maven-plugin");

            // Tokens
            final Token appTitle = tokenDao.persist(null, "The Application title as displayed to the user", "appTitle",
                    project.getKey(), HEAD, null);
            final Token tokenProject = tokenDao.persist(null, "The Project Entity", "Project", project.getKey(), HEAD, null);

            // Artifact tokens
            final TokenArtifact appTitleOst = tokenArtifactDao.persist(null, ricottaOst.getKey(), project.getKey(),
                    appTitle.getKey(), HEAD);
            tokenArtifactDao.persist(null, ricottaOst.getKey(), project.getKey(), tokenProject.getKey(), HEAD);
            tokenArtifactDao.persist(null, ricottaPlugin.getKey(), project.getKey(), appTitle.getKey(), HEAD);

            // Translations
            translationDao.persist(null, en.getKey(), "Ricotta", project.getKey(), appTitle.getKey(), HEAD);
            translationDao.persist(null, en_GB.getKey(), "Project", project.getKey(), tokenProject.getKey(), HEAD);
            translationDao.persist(null, sv.getKey(), "Projekt", project.getKey(), tokenProject.getKey(), HEAD);
        }
        {
            // populate Lang
            final Lang en = langDao.persist("en", "English");
            final Key enKey = (Key) en.getPrimaryKey();
            final Lang en_GB = langDao.persist("en_GB", "British English");
            final Lang sv = langDao.persist("sv", "Swedish");

            // populate Templates
            final Template androidStringsInherited = templateDao.persist("strings_android_inherit", MALL_BODY_ANDROID,
                    "Android strings.xml with parent default translations", "text/plain");
            final Template ricottaExportAll = persistTemplate("ricotta-export-all", "Export all ricotta projects to XML");
            final Template ricottaExportOld = persistTemplate("ricotta-export-old", "Export all old ricotta projects to XML");
            final Template ricottaExportProject = persistTemplate("ricotta-export-project",
                    "Export an old ricotta project to XML");
            final Template ricottaExportVersion = persistTemplate("ricotta-export-version",
                    "Export an old ricotta project version to XML");

            // Projects
            final Proj proj = projDao.persist("ricotta", "s.o.sandstrom@gmail.com");
            final Key projKey = (Key) proj.getPrimaryKey();
            projUserDao.persist(projKey, "test@example.com");

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
            final SubsetTokn appTitleOst = subsetToknDao.persist(ricottaOst.getPrimaryKey(), appTitle.getName(), 1L);
            subsetToknDao.persist(ricottaOst.getPrimaryKey(), tokenProject.getName(), 2L);
            subsetToknDao.persist(ricottaPlugin.getPrimaryKey(), appTitle.getName(), 1L);

            // Trans
            transDao.persist(plEN.getPrimaryKey(), appTitle.getId(), "Ricotta");
            transDao.persist(plGB.getPrimaryKey(), tokenProject.getId(), "Project");
            transDao.persist(plSV.getPrimaryKey(), tokenProject.getId(), "Projekt");

        }
        // FIXME: remove test entities
        // Proj proj = projDao.persist("proj", "test@example.com");
        // Branch head = branchDao.persist((Key) proj.getPrimaryKey(), "trunk", "2011-02-20", "proj's trunk");
        // Branch integration = branchDao.persist((Key) proj.getPrimaryKey(), "integration", "2011-02-20",
        // "proj's integration branch");
        //
        // Proj other = projDao.persist("other", "s.o.sandstrom@gmail.com");
        // Branch trunk = branchDao.persist((Key) other.getPrimaryKey(), "trunk", "2011-02-26", "other's trunk");
        // projUserDao.persist(other.getPrimaryKey(), "test@example.com");
        return HEAD;
    }

    @Override
    public Version getHead() {
        if (null == _HEAD) {
            _HEAD = versionDao.findByNameProject(UberDao.VALUE_HEAD, null);
            LOG.info("Initializing HEAD: {}", _HEAD);
        }
        return _HEAD;
    }

    public void upgradeAll() {
        for(Language l : languageDao.findAll()) {
            langDao.persist(l.getCode(), l.getName());
        }

        for(Mall m : mallDao.findAll()) {
            templateDao.persist(m.getName(), m.getBody(), m.getDescription(), m.getMimeType());
        }

        for(Project project : projectDao.findAll()) {
            upgradeProject(project);
        }
    }

    public void upgradeProject(Project project) {
        LOG.info("PROJ {}", project.getName());
        final Proj proj = projDao.persist(project.getName(), project.getOwner());

        // project users
        for(ProjectUser pu : projectUserDao.findByProject(project.getKey())) {
            projUserDao.persist(proj.getPrimaryKey(), pu.getUser());
        }

        // old HEAD singleton
        upgradeVersion(project, getHead(), proj.getPrimaryKey(), "trunk");

        // each version
        for(Version v : versionDao.findByProject(project.getKey())) {
            upgradeVersion(project, v, proj.getPrimaryKey(), v.getName());
        }
    }

    public void upgradeVersion(Project project, Version version, Object projKey, String name) {
        LOG.info("    BRANCH {} for {}", name, project.getName());
        final Branch b = branchDao.persist(projKey, name, version.getDatum(), version.getDescription());

        // ProjLang s
        Map<Key, String> langMap = new HashMap<Key, String>();
        for(ProjectLanguage pl : projectLanguageDao.findByVersion(version.getKey())) {
            // add language code to langMap:
            Language lang = languageDao.findByPrimaryKey(pl.getLanguage());
            if (null == lang) {
                LOG.warn("No language {} for projLang {}", pl.getLanguage(), pl);
            }
            else {
                langMap.put(pl.getLanguage(), lang.getCode());
                Key defKey = null;
                if (null != pl.getParent()) {
                    Language def = languageDao.findByPrimaryKey(pl.getParent());
                    defKey = KeyFactory.createKey(Lang.class.getSimpleName(), def.getCode());
                }
                LOG.info("        PROJ_LANG {} for {}", lang.getCode(), name);
                ProjLang projLang = projLangDao.persist(b.getPrimaryKey(), lang.getCode(), defKey,
                        KeyFactory.createKey(Lang.class.getSimpleName(), lang.getCode()));
                LOG.info("           returned {} for {}", projLang);
            }

        }

        // Artifacts; create one per branch
        Map<Key, Subset> artifactMap = new HashMap<Key, Subset>();
        for(Artifact a : artifactDao.findByProject(project.getKey())) {
            LOG.info("        SUBSET {} for {}", a.getName(), name);
            Subset subset = subsetDao.persist(b.getPrimaryKey(), a.getName(), a.getDescription());
            artifactMap.put(a.getKey(), subset);
        }

        // Tokens
        for(Token t : tokenDao.findByProjectVersion(project.getKey(), version.getKey(), true)) {
            upgradeToken(b.getPrimaryKey(), t, langMap, artifactMap);
        }
    }

    public void upgradeToken(Object branchKey, Token token, Map<Key, String> langMap, Map<Key, Subset> artifactMap) {
        LOG.info("        TOKN {} for {}", token.getName(), branchKey);
        Tokn tokn = toknDao.persist(branchKey, null, token.getDescription(), token.getName(), token.getViewContext());

        // TokenArtifacts
        for(TokenArtifact ta : tokenArtifactDao.findByToken(token.getKey())) {
            final Subset subset = artifactMap.get(ta.getArtifact());
            LOG.info("            SUBSET_TOKN {} for {}", subset.getName(), token.getName());
            SubsetTokn subsetTokn = subsetToknDao.persist(subset.getPrimaryKey(), tokn.getName(), tokn.getId());
        }

        // translations
        Key projLang;
        for(Translation t : translationDao.findByTokenVersion(token.getKey(), token.getVersion())) {
            LOG.info("            TRANS {} in {}", t.getLocal(), langMap.get(t.getLanguage()));
            String langCode = langMap.get(t.getLanguage());
            projLang = KeyFactory.createKey((Key) branchKey, ProjLang.class.getSimpleName(), langCode);
            Trans trans = transDao.persist(projLang, tokn.getId(), t.getLocal());
        }
    }

    public ProjectLanguageDao getProjectLanguageDao() {
        return projectLanguageDao;
    }

    public void setProjectLanguageDao(ProjectLanguageDao projectLanguageDao) {
        this.projectLanguageDao = projectLanguageDao;
    }

    public LanguageDao getLanguageDao() {
        return languageDao;
    }

    public void setLanguageDao(LanguageDao languageDao) {
        this.languageDao = languageDao;
    }

    public void setTranslationDao(TranslationDao translationDao) {
        this.translationDao = translationDao;
    }

    public TranslationDao getTranslationDao() {
        return translationDao;
    }

    public TokenDao getTokenDao() {
        return tokenDao;
    }

    public void setTokenDao(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public void setMallDao(MallDao mallDao) {
        this.mallDao = mallDao;
    }

    public void setTokenArtifactDao(TokenArtifactDao tokenArtifactDao) {
        this.tokenArtifactDao = tokenArtifactDao;
    }

    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public ProjectDao getProjectDao() {
        return projectDao;
    }

    public void setProjectUserDao(ProjectUserDao projectUserDao) {
        this.projectUserDao = projectUserDao;
    }

    public ProjectUserDao getProjectUserDao() {
        return projectUserDao;
    }

    public void setArtifactDao(ArtifactDao artifactDao) {
        this.artifactDao = artifactDao;
    }

    public void setVersionDao(VersionDao versionDao) {
        this.versionDao = versionDao;
    }

    public final void setViewContextDao(ViewContextDao viewContextDao) {
        this.viewContextDao = viewContextDao;
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

    // ------------------- export old ---------------------
    public List<Project> getProjects() {
        return projectDao.findAll();
    }

    public List<Language> getLanguages() {
        return languageDao.findAll();
    }

    public Language language(Key languageKey) {
        return languageDao.findByPrimaryKey(languageKey);
    }

    public List<Mall> getMalls() {
        return mallDao.findAll();
    }

    public List<ProjectUser> users(Key projectKey) {
        return projectUserDao.findByProject(projectKey);
    }

    public List<Version> versions(Key projectKey) {
        return versionDao.findByProject(projectKey);
    }

    public List<ProjectLanguage> languages(Key projectKey, Key versionKey) {
        return projectLanguageDao.findByProjectVersion(projectKey, versionKey);
    }

    public List<Token> tokens(Key projectKey, Key versionKey) {
        return tokenDao.findByProjectVersion(projectKey, versionKey, true);
    }

    public List<Translation> translations(Key tokenKey) {
        return translationDao.findByToken(tokenKey);
    }

    public List<Artifact> subsets(Key projectKey) {
        return artifactDao.findByProject(projectKey);
    }

    public List<TokenArtifact> subTokens(Key artifactKey, Key versionKey) {
        return tokenArtifactDao.findByArtifactVersion(artifactKey, versionKey);
    }

    public Map<Key, ViewContext> contexts(Key projectKey) {
        Map<Key, ViewContext> contexts = new HashMap<Key, ViewContext>();
        for(ViewContext vc : viewContextDao.findByProject(projectKey)) {
            contexts.put(vc.getKey(), vc);
        }
        return contexts;
    }
}

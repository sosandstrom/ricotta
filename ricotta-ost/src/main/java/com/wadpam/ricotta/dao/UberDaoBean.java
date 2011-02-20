package com.wadpam.ricotta.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

import net.sf.mardao.api.domain.PrimaryKeyEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.domain.Artifact;
import com.wadpam.ricotta.domain.Branch;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.Mall;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.ProjectLanguage;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.TokenArtifact;
import com.wadpam.ricotta.domain.Translation;
import com.wadpam.ricotta.domain.Version;
import com.wadpam.ricotta.model.ProjectLanguageModel;
import com.wadpam.ricotta.model.TranslationModel;

public class UberDaoBean implements UberDao {
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

    private ProjDao            projDao;
    private BranchDao          branchDao;

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
    public void notifyOwner(Project project, Version version, String languageCode, List<String> changes, String from) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(project.getOwner()));
            msg.setSubject("Ricotta: changes in " + project.getName() + ":" + version.getName() + " (" + languageCode + ")");
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

    /**
     * Populates the database with the basic project - ricotta-ost itself!
     */
    protected final Key populate() {
        // populate Languages
        final Language en = languageDao.persist("en", "English");
        final Language en_GB = languageDao.persist("en_GB", "British English");
        final Language sv = languageDao.persist("sv", "Swedish");

        // populate Templates
        final Mall androidStringsInherited = mallDao.persist(MALL_BODY_ANDROID,
                "Android strings.xml with parent default translations", "text/plain", "strings_android_inherit");

        // HEAD version is cross-project
        _HEAD = versionDao.persist(null, "2011-01-28 10:10 GMT+7", "Latest version", VALUE_HEAD, null);
        final Key HEAD = _HEAD.getKey();

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

        // FIXME: remove test entities
        Proj proj = projDao.persist("proj", "test@example.com");
        Branch head = branchDao.persist((Key) proj.getPrimaryKey(), "trunk", "2011-02-20", "proj's trunk");
        Branch integration = branchDao.persist((Key) proj.getPrimaryKey(), "integration", "2011-02-20",
                "proj's integration branch");

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

    public void upgrade() {
        for(Project project : projectDao.findAll()) {
            upgradeProject(project);
        }
    }

    public void upgradeProject(Project project) {
        final Proj proj = projDao.persist(project.getName(), project.getOwner());

        upgradeVersion((Key) proj.getPrimaryKey(), getHead());
        for(Version v : versionDao.findByProject(project.getKey())) {
            upgradeVersion((Key) proj.getPrimaryKey(), v);
        }
    }

    public void upgradeVersion(Key projectKey, Version v) {
        branchDao.persist(projectKey, v.getName(), v.getDatum(), v.getDescription());
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

    public void setProjDao(ProjDao projDao) {
        this.projDao = projDao;
    }

    public void setBranchDao(BranchDao branchDao) {
        this.branchDao = branchDao;
    }
}

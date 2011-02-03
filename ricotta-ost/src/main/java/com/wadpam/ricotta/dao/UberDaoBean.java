package com.wadpam.ricotta.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import net.sf.mardao.api.domain.PrimaryKeyEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Artifact;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.Mall;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.ProjectLanguage;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.TokenArtifact;
import com.wadpam.ricotta.domain.Translation;
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

    public void init() {
        patch();
        populate();
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

    public static String cacheKeyLoadTranslations(Key projectKey, Key languageKey, Key artifactKey) {
        return CACHE_KEY_TRANSLATIONS_PREFIX + projectKey
                + (null == languageKey ? "" : languageKey.toString() + (null != artifactKey ? artifactKey : ""));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void invalidateCache(Key projectKey, Key languageKey, Key artifactKey) {
        if (null != projectKey) {

            // all languages or specific?
            if (null == languageKey) {
                // get the default language
                ProjectLanguage pl = projectLanguageDao.findDefault(projectKey);
                // LOG.debug("invalidateCache {} PL {}", languageKey, pl);
                // invalidate defualt language first:
                invalidateCache(projectKey, pl.getLanguage(), artifactKey);
            }
            else {
                // get the specified language
                ProjectLanguage pl = projectLanguageDao.findByLanguageProject(languageKey, projectKey);
                // LOG.debug("invalidateCache {} PL {}", languageKey, pl);
                // invalidate sub-languages (NOT recursive, only one level)
                for(ProjectLanguage spl : projectLanguageDao.findByParent(pl.getKey())) {
                    invalidateCacheInternal(spl.getProject(), spl.getLanguage(), artifactKey);
                }

                // all artifacts or specific?
                if (null == artifactKey) {
                    // project-language first
                    invalidateCacheInternal(projectKey, languageKey, null);

                    // all!
                    for(Key a : artifactDao.findKeysByProject(projectKey)) {
                        invalidateCache(projectKey, languageKey, a);
                    }
                }
                else {
                    // base: invalidate this
                    invalidateCacheInternal(projectKey, languageKey, artifactKey);
                }

            }
        }
    }

    /** For exact project-language-artifact */
    protected void invalidateCacheInternal(Key projectKey, Key languageKey, Key artifactKey) {
        final String cacheKey = cacheKeyLoadTranslations(projectKey, languageKey, artifactKey);
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
    public List<ProjectLanguageModel> loadProjectLanguages(Key project) {
        final List<ProjectLanguageModel> returnValue = new ArrayList<ProjectLanguageModel>();

        final List<ProjectLanguage> pls = projectLanguageDao.findByProject(project);

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
    public List<TranslationModel> loadTranslations(Key projectKey, Key languageKey, Key artifactKey) {
        final List<TranslationModel> returnValue = new ArrayList<TranslationModel>();
        try {
            // check cache first!
            Cache cache = getCache();
            final String cacheKey = cacheKeyLoadTranslations(projectKey, languageKey, artifactKey);
            final List<TranslationModel> tms = (List<TranslationModel>) cache.get(cacheKey);
            LOG.debug("Cache hit for {}: {}", cacheKey, tms);
            if (null != tms) {
                return tms;
            }

            final ProjectLanguage projectLanguage = projectLanguageDao.findByLanguageProject(languageKey, projectKey);
            List<Token> tokens = null;
            if (null != artifactKey) {
                List<TokenArtifact> mappings = tokenArtifactDao.findByArtifact(artifactKey);
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
                tokens = tokenDao.findByProject(projectKey);
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
                ProjectLanguage parent = projectLanguageDao.findByPrimaryKey(projectLanguage.getParent());
                parents = translationDao.findByLanguageKeyTokens(projectKey, parent.getLanguage(), tokenKeys);
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

            cache.put(cacheKeyLoadTranslations(projectKey, languageKey, artifactKey), returnValue);
        }
        catch (CacheException e) {
            e.printStackTrace();
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
    protected void patch() {
        Map<Key, Key> tokenProjectMap = new HashMap<Key, Key>();
        for(Token t : tokenDao.findAll()) {
            tokenProjectMap.put(t.getKey(), t.getProject());
        }

        // patch all translations:
        for(Translation t : translationDao.findAll()) {
            if (null == t.getProject()) {
                t.setProject(tokenProjectMap.get(t.getToken()));
                translationDao.update(t);
            }
        }
    }

    /**
     * Populates the database with the basic project - ricotta-ost itself!
     */
    protected final void populate() {
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
        ProjectLanguage root = projectLanguageDao.persist(null, project.getKey(), en.getKey());
        projectLanguageDao.persist(root.getKey(), project.getKey(), en_GB.getKey());
        projectLanguageDao.persist(root.getKey(), project.getKey(), sv.getKey());

        // Artifact
        final Artifact ricottaOst = artifactDao.persist(project.getKey(), "ricotta-ost");
        final Artifact ricottaPlugin = artifactDao.persist(project.getKey(), "ricotta-maven-plugin");

        // Tokens
        final Token appTitle = tokenDao.persist(project.getKey(), "appTitle", "The Application title as displayed to the user");
        final Token tokenProject = tokenDao.persist(project.getKey(), "Project", "The Project Entity");

        // Artifact tokens
        final TokenArtifact appTitleOst = tokenArtifactDao.persist(appTitle.getKey(), ricottaOst.getKey(), project.getKey());
        tokenArtifactDao.persist(tokenProject.getKey(), ricottaOst.getKey(), project.getKey());
        tokenArtifactDao.persist(appTitle.getKey(), ricottaPlugin.getKey(), project.getKey());

        // Translations
        translationDao.persist(en.getKey(), project.getKey(), appTitle.getKey(), null, "Ricotta");
        translationDao.persist(en_GB.getKey(), project.getKey(), tokenProject.getKey(), null, "Project");
        translationDao.persist(sv.getKey(), project.getKey(), tokenProject.getKey(), null, "Projekt");
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
}

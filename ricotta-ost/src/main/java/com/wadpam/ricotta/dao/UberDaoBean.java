package com.wadpam.ricotta.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    static final Logger        LOG               = LoggerFactory.getLogger(UberDaoBean.class);

    public static final String MALL_BODY_ANDROID = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
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
        populate();
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

    @Override
    public List<TranslationModel> loadTranslations(Key projectKey, Key languageKey, Key artifactKey) {
        final List<TranslationModel> returnValue = new ArrayList<TranslationModel>();

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

        // this language's tokens
        final Map<Key, Translation> locals = translationDao.findByLanguageKeyTokens(languageKey, tokens);

        // if there is a parent, get its tokens
        Map<Key, Translation> parents = new HashMap<Key, Translation>();
        if (null != projectLanguage.getParent()) {
            parents = translationDao.findByLanguageKeyTokens(projectLanguage.getParent(), tokens);
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

        // Artifact
        final Artifact ricottaOst = artifactDao.persist(project.getKey(), "ricotta-ost");

        // Tokens
        final Token appTitle = tokenDao.persist(project.getKey(), "appTitle", "The Application title as displayed to the user");
        final Token tokenProject = tokenDao.persist(project.getKey(), "Project", "The Project Entity");

        // Artifact tokens

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

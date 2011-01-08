package com.wadpam.ricotta.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Language;
import com.wadpam.ricotta.domain.PrimaryKeyEntity;
import com.wadpam.ricotta.domain.ProjectLanguage;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Translation;
import com.wadpam.ricotta.model.ProjectLanguageModel;
import com.wadpam.ricotta.model.TranslationModel;

public class UberDaoBean implements UberDao {
    private LanguageDao        languageDao;
    private MallDao            mallDao;
    private ProjectLanguageDao projectLanguageDao;
    private TokenDao           tokenDao;
    private TranslationDao     translationDao;

    public void init() {
        // mallDao.persist("The normal properties file layout", "properties", "#This is the file\n#End-of-file");
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
    public List<TranslationModel> loadTranslations(Key projectKey, Key languageKey) {
        final List<TranslationModel> returnValue = new ArrayList<TranslationModel>();

        final ProjectLanguage projectLanguage = projectLanguageDao.findByLanguageProject(languageKey, projectKey);
        final List<Token> tokens = tokenDao.findByProject(projectKey);

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
                // TODO Auto-generated method stub
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
}

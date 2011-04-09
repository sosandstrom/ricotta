package com.wadpam.ricotta.dao;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.Tokn;
import com.wadpam.ricotta.domain.Trans;
import com.wadpam.ricotta.domain.Version;
import com.wadpam.ricotta.model.ProjectLanguageModel;
import com.wadpam.ricotta.model.TransModel;
import com.wadpam.ricotta.model.TranslationModel;

public interface UberDao {

    final static String VALUE_HEAD = "HEAD";

    List<ProjectLanguageModel> loadProjectLanguages(Key project, Key version);

    List<TranslationModel> loadTranslations(Key project, Key version, Key language, Key artifactKey);

    void deleteTokens(List<Key> keys);

    void invalidateCache(Key projectKey, Key versionKey, Key languageKey, Key artifactKey);

    void notifyOwner(Project project, Version version, String languageCode, List<String> changes, String from);

    void cloneVersion(Project project, Key from, Version version);

    Version getHead();

    void deleteVersion(Project project, String vk);

    Collection<TransModel> loadTrans(Key branchKey, Key subsetKey, ProjLang projLang, Key ctxtKey);

    List<String> updateTrans(Key projLangKey, Tokn token, Trans t, String name, String value, boolean delete);

    void notifyOwner(Proj proj, String branchName, String langCode, List<String> changes, String from);

    void importBody(HttpServletRequest request, Key branchKey, String langCode, String regexp, String body);

}

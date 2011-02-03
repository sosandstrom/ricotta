package com.wadpam.ricotta.dao;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.model.ProjectLanguageModel;
import com.wadpam.ricotta.model.TranslationModel;

public interface UberDao {

    List<ProjectLanguageModel> loadProjectLanguages(Key project);

    List<TranslationModel> loadTranslations(Key project, Key language, Key artifactKey);

    void deleteTokens(List<Key> keys);

    void invalidateCache(Key projectKey, Key languageKey, Key artifactKey);

}

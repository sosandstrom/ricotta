package com.wadpam.ricotta.dao;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Project;
import com.wadpam.ricotta.domain.Version;
import com.wadpam.ricotta.model.ProjectLanguageModel;
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

}

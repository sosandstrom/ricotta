package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.ProjectLanguage;

/**
 * Business Methods interface for entity ProjectLanguage.
 */
public interface ProjectLanguageDao extends GeneratedProjectLanguageDao {

    ProjectLanguage persist(Key parentKey, Key projectKey, Key languageKey);

    ProjectLanguage findDefault(Key projectKey);

}

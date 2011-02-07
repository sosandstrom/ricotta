package com.wadpam.ricotta.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Translation;

/**
 * Business Methods interface for entity Translation.
 */
public interface TranslationDao extends GeneratedTranslationDao {

    /**
     * @return a <tokenKey, Translation> Map
     */
    Map<Key, Translation> findByLanguageKeyTokens(Key projectKey, Key languageKey, Set<Key> tokenKeys);

    List<Key> findKeysByTokenLanguageVersion(Key token, Key language, Key version);

    List<Translation> findByTokenVersion(Key tokenKey, Key versionKey);

    // Translation persist(Key language, Key project, Key token, Key version, String value);

}

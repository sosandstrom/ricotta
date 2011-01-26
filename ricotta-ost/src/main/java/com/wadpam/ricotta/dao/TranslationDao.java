package com.wadpam.ricotta.dao;

import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Translation;

/**
 * Business Methods interface for entity Translation.
 */
public interface TranslationDao extends GeneratedTranslationDao {

    /**
     * @return a <tokenKey, Translation> Map
     */
    Map<Key, Translation> findByLanguageKeyTokens(Key languageKey, List<Token> tokens);

}

package com.wadpam.ricotta.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Translation;

/**
 * Implementation of Business Methods related to entity Translation.
 */
public class TranslationDaoBean extends AbstractTranslationDao implements TranslationDao {

    /**
     * @return a <tokenKey, Translation> Map
     */
    @Override
    public Map<Key, Translation> findByLanguageKeyTokens(Key languageKey, List<Token> tokens) {
        final Map<Key, Translation> returnValue = new HashMap<Key, Translation>();
        if (false == tokens.isEmpty()) {
            final Expression eqLang = new Expression(COLUMN_NAME_LANGUAGE, "=", languageKey);
            final Expression inTokens = new Expression.IN(COLUMN_NAME_TOKEN, UberDaoBean.getKeys(tokens));

            final List<Translation> translations = findBy(null, false, -1, eqLang, inTokens);
            for(Translation t : translations) {
                returnValue.put(t.getToken(), t);
            }
        }
        return returnValue;
    }
}

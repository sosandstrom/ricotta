package com.wadpam.ricotta.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.mardao.api.dao.Expression;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Translation;

/**
 * Implementation of Business Methods related to entity Translation.
 */
public class TranslationDaoBean extends GeneratedTranslationDaoImpl implements TranslationDao {

    /**
     * @return a <tokenKey, Translation> Map
     */
    @Override
    public Map<Key, Translation> findByLanguageKeyTokens(Key languageKey, List<Token> tokens) {
        final Map<Key, Translation> returnValue = new HashMap<Key, Translation>();
        if (false == tokens.isEmpty()) {
            final Expression eqLang = new Expression(COLUMN_NAME_LANGUAGE, FilterOperator.EQUAL, languageKey);
            final Expression inTokens = new Expression(COLUMN_NAME_TOKEN, FilterOperator.IN, UberDaoBean.getKeys(tokens));

            final List<Translation> translations = findBy(null, false, -1, eqLang, inTokens);
            for(Translation t : translations) {
                returnValue.put(t.getToken(), t);
            }
        }
        return returnValue;
    }
}

package com.wadpam.ricotta.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mardao.api.dao.Expression;
import net.sf.mardao.api.dao.FilterEqual;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.wadpam.ricotta.domain.Translation;

/**
 * Implementation of Business Methods related to entity Translation.
 */
public class TranslationDaoBean extends GeneratedTranslationDaoImpl implements TranslationDao {

    /**
     * @return a <tokenKey, Translation> Map
     */
    @Override
    public Map<Key, Translation> findByLanguageKeyTokens(Key projectKey, Key languageKey, Set<Key> tokenKeys) {
        final Map<Key, Translation> returnValue = new HashMap<Key, Translation>();
        if (false == tokenKeys.isEmpty()) {
            final Expression eqLang = new Expression(COLUMN_NAME_LANGUAGE, FilterOperator.EQUAL, languageKey);
            final Expression eqProject = new FilterEqual(COLUMN_NAME_PROJECT, projectKey);
            LOGGER.debug("findByLanguage {} Project {}", languageKey, projectKey);

            final List<Translation> translations = findBy(null, false, -1, 0, eqLang, eqProject);
            for(Translation t : translations) {
                if (tokenKeys.contains(t.getToken())) {
                    returnValue.put(t.getToken(), t);
                }
            }
            LOGGER.debug("filtered {} translations from {}", returnValue.size(), translations.size());
        }
        return returnValue;
    }

    @Override
    public List<Key> findKeysByTokenLanguageVersion(Key token, Key language, Key version) {
        return findKeysBy(null, false, -1, 0, new FilterEqual(COLUMN_NAME_TOKEN, token), new FilterEqual(COLUMN_NAME_LANGUAGE,
                language));
    }
}

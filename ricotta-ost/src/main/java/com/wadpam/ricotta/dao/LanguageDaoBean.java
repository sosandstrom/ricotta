package com.wadpam.ricotta.dao;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Language;

/**
 * Implementation of Business Methods related to entity Language.
 */
public class LanguageDaoBean extends GeneratedLanguageDaoImpl implements LanguageDao {

    private final Map<Key, Language> _map = new HashMap<Key, Language>();

    @Override
    public Language findByPrimaryKey(Key primaryKey) {
        Language returnValue = _map.get(primaryKey);
        if (null == returnValue) {
            returnValue = super.findByPrimaryKey(primaryKey);
            if (null != returnValue) {
                _map.put(primaryKey, returnValue);
            }
        }
        return returnValue;
    }

}

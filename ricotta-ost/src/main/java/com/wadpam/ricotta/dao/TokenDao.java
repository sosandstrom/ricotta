package com.wadpam.ricotta.dao;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Token;

/**
 * Business Methods interface for entity Token.
 */
public interface TokenDao extends GeneratedTokenDao {

    List<Token> findByNameProjectVersion(String tokenName, Key key, Key versionKey);

    List<Token> findByProject(Key projectKey, boolean ascending);

    List<Token> findByProjectVersion(Key projectKey, Key versionKey, boolean ascending);

}

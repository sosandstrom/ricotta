package com.wadpam.ricotta.dao;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Token;

/**
 * Business Methods interface for entity Token.
 */
public interface TokenDao extends GeneratedTokenDao {

    List<Token> findByNameProject(String tokenName, Key key);

    List<Token> findByProject(Key projectKey, boolean ascending);

}

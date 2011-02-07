package com.wadpam.ricotta.dao;

import java.util.HashMap;
import java.util.List;

import net.sf.mardao.api.dao.FilterEqual;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Token;

/**
 * Implementation of Business Methods related to entity Token.
 */
public class TokenDaoBean extends GeneratedTokenDaoImpl implements TokenDao {

    @Override
    public List<Token> findByNameProjectVersion(String tokenName, Key projectKey, Key versionKey) {
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put(COLUMN_NAME_NAME, tokenName);
        args.put(COLUMN_NAME_PROJECT, projectKey);
        args.put(COLUMN_NAME_VERSION, versionKey);
        return findBy(args, null, false);
    }

    @Override
    public final List<Token> findByProject(Key projectKey, boolean ascending) {
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put(COLUMN_NAME_PROJECT, projectKey);
        return findBy(args, COLUMN_NAME_NAME, ascending);
    }

    @Override
    public List<Token> findByProjectVersion(Key projectKey, Key versionKey, boolean ascending) {
        return findBy(COLUMN_NAME_NAME, ascending, -1, 0, new FilterEqual(COLUMN_NAME_PROJECT, projectKey), new FilterEqual(
                COLUMN_NAME_VERSION, versionKey));
    }

    // @Override
    // public Token persist(Key projectKey, String name, String description) {
    // List<Token> ts = findByNameProject(name, projectKey);
    // if (ts.isEmpty()) {
    // Token t = new Token();
    // t.setProject(projectKey);
    // t.setName(name);
    // t.setDescription(description);
    // persist(t);
    // return t;
    // }
    // return ts.get(0);
    // }

}

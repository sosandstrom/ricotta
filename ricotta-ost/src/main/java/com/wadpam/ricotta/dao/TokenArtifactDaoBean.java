package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.TokenArtifact;

/**
 * Implementation of Business Methods related to entity TokenArtifact.
 */
public class TokenArtifactDaoBean extends GeneratedTokenArtifactDaoImpl implements TokenArtifactDao {

    @Override
    public TokenArtifact persist(Key tokenKey, Key artifactKey, Key projectKey) {
        TokenArtifact returnValue = findByArtifactToken(artifactKey, tokenKey);
        if (null == returnValue) {
            returnValue = new TokenArtifact();
            returnValue.setToken(tokenKey);
            returnValue.setArtifact(artifactKey);
            returnValue.setProject(projectKey);
            persist(returnValue);
        }
        return returnValue;
    }

}

package com.wadpam.ricotta.dao;

import java.util.List;

import net.sf.mardao.api.dao.FilterEqual;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.TokenArtifact;

/**
 * Implementation of Business Methods related to entity TokenArtifact.
 */
public class TokenArtifactDaoBean extends GeneratedTokenArtifactDaoImpl implements TokenArtifactDao {

    // @Override
    // public TokenArtifact persist(Key tokenKey, Key artifactKey, Key projectKey) {
    // TokenArtifact returnValue = findByArtifactToken(artifactKey, tokenKey);
    // if (null == returnValue) {
    // returnValue = new TokenArtifact();
    // returnValue.setToken(tokenKey);
    // returnValue.setArtifact(artifactKey);
    // returnValue.setProject(projectKey);
    // persist(returnValue);
    // }
    // return returnValue;
    // }

    @Override
    public List<TokenArtifact> findByArtifactVersion(Key artifactKey, Key versionKey) {
        return findBy(null, false, -1, 0, new FilterEqual(COLUMN_NAME_ARTIFACT, artifactKey), new FilterEqual(
                COLUMN_NAME_VERSION, versionKey));
    }
}

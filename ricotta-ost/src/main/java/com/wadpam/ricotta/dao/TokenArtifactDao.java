package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.TokenArtifact;

/**
 * Business Methods interface for entity TokenArtifact.
 */
public interface TokenArtifactDao extends GeneratedTokenArtifactDao {

    TokenArtifact persist(Key tokenKey, Key artifactKey, Key projectKey);

}

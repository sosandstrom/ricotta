package com.wadpam.ricotta.dao;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.TokenArtifact;

/**
 * Business Methods interface for entity TokenArtifact.
 */
public interface TokenArtifactDao extends GeneratedTokenArtifactDao {

    List<TokenArtifact> findByArtifactVersion(Key artifactKey, Key versionKey);

}

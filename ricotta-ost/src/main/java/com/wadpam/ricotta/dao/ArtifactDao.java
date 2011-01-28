package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Artifact;

/**
 * Business Methods interface for entity Artifact.
 */
public interface ArtifactDao extends GeneratedArtifactDao {

    Artifact persist(Key key, String string);

}

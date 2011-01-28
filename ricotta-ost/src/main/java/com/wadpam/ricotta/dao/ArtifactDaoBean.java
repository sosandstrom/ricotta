package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Artifact;

/**
 * Implementation of Business Methods related to entity Artifact.
 */
public class ArtifactDaoBean extends GeneratedArtifactDaoImpl implements ArtifactDao {

    @Override
    public Artifact persist(Key projectKey, String artifactName) {
        Artifact a = findByNameProject(artifactName, projectKey);
        if (null == a) {
            a = new Artifact();
            a.setProject(projectKey);
            a.setName(artifactName);
            persist(a);
        }
        return a;
    }

}

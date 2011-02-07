package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"token", "artifact"})})
public class TokenArtifact extends AEDPrimaryKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Key key;

    // ManyToOne
    @Basic
    Key project;

    // ManyToOne
    @Basic
    Key token;

    // ManyToOne
    @Basic
    Key artifact;

    // ManyToOne
    @Basic
    Key version;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + token + '.' + artifact + ',' + key + '}';
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Key getToken() {
        return token;
    }

    public void setToken(Key token) {
        this.token = token;
    }

    public Key getArtifact() {
        return artifact;
    }

    public void setArtifact(Key artifact) {
        this.artifact = artifact;
    }

    @Override
    public Object getPrimaryKey() {
        return key;
    }

    @Override
    public String getKeyString() {
        return (null != token && null != artifact) ? KeyFactory.keyToString(token) + '.' + KeyFactory.keyToString(artifact)
                : null;
    }

    public Key getProject() {
        return project;
    }

    public void setProject(Key project) {
        this.project = project;
    }

    public Key getVersion() {
        return version;
    }

    public void setVersion(Key version) {
        this.version = version;
    }

}

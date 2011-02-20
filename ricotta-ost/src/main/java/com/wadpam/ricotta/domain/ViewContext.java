package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project", "name"})})
public class ViewContext extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = 7269107590420055343L;

    @Id
    private Key               key;

    @Basic
    private Key               project;
    private String            name;
    private String            description;
    private BlobKey           blobKey;

    @Override
    public Object getSimpleKey() {
        return key;
    }

    @Override
    public String getKeyString() {
        if (null == key) {
            return "";
        }
        return super.getKeyString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    public void setProject(Key project) {
        this.project = project;
    }

    public Key getProject() {
        return project;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setBlobKey(BlobKey blobKey) {
        this.blobKey = blobKey;
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

}

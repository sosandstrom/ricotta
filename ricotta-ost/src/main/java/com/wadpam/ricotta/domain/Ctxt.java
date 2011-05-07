package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;

@Entity
public class Ctxt extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = 7269107590420055343L;
    @Parent(kind = "Branch")
    private Key               branch;

    @Id
    private String            name;

    @Basic
    private String            description;
    private BlobKey           blobKey;

    @Override
    public Object getSimpleKey() {
        return name;
    }

    @Override
    public Object getParentKey() {
        return branch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public final Key getBranch() {
        return branch;
    }

    public final void setBranch(Key branch) {
        this.branch = branch;
    }

}

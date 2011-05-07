package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;
import net.sf.mardao.api.domain.PrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
public class Subset extends AEDPrimaryKeyEntity implements PrimaryKeyEntity {
    private static final long serialVersionUID = -3099992336486992018L;

    @Id
    String                    name;

    String                    description;

    // ManyToOne
    @Parent(kind = "Branch")
    Key                       branch;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + branch + ',' + name + '}';
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Key getBranch() {
        return branch;
    }

    public void setBranch(Key branch) {
        this.branch = branch;
    }

}

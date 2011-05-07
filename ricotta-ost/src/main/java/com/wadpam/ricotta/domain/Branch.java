package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
public class Branch extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = -5395287576673002330L;

    @Parent(kind = "Proj")
    Key                       project;

    @Id
    String                    name;

    String                    description;

    String                    datum;

    @Override
    public Key getParentKey() {
        return project;
    }

    @Override
    public Object getSimpleKey() {
        return name;
    }

    @Override
    public String toString() {
        return "Branch{" + project + ',' + name + ',' + datum + '}';
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

    public Key getProject() {
        return project;
    }

    public void setProject(Key project) {
        this.project = project;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }
}

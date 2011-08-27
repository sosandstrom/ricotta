package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDStringEntity;

import com.google.appengine.api.datastore.Key;

@Entity
public class Branch extends AEDStringEntity {
    private static final long serialVersionUID = -5395287576673002330L;

    @Parent(kind = "Proj")
    Key                       project;

    @Id
    String                    name;

    String                    description;

    @Override
    public Key getParentKey() {
        return project;
    }

    @Override
    public String getSimpleKey() {
        return name;
    }

    @Override
    public String toString() {
        return "Branch{" + project + ',' + name + '}';
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

}

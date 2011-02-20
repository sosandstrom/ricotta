package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;
import net.sf.mardao.api.domain.PrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project", "version", "name", "description"})})
public class Token extends AEDPrimaryKeyEntity implements PrimaryKeyEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Key                       key;

    String                    name;

    String                    description;

    // ManyToOne
    @Basic
    Key                       project;

    // ManyToOne
    @Basic
    Key                       version;

    // ManyToOne
    @Basic
    Key                       viewContext;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + name + ',' + description + ',' + key + '}';
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
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

    @Override
    public Object getSimpleKey() {
        return key;
    }

    public Key getVersion() {
        return version;
    }

    public void setVersion(Key version) {
        this.version = version;
    }

    public Key getViewContext() {
        return viewContext;
    }

    public void setViewContext(Key viewContext) {
        this.viewContext = viewContext;
    }

}

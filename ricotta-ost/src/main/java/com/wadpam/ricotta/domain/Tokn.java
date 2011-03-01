package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project", "version", "name", "description"})})
public class Tokn extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = 1L;
    @Parent(kind = "Branch")
    Key                       branch;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long                      id;

    String                    name;

    String                    description;

    @Basic
    Key                       viewContext;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + name + ',' + description + ',' + id + '}';
    }

    @Override
    public Object getSimpleKey() {
        return id;
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

    public Key getViewContext() {
        return viewContext;
    }

    public void setViewContext(Key viewContext) {
        this.viewContext = viewContext;
    }

    public final Long getId() {
        return id;
    }

    public final void setId(Long id) {
        this.id = id;
    }

}

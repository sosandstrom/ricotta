package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.appengine.api.datastore.Key;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "project"})})
public class Artifact extends AbstractPrimaryKeyEntity implements PrimaryKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Key    key;

    String name;

    String description;

    // ManyToOne
    @Basic
    Key    project;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + name + ',' + project + ',' + key + '}';
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
    public Object getPrimaryKey() {
        return key;
    }

}

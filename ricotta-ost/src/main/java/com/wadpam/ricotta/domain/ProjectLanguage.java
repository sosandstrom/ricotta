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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project", "language"}),
        @UniqueConstraint(columnNames = {"parent", "language"})})
public class ProjectLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Key key;

    // ManyToOne
    @Basic
    Key parent;

    // ManyToOne
    @Basic
    Key project;

    // ManyToOne
    @Basic
    Key language;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + parent + ',' + project + ',' + language + ',' + key + '}';
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Key getParent() {
        return parent;
    }

    public void setParent(Key parent) {
        this.parent = parent;
    }

    public Key getLanguage() {
        return language;
    }

    public void setLanguage(Key language) {
        this.language = language;
    }

    public Key getProject() {
        return project;
    }

    public void setProject(Key project) {
        this.project = project;
    }

}

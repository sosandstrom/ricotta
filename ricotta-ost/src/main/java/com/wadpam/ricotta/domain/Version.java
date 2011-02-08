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

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "project"})})
public class Version extends AEDPrimaryKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Key    key;

    String name;

    String description;

    String datum;

    // @ManyToOne
    @Basic
    Key    project;

    @Override
    public String toString() {
        return "Version{" + name + ',' + datum + '}';
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

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    @Override
    public Object getPrimaryKey() {
        return key;
    }

}

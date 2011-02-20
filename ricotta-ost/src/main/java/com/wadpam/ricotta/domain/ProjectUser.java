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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project", "user"})})
public class ProjectUser extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = 3376949683379424856L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Key                       key;

    String                    user;

    // ManyToOne
    @Basic
    Key                       project;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + project + '-' + user + ',' + key + '}';
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public Object getSimpleKey() {
        return key;
    }

    public Key getProject() {
        return project;
    }

    public void setProject(Key project) {
        this.project = project;
    }

}

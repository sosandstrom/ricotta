package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
public class ProjUser extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = 3376949683379424856L;
    @Parent(kind = "Proj")
    Key                       proj;

    @Id
    String                    user;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + proj + ',' + user + '}';
    }

    @Override
    public Key getParentKey() {
        return proj;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public Object getSimpleKey() {
        return user;
    }

    public Key getProj() {
        return proj;
    }

    public void setProj(Key proj) {
        this.proj = proj;
    }

}

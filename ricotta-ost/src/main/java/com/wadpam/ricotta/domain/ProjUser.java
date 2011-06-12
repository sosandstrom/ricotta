package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
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

    @Basic
    Long                      role;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + proj + ',' + user + ", 0b" + Long.toBinaryString(role) + '}';
    }

    @Override
    public Key getParentKey() {
        return proj;
    }

    @Override
    public Object getSimpleKey() {
        return user;
    }

    public boolean isTrans() {
        return null != role && 0 != (role & Role.GRANT_TRANS);
    }

    public boolean isTokn() {
        return null != role && 0 != (role & Role.GRANT_TOKN);
    }

    public boolean isManage() {
        return null != role && 0 != (role & Role.GRANT_MANAGE);
    }

    public boolean isDestroy() {
        return null != role && 0 != (role & Role.GRANT_DESTROY);
    }

    public boolean isEverything() {
        return null != role && 0 != (role & Role.GRANT_ALL);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Key getProj() {
        return proj;
    }

    public void setProj(Key proj) {
        this.proj = proj;
    }

    public final Long getRole() {
        return role;
    }

    public final void setRole(Long role) {
        this.role = role;
    }

}

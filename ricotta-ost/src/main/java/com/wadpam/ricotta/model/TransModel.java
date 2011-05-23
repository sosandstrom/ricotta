package com.wadpam.ricotta.model;

import java.io.Serializable;

import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Tokn;
import com.wadpam.ricotta.domain.Trans;

public class TransModel extends AEDPrimaryKeyEntity implements Serializable, Comparable<TransModel> {
    private static final long serialVersionUID = 1059177606423578865L;

    private Key               key;
    private Tokn              token;
    private Trans             local;
    private Trans             parent;

    @Override
    public Object getSimpleKey() {
        return key;
    }

    public Tokn getToken() {
        return token;
    }

    public void setToken(Tokn token) {
        this.token = token;
    }

    public Trans getLocal() {
        return local;
    }

    public void setLocal(Trans local) {
        this.local = local;
    }

    public Trans getParent() {
        return parent;
    }

    public void setParent(Trans parent) {
        this.parent = parent;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    @Override
    public int compareTo(TransModel other) {
        return this.token.getName().compareTo(other.token.getName());
    }
}

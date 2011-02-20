package com.wadpam.ricotta.model;

import java.io.Serializable;

import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Translation;

public class TranslationModel extends AEDPrimaryKeyEntity implements Serializable {
    private static final long serialVersionUID = 1059177606423578865L;

    private Key               key;
    private Token             token;
    private Translation       local;
    private Translation       parent;

    @Override
    public Object getSimpleKey() {
        return key;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Translation getLocal() {
        return local;
    }

    public void setLocal(Translation local) {
        this.local = local;
    }

    public Translation getParent() {
        return parent;
    }

    public void setParent(Translation parent) {
        this.parent = parent;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }
}

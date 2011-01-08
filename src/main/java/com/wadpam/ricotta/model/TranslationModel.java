package com.wadpam.ricotta.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Translation;

public class TranslationModel {
    private Key         key;
    private Token       token;
    private Translation local;
    private Translation parent;

    public String getKeyString() {
        return (null != key) ? KeyFactory.keyToString(key) : null;
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

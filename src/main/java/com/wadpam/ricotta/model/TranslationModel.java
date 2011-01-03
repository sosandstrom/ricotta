package com.wadpam.ricotta.model;

import com.wadpam.ricotta.domain.Token;
import com.wadpam.ricotta.domain.Translation;

public class TranslationModel {
    private Token       token;
    private Translation local;
    private Translation parent;

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
}

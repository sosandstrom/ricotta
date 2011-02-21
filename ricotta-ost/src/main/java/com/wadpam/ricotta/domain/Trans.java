package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
// @Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"token", "language", "version"})})
public class Trans extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = -5659004527175071885L;
    @Parent(kind = "Tokn")
    Key                       token;

    @Id
    String                    langCode;

    String                    local;

    // ManyToOne
    @Basic
    Key                       lang;

    @Override
    public String toString() {
        return "Translation{" + local + ", tokenKey=" + token + ", languageKey=" + lang + '}';
    }

    @Override
    public Object getSimpleKey() {
        return langCode;
    }

    @Override
    public Object getParentKey() {
        return token;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public Key getToken() {
        return token;
    }

    public void setToken(Key token) {
        this.token = token;
    }

    public Key getLang() {
        return lang;
    }

    public void setLang(Key langKey) {
        this.lang = langKey;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

}

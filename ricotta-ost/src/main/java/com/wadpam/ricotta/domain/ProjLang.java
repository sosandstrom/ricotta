package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDStringEntity;

import com.google.appengine.api.datastore.Key;

@Entity
public class ProjLang extends AEDStringEntity {
    private static final long serialVersionUID = -5999417886086767472L;

    @Parent(kind = "Branch")
    Key                       branch;

    @Id
    String                    langCode;

    @Basic
    Key                       lang;

    /**
     * Default Lang, not Parent ProjLang!
     */
    @Basic
    Key                       defaultLang;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + branch + ',' + langCode + '(' + defaultLang + ")}";
    }

    @Override
    public String getSimpleKey() {
        return langCode;
    }

    @Override
    public Key getParentKey() {
        return branch;
    }

    public Key getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(Key defaultLang) {
        this.defaultLang = defaultLang;
    }

    public Key getLang() {
        return lang;
    }

    public void setLang(Key language) {
        this.lang = language;
    }

    public Key getBranch() {
        return branch;
    }

    public void setBranch(Key branch) {
        this.branch = branch;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

}

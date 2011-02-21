package com.wadpam.ricotta.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
// @Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project", "language", "version"})})
public class ProjLang extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = -5999417886086767472L;

    @Parent(kind = "Branch")
    Key                       branch;

    @Id
    String                    langCode;

    @Basic
    Key                       language;

    /**
     * Parent Language, not Parent ProjectLanguage!
     */
    @Basic
    Key                       parentLang;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + branch + ',' + langCode + '(' + parentLang + ")}";
    }

    @Override
    public Object getSimpleKey() {
        return langCode;
    }

    @Override
    public Object getParentKey() {
        return branch;
    }

    public Key getParentLang() {
        return parentLang;
    }

    public void setParentLang(Key parentLang) {
        this.parentLang = parentLang;
    }

    public Key getLanguage() {
        return language;
    }

    public void setLanguage(Key language) {
        this.language = language;
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
